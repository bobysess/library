package com.bobysess.library.author;

import lombok.Getter;

@Getter
public class InvalidAuthorException extends RuntimeException {

    private final Reason reason;

    public InvalidAuthorException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public enum Reason {
        MISSING_FIRST_NAME,
        MISSING_LAST_NAME,
        INVALID_BIRTH_DATE,
        INVALID_DEATH_DATE,
        DEATH_BEFORE_BIRTH
    }
}
