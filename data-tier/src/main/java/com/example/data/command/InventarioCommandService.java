package com.example.data.command;

import com.example.data.dto.ItemPedidoRequest;
import com.example.data.entities.EstadoReserva;
import com.example.data.entities.Inventario;
import com.example.data.entities.PasoSaga;
import com.example.data.entities.Pedido;
import com.example.data.entities.Producto;
import com.example.data.entities.ReservaInventario;
import com.example.data.entities.SagaPedido;
import com.example.data.exception.RecursoNoEncontradoException;
import com.example.data.exception.StockInsuficienteException;
import com.example.data.repository.InventarioRepository;
import com.example.data.repository.PedidoRepository;
import com.example.data.repository.ProductoRepository;
import com.example.data.repository.ReservaInventarioRepository;
import com.example.data.repository.SagaPedidoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Paso 2 de la saga: reservar inventario. Usa bloqueo optimista
 * (columna @Version en Inventario) para que dos pedidos concurrentes
 * nunca sobre-reserven el mismo stock sin darse cuenta.
 */
@ApplicationScoped
public class InventarioCommandService {

    @Inject
    private InventarioRepository inventarioRepository;

    @Inject
    private ReservaInventarioRepository reservaRepository;

    @Inject
    private PedidoRepository pedidoRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private SagaPedidoRepository sagaPedidoRepository;

    /**
     * Intenta reservar, en una única transacción local, el stock de todos
     * los items del pedido. Si el stock no alcanza, lanza
     * StockInsuficienteException y la transacción se revierte completa
     * (ninguna reserva parcial queda guardada).
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void reservar(Long pedidoId, List<ItemPedidoRequest> items) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId);
        if (pedido == null) {
            throw new RecursoNoEncontradoException("Pedido " + pedidoId + " no existe");
        }

        for (ItemPedidoRequest item : items) {
            Inventario inventario = inventarioRepository.buscarPorProducto(item.productoId());
            if (inventario == null) {
                throw new RecursoNoEncontradoException("No hay inventario para el producto " + item.productoId());
            }
            int disponibleReal = inventario.getCantidadDisponible() - inventario.getCantidadReservada();
            if (disponibleReal < item.cantidad()) {
                throw new StockInsuficienteException(
                    "Stock insuficiente para el producto " + item.productoId()
                        + ": disponible=" + disponibleReal + ", solicitado=" + item.cantidad());
            }
            inventario.setCantidadReservada(inventario.getCantidadReservada() + item.cantidad());
            inventarioRepository.flush();

            Producto producto = productoRepository.buscarPorId(item.productoId());
            ReservaInventario reserva = new ReservaInventario();
            reserva.setPedido(pedido);
            reserva.setProducto(producto);
            reserva.setCantidad(item.cantidad());
            reserva.setEstado(EstadoReserva.RESERVADA);
            reservaRepository.guardar(reserva);
        }

        pedido.setEstado(com.example.data.entities.EstadoPedido.INVENTARIO_RESERVADO);

        SagaPedido saga = sagaPedidoRepository.buscarPorPedido(pedidoId);
        saga.setPasoActual(PasoSaga.RESERVAR);
    }

    /** Paso final feliz: pasa las reservas de RESERVADA a CONFIRMADA y descuenta el stock físico. */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void confirmarReservas(Long pedidoId) {
        for (ReservaInventario reserva : reservaRepository.listarPorPedido(pedidoId)) {
            if (reserva.getEstado() != EstadoReserva.RESERVADA) {
                continue;
            }
            Inventario inventario = inventarioRepository.buscarPorProducto(reserva.getProducto().getId());
            inventario.setCantidadDisponible(inventario.getCantidadDisponible() - reserva.getCantidad());
            inventario.setCantidadReservada(inventario.getCantidadReservada() - reserva.getCantidad());
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }
    }

    /**
     * Compensación: libera toda reserva que siga en estado RESERVADA.
     * Idempotente -- si ya está LIBERADA o CONFIRMADA no la toca, por lo que
     * llamarla dos veces sobre el mismo pedido no produce doble liberación.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void liberarReservas(Long pedidoId) {
        for (ReservaInventario reserva : reservaRepository.listarPorPedido(pedidoId)) {
            if (reserva.getEstado() != EstadoReserva.RESERVADA) {
                continue;
            }
            Inventario inventario = inventarioRepository.buscarPorProducto(reserva.getProducto().getId());
            inventario.setCantidadReservada(inventario.getCantidadReservada() - reserva.getCantidad());
            reserva.setEstado(EstadoReserva.LIBERADA);
        }
    }
}
