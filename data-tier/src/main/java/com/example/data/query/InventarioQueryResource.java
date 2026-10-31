package com.example.data.query;

import com.example.data.dto.InventarioDTO;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("queries/inventario")
@Produces(MediaType.APPLICATION_JSON)
public class InventarioQueryResource {

    @Inject
    private InventarioQueryService inventarioQueryService;

    @GET
    public List<InventarioDTO> listar() {
        return inventarioQueryService.listarTodos();
    }
}
