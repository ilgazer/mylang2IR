package com.team.mylang2IR;

public class InvalidSyntaxException extends Exception {
    public final int line;

    public InvalidSyntaxException(int line) {
        this.line = line;
    }
}
