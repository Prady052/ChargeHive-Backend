package com.charginghive.station.customException;

// simple runtime exception to represent 404 resources not found
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
