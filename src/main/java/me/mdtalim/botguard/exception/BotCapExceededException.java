package me.mdtalim.botguard.exception;

public class BotCapExceededException extends RuntimeException {

    public BotCapExceededException(String msg) {
        super(msg);
    }
}
