package com.example.data.command;

import com.example.data.dto.CrearPedidoRequest;
import com.example.data.entities.Pedido;
import com.example.data.query.PedidoQueryService;
import com.example.data.saga.SagaOrquestador;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("commands/pedidos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PedidoCommandResource {

    @Inject
    private SagaOrquestador sagaOrquestador;

    @Inject
    private PedidoQueryService pedidoQueryService;

    @POST
    public Response crearPedido(@Valid CrearPedidoRequest request) {
        Pedido pedido = sagaOrquestador.ejecutar(request);
        return Response.status(Response.Status.CREATED)
            .entity(pedidoQueryService.obtenerPorId(pedido.getId()))
            .build();
    }
}
