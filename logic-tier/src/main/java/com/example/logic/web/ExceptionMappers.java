package com.example.logic.web;

import com.example.logic.exception.DataTierErrorException;
import com.example.logic.exception.DataTierNoDisponibleException;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

public class ExceptionMappers {

    /** Traduce 1:1 el código/mensaje y estado HTTP que ya decidió el data-tier (404, 409, etc). */
    @Provider
    public static class DataTierErrorMapper implements ExceptionMapper<DataTierErrorException> {
        @Override
        public Response toResponse(DataTierErrorException e) {
            return Response.status(e.getStatus())
                .entity(new ErrorResponse(e.getCodigo(), e.getMessage()))
                .build();
        }
    }

    /** El data-tier no respondió: se traduce como 502 Bad Gateway. */
    @Provider
    public static class DataTierNoDisponibleMapper implements ExceptionMapper<DataTierNoDisponibleException> {
        @Override
        public Response toResponse(DataTierNoDisponibleException e) {
            return Response.status(Response.Status.BAD_GATEWAY)
                .entity(new ErrorResponse("DATA_TIER_NO_DISPONIBLE", e.getMessage()))
                .build();
        }
    }

    @Provider
    public static class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("VALIDACION", e.getMessage()))
                .build();
        }
    }

    @Provider
    public static class GenericoMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("ERROR_INTERNO", e.getMessage()))
                .build();
        }
    }
}
