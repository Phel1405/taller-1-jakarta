package com.example.logic.controllers;

import com.example.logic.dtos.pedido.CrearPedidoRequest;
import com.example.logic.dtos.pedido.PedidoResponse;
import com.example.logic.dtos.pedido.SagaResponse;
import com.example.logic.services.PedidoService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("pedidos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PedidoController {

    @Inject
    private PedidoService pedidoService;

    @POST
    public Response crear(@Valid CrearPedidoRequest request) {
        PedidoResponse pedido = pedidoService.crear(request);
        return Response.status(Response.Status.CREATED).entity(pedido).build();
    }

    @GET
    public List<PedidoResponse> listar() {
        return pedidoService.listar();
    }

    @GET
    @Path("{id}")
    public PedidoResponse obtener(@PathParam("id") Long id) {
        return pedidoService.obtener(id);
    }

    @GET
    @Path("{id}/saga")
    public SagaResponse obtenerSaga(@PathParam("id") Long id) {
        return pedidoService.obtenerSaga(id);
    }
}
