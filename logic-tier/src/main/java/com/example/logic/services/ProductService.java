package com.example.logic.services;

import com.example.logic.client.DataTierClient;
import com.example.logic.dtos.product.ActualizarProductoRequest;
import com.example.logic.dtos.product.AjustarInventarioRequest;
import com.example.logic.dtos.product.CrearProductoRequest;
import com.example.logic.dtos.product.IdResponse;
import com.example.logic.dtos.product.InventarioResponse;
import com.example.logic.dtos.product.ProductResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.util.List;

/**
 * Fachada de productos: valida (Bean Validation en el recurso) y delega
 * en el data-tier. El logic-tier no tiene base de datos propia.
 */
@ApplicationScoped
public class ProductService {

    @Inject
    private DataTierClient dataTierClient;

    public ProductResponse crear(CrearProductoRequest request) {
        IdResponse creado = dataTierClient.post("commands/productos", request, IdResponse.class);
        return obtener(creado.id());
    }

    public ProductResponse actualizar(Long id, ActualizarProductoRequest request) {
        dataTierClient.put("commands/productos/" + id, request, IdResponse.class);
        return obtener(id);
    }

    public void ajustarInventario(Long id, AjustarInventarioRequest request) {
        dataTierClient.put("commands/productos/" + id + "/inventario", request, IdResponse.class);
    }

    public List<ProductResponse> listar() {
        return dataTierClient.get("queries/productos", new GenericType<List<ProductResponse>>() {
        });
    }

    public ProductResponse obtener(Long id) {
        return dataTierClient.get("queries/productos/" + id, ProductResponse.class);
    }

    public List<InventarioResponse> listarInventario() {
        return dataTierClient.get("queries/inventario", new GenericType<List<InventarioResponse>>() {
        });
    }
}
