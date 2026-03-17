package com.legal.analysis.infrastructure.exception;

public class PwnedPasswordException extends RuntimeException {

    public PwnedPasswordException(String message) {
        super(message);
    }
}
