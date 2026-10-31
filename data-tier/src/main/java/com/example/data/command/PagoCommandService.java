package com.example.data.command;

import com.example.data.entities.EstadoPago;
import com.example.data.entities.EstadoPedido;
import com.example.data.entities.Pago;
import com.example.data.entities.PasoSaga;
import com.example.data.entities.Pedido;
import com.example.data.entities.SagaPedido;
import com.example.data.repository.PagoRepository;
import com.example.data.repository.PedidoRepository;
import com.example.data.repository.SagaPedidoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

/**
 * Paso 3 de la saga: procesar el pago. Es el ÚNICO paso que abre una
 * transacción XA distribuida de verdad: en el mismo método se escribe
 * en MySQL (tabla pago, vía pagosPU) y en PostgreSQL (pedido y
 * saga_pedido, vía pedidosPU). Al tocar dos EntityManagers JTA con
 * datasources XA distintos dentro de la MISMA transacción, el
 * TransactionManager de WildFly enlista ambos recursos y hace
 * two-phase commit (2PC): o se confirman ambas bases, o ninguna.
 *
 * El resto de pasos de la saga son transacciones locales cortas sobre
 * una sola base -- la saga completa NUNCA es una sola transacción.
 */
@ApplicationScoped
public class PagoCommandService {

    /** Valor por defecto si la variable de entorno PAGO_LIMITE no está definida. */
    private static final BigDecimal LIMITE_POR_DEFECTO = new BigDecimal("1000000");

    @Inject
    private PagoRepository pagoRepository;

    @Inject
    private PedidoRepository pedidoRepository;

    @Inject
    private SagaPedidoRepository sagaPedidoRepository;

    public BigDecimal obtenerLimitePago() {
        String valor = System.getenv("PAGO_LIMITE");
        if (valor == null || valor.isBlank()) {
            return LIMITE_POR_DEFECTO;
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            return LIMITE_POR_DEFECTO;
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Pago procesarPago(Long pedidoId, BigDecimal total) {
        boolean aprobado = total.compareTo(obtenerLimitePago()) <= 0;

        Pago pago = new Pago();
        pago.setPedidoId(pedidoId);
        pago.setMonto(total);
        pago.setEstado(aprobado ? EstadoPago.APROBADO : EstadoPago.RECHAZADO);
        pagoRepository.guardar(pago);

        SagaPedido saga = sagaPedidoRepository.buscarPorPedido(pedidoId);
        saga.setPasoActual(PasoSaga.PAGAR);

        if (aprobado) {
            Pedido pedido = pedidoRepository.buscarPorId(pedidoId);
            pedido.setEstado(EstadoPedido.PAGO_APROBADO);
        }

        return pago;
    }
}
