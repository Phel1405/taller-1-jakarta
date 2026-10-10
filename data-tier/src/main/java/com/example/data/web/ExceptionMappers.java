package com.example.data.web;

import com.example.data.exception.ConflictoConcurrenciaException;
import com.example.data.exception.RecursoNoEncontradoException;
import com.example.data.exception.StockInsuficienteException;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

public class ExceptionMappers {

    @Provider
    public static class RecursoNoEncontradoMapper implements ExceptionMapper<RecursoNoEncontradoException> {
        @Override
        public Response toResponse(RecursoNoEncontradoException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NO_ENCONTRADO", e.getMessage()))
                .build();
        }
    }

    @Provider
    public static class StockInsuficienteMapper implements ExceptionMapper<StockInsuficienteException> {
        @Override
        public Response toResponse(StockInsuficienteException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("STOCK_INSUFICIENTE", e.getMessage()))
                .build();
        }
    }

    @Provider
    public static class ConflictoConcurrenciaMapper implements ExceptionMapper<ConflictoConcurrenciaException> {
        @Override
        public Response toResponse(ConflictoConcurrenciaException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("CONFLICTO_CONCURRENCIA", e.getMessage()))
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
