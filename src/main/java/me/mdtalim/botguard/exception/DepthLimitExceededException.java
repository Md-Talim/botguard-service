package me.mdtalim.botguard.exception;

public class DepthLimitExceededException extends RuntimeException {

    public DepthLimitExceededException(String msg) {
        super(msg);
    }
}
