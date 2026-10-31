package com.example.data.command;

import com.example.data.dto.ItemPedidoRequest;
import com.example.data.entities.DetallePedido;
import com.example.data.entities.EstadoPedido;
import com.example.data.entities.EstadoSaga;
import com.example.data.entities.PasoSaga;
import com.example.data.entities.Pedido;
import com.example.data.entities.Producto;
import com.example.data.entities.SagaPedido;
import com.example.data.exception.RecursoNoEncontradoException;
import com.example.data.repository.PedidoRepository;
import com.example.data.repository.ProductoRepository;
import com.example.data.repository.SagaPedidoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pasos de la saga sobre el agregado Pedido. Cada método es su propia
 * transacción LOCAL y corta (REQUIRES_NEW): crea/actualiza el Pedido y,
 * en la misma transacción, el registro de seguimiento saga_pedido.
 * El SagaOrquestador (que no es transaccional) encadena estas llamadas.
 */
@ApplicationScoped
public class PedidoCommandService {

    @Inject
    private PedidoRepository pedidoRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private SagaPedidoRepository sagaPedidoRepository;

    /** Paso 1: crear el pedido en estado PENDIENTE e iniciar su saga. */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Pedido crearPendiente(String clienteId, List<ItemPedidoRequest> items) {
        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest item : items) {
            Producto producto = productoRepository.buscarPorId(item.productoId());
            if (producto == null) {
                throw new RecursoNoEncontradoException("Producto " + item.productoId() + " no existe");
            }
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            pedido.agregarDetalle(detalle);
            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
        }
        pedido.setTotal(total);
        pedidoRepository.guardar(pedido);

        SagaPedido saga = new SagaPedido();
        saga.setPedido(pedido);
        saga.setPasoActual(PasoSaga.CREAR);
        saga.setEstado(EstadoSaga.EN_CURSO);
        sagaPedidoRepository.guardar(saga);

        return pedido;
    }

    /** Paso final feliz: confirmar el pedido y cerrar la saga como COMPLETADA. */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void confirmar(Long pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId);
        pedido.setEstado(EstadoPedido.CONFIRMADO);

        SagaPedido saga = sagaPedidoRepository.buscarPorPedido(pedidoId);
        saga.setPasoActual(PasoSaga.CONFIRMAR);
        saga.setEstado(EstadoSaga.COMPLETADA);
        saga.setFechaFin(java.time.LocalDateTime.now());
    }

    /**
     * Compensación: cancela el pedido con un motivo y marca la saga como
     * COMPENSADA. Idempotente: si el pedido ya está CANCELADO no hace nada.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void cancelar(Long pedidoId, String motivo) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId);
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return;
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setMotivoCancelacion(motivo);

        SagaPedido saga = sagaPedidoRepository.buscarPorPedido(pedidoId);
        saga.setPasoActual(PasoSaga.COMPENSAR);
        saga.setEstado(EstadoSaga.COMPENSADA);
        saga.setFechaFin(java.time.LocalDateTime.now());
    }

    /** Marca la saga como FALLIDA cuando una compensación no pudo completarse. */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void marcarSagaFallida(Long pedidoId, String error) {
        SagaPedido saga = sagaPedidoRepository.buscarPorPedido(pedidoId);
        saga.setEstado(EstadoSaga.FALLIDA);
        saga.setError(error);
        saga.setFechaFin(java.time.LocalDateTime.now());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void actualizarPaso(Long pedidoId, PasoSaga paso) {
        SagaPedido saga = sagaPedidoRepository.buscarPorPedido(pedidoId);
        saga.setPasoActual(paso);
    }
}
