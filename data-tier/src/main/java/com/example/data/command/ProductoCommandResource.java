package com.example.data.command;

import com.example.data.dto.InventarioAjusteRequest;
import com.example.data.dto.ProductoRequest;
import com.example.data.dto.ProductoUpdateRequest;
import com.example.data.entities.Inventario;
import com.example.data.entities.Producto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("commands/productos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductoCommandResource {

    @Inject
    private ProductoCommandService productoCommandService;

    @POST
    public Response crear(@Valid ProductoRequest request) {
        Producto producto = productoCommandService.crear(
            request.nombre(), request.descripcion(), request.precio(), request.stockInicial());
        return Response.status(Response.Status.CREATED).entity(toId(producto)).build();
    }

    @PUT
    @Path("{id}")
    public Response actualizar(@PathParam("id") Long id, @Valid ProductoUpdateRequest request) {
        Producto producto = productoCommandService.actualizar(id, request.precio(), request.disponible());
        return Response.ok(toId(producto)).build();
    }

    @PUT
    @Path("{id}/inventario")
    public Response ajustarInventario(@PathParam("id") Long id, @Valid InventarioAjusteRequest request) {
        Inventario inventario = productoCommandService.ajustarInventario(id, request.delta());
        return Response.ok(new IdResponse(inventario.getId())).build();
    }

    private IdResponse toId(Producto producto) {
        return new IdResponse(producto.getId());
    }

    public record IdResponse(Long id) {
    }
}
