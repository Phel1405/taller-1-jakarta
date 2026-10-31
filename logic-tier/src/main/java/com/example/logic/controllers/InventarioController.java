package com.example.logic.controllers;

import com.example.logic.dtos.product.InventarioResponse;
import com.example.logic.services.ProductService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("inventario")
@Produces(MediaType.APPLICATION_JSON)
public class InventarioController {

    @Inject
    private ProductService productService;

    @GET
    public List<InventarioResponse> listar() {
        return productService.listarInventario();
    }
}
