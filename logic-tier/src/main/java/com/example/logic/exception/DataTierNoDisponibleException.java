package com.example.logic.exception;

/** El data-tier no respondió (timeout, conexión rechazada, etc). */
public class DataTierNoDisponibleException extends RuntimeException {
    public DataTierNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
