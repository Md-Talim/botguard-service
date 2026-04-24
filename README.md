# BotGuard

Spring Boot microservice with Redis-backed guardrails for AI bot
rate limiting and concurrency control.

## Getting Started

### Prerequisites

- Docker + Docker Compose
- Java 21
- Maven

### Run locally

```sh
git clone https://github.com/Md-Talim/botguard-service
cd botguard-service
docker compose up -d # starts Postgres + Redis
./mvnw spring-boot:run # starts the app on :8080
```

Flyway migrations run automatically on startup.

## Project Structure

```
src/main/java/me/mdtalim/botguard/
├── controller/         # REST layer
├── service/            # Business logic (PostService, GuardrailService, ViralityService, NotificationService)
├── scheduler/          # CRON jobs (NotificationSweeper)
├── entity/             # JPA entities
├── repository/         # Spring Data repositories
├── dto/                # Request/Response DTOs
├── redis/              # RedisKeys constants
├── config/             # RedisConfig
└── exception/          # Global exception handler + custom exceptions
```

## API Reference

### Posts

| Method | Endpoint                 | Description        |
| ------ | ------------------------ | ------------------ |
| POST   | /api/posts               | Create a post      |
| POST   | /api/posts/{id}/comments | Add a comment      |
| POST   | /api/posts/{id}/like     | Like a post        |
| GET    | /api/posts/{id}/virality | Get virality score |

### Request bodies

POST /api/posts

```jsonc
{
    "authorType": "USER", // USER or BOT
    "authorId": 1,
    "content": "Hello world",
}
```

POST /api/posts/{id}/comments

```jsonc
{
    "authorType": "BOT",
    "authorId": 1,
    "content": "Nice post!",
    "parentCommentId": null, // null for top-level, comment id for reply
}
```

POST /api/posts/{id}/like

```jsonc
{
    "userId": 1,
}
```

## Architecture

### Redis as Gatekeeper, PostgreSQL as Source of Truth

All guardrails are checked in Redis before any DB write is attempted.
If Redis rejects the request, the DB is never touched.
If a DB write fails after Redis has been updated, the Redis counter
is rolled back manually (see tradeoffs section).

### Redis Key Schema

| Key                          | Type   | TTL    | Purpose                      |
| ---------------------------- | ------ | ------ | ---------------------------- |
| post:{id}:virality_score     | STRING | none   | Weighted engagement score    |
| post:{id}:bot_count          | STRING | none   | Total bot replies on a post  |
| cooldown:bot*{id}:human*{id} | STRING | 10 min | Per-bot-per-human rate limit |
| notif:cooldown:user\_{id}    | STRING | 15 min | Notification rate limit      |
| user:{id}:pending_notifs     | LIST   | none   | Queued notification messages |

## Phase 2: Guardrails: How Thread Safety is Guaranteed

This is the core engineering challenge of the project.
Three guardrails protect bot interactions:

### 1. Horizontal Cap (max 100 bot replies per post)

Naive approach (wrong):

```
GET bot_count -> check < 100 -> INCR
Problem: two threads both read 99, both pass the check, both write -> 101
```

**Correct approach used here:**

```
INCR post:1:bot_count - returns new value atomically
if newValue > 100, DECR to rollback, reject with 429
if newValue <= 100, proceed
```

Redis INCR is atomic. No two concurrent threads can receive the same
return value. The thread that gets 101 is deterministically the one
that gets rejected. Under 200 concurrent requests, the counter will
stop at exactly 100, the race condition is impossible by design.

### 2. Depth Cap (max 20 levels of nesting)

No Redis needed. Depth is deterministic, calculated from the parent
comment's depth_level + 1 at write time. Enforced in the service layer
before any Redis or DB interaction.

### 3. Cooldown Cap (bot cannot interact with same human twice in 10 min)

Approach: `SET NX EX` (Set if Not eXists + Expiry in one atomic command)

```
SET cooldown:bot_1:human_2 "1" NX EX 600
```

This is the cleanest lock pattern in Redis. One command, atomic, self-cleaning.
No separate TTL call needed.

**The tradeoff**: the cooldown resets on the first interaction. If a bot spams
at t=0 and t=599, they both succeed (second one finds key about to expire).
Acceptable for this use case.

## Phase 3: Notification Engine

### How it works

When a bot interacts with a human's post:

1. Check if `notif:cooldown:user_{id}` exists (SET NX EX 900)
    - Key was SET (first interaction in 15 min): log immediate push,
      cooldown starts
    - Key already existed: push message into `user:{id}:pending_notifs` LIST

2. CRON sweeper runs every 5 minutes:
    - Scans for all `user:*:pending_notifs` keys
    - For each key: reads all messages, deletes the LIST, logs summary
    - Does NOT touch the cooldown key, it expires on its own after 15 min

Result: user receives at most one immediate push per 15-minute window.
Subsequent bot interactions in that window are queued and delivered as
a single summary. The 5-minute sweep cadence only affects how quickly
the batch is delivered, not the 15-minute notification rate limit.

## Virality Score

Posts accumulate a weighted engagement score in Redis:

- Bot reply = +1 (low signal, automated)
- Human like = +20 (real person noticed)
- Human comment = +50 (real person engaged)

Scores are updated via Redis INCRBY (atomic), no locking needed since
virality is write-only (no read-then-write race possible).

Use cases: feed ranking, trending detection, bot spam identification
(a post with 500 points from 500 bot replies looks very different from
one with 500 points from 10 human comments).

## Tradeoffs & Known Limitations

### Redis-DB consistency

If a DB write fails after a Redis counter has been incremented,
the counter is manually decremented in a catch block to keep state
in sync. The reverse (DB succeeds, Redis virality update fails) is
tolerated, virality is a derived metric, not a gating value. A missed
increment slightly understates engagement but does not corrupt data and
can be recalculated from DB if needed.

### KEYS vs SCAN in sweeper

The notification sweeper uses KEYS `user:*:pending_notifs` for
simplicity. In production this would be replaced with cursor-based
SCAN to avoid blocking Redis on large keyspaces.

### Cooldown window edge case

The cooldown starts on the first interaction. A bot interacting at
t=0 and again at t=599 (1 second before expiry) will both succeed.
Acceptable for this use case, the intent is spam prevention, not
perfect rate limiting.

## Testing the Concurrency Guarantee

To verify the Horizontal Cap stops at exactly 100 under load:

# Fire 200 concurrent requests

```
seq 200 | xargs -P 200 -I {} curl -s -o /dev/null -w "%{http_code}\n" \
 -X POST http://localhost:8080/api/posts/1/comments \
 -H "Content-Type: application/json" \
 -d '{"authorType":"BOT","authorId":1,"content":"spam","parentCommentId":null}'
```

Expected: exactly 100 responses with 201, exactly 100 with 429.
Check DB: `SELECT COUNT(*) FROM comments WHERE post_id = 1` - must equal 100.
