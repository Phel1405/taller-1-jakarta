package com.example.logic.exception;

/** Representa una respuesta de error ya tipada que devolvió el data-tier (4xx/5xx). */
public class DataTierErrorException extends RuntimeException {

    private final int status;
    private final String codigo;

    public DataTierErrorException(int status, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
