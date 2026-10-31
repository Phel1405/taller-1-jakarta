package com.example.data.query;

import com.example.data.dto.ProductoDTO;
import com.example.data.exception.RecursoNoEncontradoException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("queries/productos")
@Produces(MediaType.APPLICATION_JSON)
public class ProductoQueryResource {

    @Inject
    private ProductoQueryService productoQueryService;

    @GET
    public List<ProductoDTO> listar() {
        return productoQueryService.listarTodos();
    }

    @GET
    @Path("{id}")
    public ProductoDTO obtener(@PathParam("id") Long id) {
        ProductoDTO dto = productoQueryService.obtenerPorId(id);
        if (dto == null) {
            throw new RecursoNoEncontradoException("Producto " + id + " no existe");
        }
        return dto;
    }
}
