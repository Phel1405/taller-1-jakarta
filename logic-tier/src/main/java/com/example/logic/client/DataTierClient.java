package com.example.logic.client;

import com.example.logic.exception.DataTierErrorException;
import com.example.logic.exception.DataTierNoDisponibleException;
import com.example.logic.web.ErrorResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Cliente JAX-RS hacia el data-tier. Esta clase es la ÚNICA puerta de salida
 * del logic-tier hacia la red: si el data-tier no responde, aquí se traduce
 * a DataTierNoDisponibleException (-> 502 para el cliente final). Si el
 * data-tier respondió con un error de negocio (404/409/400), se propaga
 * tal cual mediante DataTierErrorException.
 */
@ApplicationScoped
public class DataTierClient {

    private static final String URL_POR_DEFECTO = "http://data-tier:8080/jakartaee-data-tier/api";

    private Client client;
    private String baseUrl;

    @PostConstruct
    void iniciar() {
        this.client = ClientBuilder.newBuilder().build();
        String configurada = System.getenv("DATA_TIER_URL");
        this.baseUrl = (configurada == null || configurada.isBlank()) ? URL_POR_DEFECTO : configurada;
    }

    @PreDestroy
    void cerrar() {
        if (client != null) {
            client.close();
        }
    }

    private WebTarget target(String path) {
        return client.target(baseUrl).path(path);
    }

    public <T> T get(String path, Class<T> tipo) {
        try (Response respuesta = target(path).request(MediaType.APPLICATION_JSON).get()) {
            return leer(respuesta, tipo);
        } catch (ProcessingException e) {
            throw noDisponible(e);
        }
    }

    public <T> T get(String path, GenericType<T> tipo) {
        try (Response respuesta = target(path).request(MediaType.APPLICATION_JSON).get()) {
            return leer(respuesta, tipo);
        } catch (ProcessingException e) {
            throw noDisponible(e);
        }
    }

    public <T> T post(String path, Object cuerpo, Class<T> tipo) {
        try (Response respuesta = target(path)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(cuerpo, MediaType.APPLICATION_JSON))) {
            return leer(respuesta, tipo);
        } catch (ProcessingException e) {
            throw noDisponible(e);
        }
    }

    public <T> T put(String path, Object cuerpo, Class<T> tipo) {
        try (Response respuesta = target(path)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(cuerpo, MediaType.APPLICATION_JSON))) {
            return leer(respuesta, tipo);
        } catch (ProcessingException e) {
            throw noDisponible(e);
        }
    }

    private <T> T leer(Response respuesta, Class<T> tipo) {
        if (respuesta.getStatus() >= 200 && respuesta.getStatus() < 300) {
            return tipo == Void.class ? null : respuesta.readEntity(tipo);
        }
        throw errorDeNegocio(respuesta);
    }

    private <T> T leer(Response respuesta, GenericType<T> tipo) {
        if (respuesta.getStatus() >= 200 && respuesta.getStatus() < 300) {
            return respuesta.readEntity(tipo);
        }
        throw errorDeNegocio(respuesta);
    }

    private DataTierErrorException errorDeNegocio(Response respuesta) {
        ErrorResponse error;
        try {
            error = respuesta.readEntity(ErrorResponse.class);
        } catch (Exception e) {
            error = new ErrorResponse("ERROR_DATA_TIER", "El data-tier respondió " + respuesta.getStatus());
        }
        return new DataTierErrorException(respuesta.getStatus(), error.codigo(), error.mensaje());
    }

    private DataTierNoDisponibleException noDisponible(ProcessingException e) {
        return new DataTierNoDisponibleException("El data-tier no respondió: " + e.getMessage(), e);
    }
}
