package com.example.logic.controllers;

import com.example.logic.dtos.product.ActualizarProductoRequest;
import com.example.logic.dtos.product.AjustarInventarioRequest;
import com.example.logic.dtos.product.CrearProductoRequest;
import com.example.logic.dtos.product.ProductResponse;
import com.example.logic.services.ProductService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("productos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductController {

    @Inject
    private ProductService productService;

    @POST
    public Response crear(@Valid CrearProductoRequest request) {
        ProductResponse creado = productService.crear(request);
        return Response.status(Response.Status.CREATED).entity(creado).build();
    }

    @PUT
    @Path("{id}")
    public ProductResponse actualizar(@PathParam("id") Long id, @Valid ActualizarProductoRequest request) {
        return productService.actualizar(id, request);
    }

    @PUT
    @Path("{id}/inventario")
    public Response ajustarInventario(@PathParam("id") Long id, @Valid AjustarInventarioRequest request) {
        productService.ajustarInventario(id, request);
        return Response.ok(productService.obtener(id)).build();
    }

    @GET
    public List<ProductResponse> listar() {
        return productService.listar();
    }

    @GET
    @Path("{id}")
    public ProductResponse obtener(@PathParam("id") Long id) {
        return productService.obtener(id);
    }

}
