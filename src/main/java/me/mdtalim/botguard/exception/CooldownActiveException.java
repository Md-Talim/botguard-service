package me.mdtalim.botguard.exception;

public class CooldownActiveException extends RuntimeException {

    public CooldownActiveException(String message) {
        super(message);
    }
}
