package com.charginghive.station.customException;

public class OwnerIdMissMatchException extends RuntimeException {
    public OwnerIdMissMatchException(String message){
        super(message);
    }
}
