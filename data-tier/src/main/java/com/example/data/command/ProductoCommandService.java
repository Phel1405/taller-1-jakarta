package com.example.data.command;

import com.example.data.entities.Inventario;
import com.example.data.entities.Producto;
import com.example.data.exception.RecursoNoEncontradoException;
import com.example.data.repository.InventarioRepository;
import com.example.data.repository.ProductoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

/**
 * Lado "comando" de CQRS: opera sobre entidades y confirma cambios de estado.
 * Cada método es una transacción local corta sobre pedidosPU (PostgreSQL).
 */
@ApplicationScoped
public class ProductoCommandService {

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private InventarioRepository inventarioRepository;

    @Transactional(Transactional.TxType.REQUIRED)
    public Producto crear(String nombre, String descripcion, BigDecimal precio, int stockInicial) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setDisponible(true);
        productoRepository.guardar(producto);

        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidadDisponible(stockInicial);
        inventario.setCantidadReservada(0);
        inventarioRepository.guardar(inventario);

        return producto;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Producto actualizar(Long id, BigDecimal precio, boolean disponible) {
        Producto producto = productoRepository.buscarPorId(id);
        if (producto == null) {
            throw new RecursoNoEncontradoException("Producto " + id + " no existe");
        }
        producto.setPrecio(precio);
        producto.setDisponible(disponible);
        return productoRepository.actualizar(producto);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Inventario ajustarInventario(Long productoId, int delta) {
        Inventario inventario = inventarioRepository.buscarPorProducto(productoId);
        if (inventario == null) {
            throw new RecursoNoEncontradoException("No hay inventario para el producto " + productoId);
        }
        int nuevaCantidad = inventario.getCantidadDisponible() + delta;
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("El ajuste dejaría el inventario en negativo");
        }
        inventario.setCantidadDisponible(nuevaCantidad);
        return inventario;
    }
}
