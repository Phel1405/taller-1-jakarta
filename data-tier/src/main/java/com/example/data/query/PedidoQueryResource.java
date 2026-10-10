package com.example.data.query;

import com.example.data.dto.PedidoDTO;
import com.example.data.dto.SagaDTO;
import com.example.data.exception.RecursoNoEncontradoException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("queries/pedidos")
@Produces(MediaType.APPLICATION_JSON)
public class PedidoQueryResource {

    @Inject
    private PedidoQueryService pedidoQueryService;

    @Inject
    private SagaQueryService sagaQueryService;

    @GET
    public List<PedidoDTO> listar() {
        return pedidoQueryService.listarTodos();
    }

    @GET
    @Path("{id}")
    public PedidoDTO obtener(@PathParam("id") Long id) {
        PedidoDTO dto = pedidoQueryService.obtenerPorId(id);
        if (dto == null) {
            throw new RecursoNoEncontradoException("Pedido " + id + " no existe");
        }
        return dto;
    }

    @GET
    @Path("{id}/saga")
    public SagaDTO obtenerSaga(@PathParam("id") Long id) {
        SagaDTO dto = sagaQueryService.obtenerPorPedido(id);
        if (dto == null) {
            throw new RecursoNoEncontradoException("No hay saga para el pedido " + id);
        }
        return dto;
    }
}
