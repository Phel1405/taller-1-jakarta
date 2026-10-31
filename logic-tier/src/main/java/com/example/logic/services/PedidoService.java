package com.example.logic.services;

import com.example.logic.client.DataTierClient;
import com.example.logic.dtos.pedido.CrearPedidoRequest;
import com.example.logic.dtos.pedido.PedidoResponse;
import com.example.logic.dtos.pedido.SagaResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.util.List;

@ApplicationScoped
public class PedidoService {

    @Inject
    private DataTierClient dataTierClient;

    public PedidoResponse crear(CrearPedidoRequest request) {
        return dataTierClient.post("commands/pedidos", request, PedidoResponse.class);
    }

    public List<PedidoResponse> listar() {
        return dataTierClient.get("queries/pedidos", new GenericType<List<PedidoResponse>>() {
        });
    }

    public PedidoResponse obtener(Long id) {
        return dataTierClient.get("queries/pedidos/" + id, PedidoResponse.class);
    }

    public SagaResponse obtenerSaga(Long id) {
        return dataTierClient.get("queries/pedidos/" + id + "/saga", SagaResponse.class);
    }
}
