package com.example.data.exception;

public class ConflictoConcurrenciaException extends RuntimeException {
    public ConflictoConcurrenciaException(String mensaje) {
        super(mensaje);
    }
}
