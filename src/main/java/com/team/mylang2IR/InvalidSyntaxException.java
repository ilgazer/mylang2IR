package com.team.mylang2IR;

/**
 * Signals that the syntax at {@code line} is invalid.
 */
public class InvalidSyntaxException extends Exception {
    public final int line;

    public InvalidSyntaxException(int line) {
        this.line = line;
    }
}
