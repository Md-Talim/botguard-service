package me.mdtalim.botguard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.dto.request.CreateBotRequest;
import me.mdtalim.botguard.entity.Bot;
import me.mdtalim.botguard.repository.BotRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
public class BotController {

    private final BotRepository botRepository;

    @PostMapping
    public ResponseEntity<Bot> createBot(@Valid @RequestBody CreateBotRequest req) {
        Bot bot = Bot.builder().name(req.getName()).personaDescription(req.getPersonaDescription()).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(botRepository.save(bot));
    }

    @GetMapping
    public ResponseEntity<?> getAllBots() {
        return ResponseEntity.ok(botRepository.findAll());
    }
}
