package com.example.data.saga;

import com.example.data.command.InventarioCommandService;
import com.example.data.command.PagoCommandService;
import com.example.data.command.PedidoCommandService;
import com.example.data.dto.CrearPedidoRequest;
import com.example.data.entities.EstadoPago;
import com.example.data.entities.Pago;
import com.example.data.entities.Pedido;
import com.example.data.exception.ConflictoConcurrenciaException;
import com.example.data.exception.StockInsuficienteException;
import com.example.data.repository.PedidoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;

/**
 * Orquestador de la SAGA "crear pedido": CREAR -> RESERVAR -> PAGAR -> CONFIRMAR,
 * con compensaciones si algo falla.
 *
 * A propósito esta clase es un bean CDI SIN @Transactional: así ningún
 * interceptor abre una transacción alrededor del método completo. Cada paso
 * (en los *CommandService) lleva su propio @Transactional(REQUIRES_NEW) y
 * abre y confirma su PROPIA transacción corta. Si la saga completa fuera
 * una sola transacción JTA perderíamos el propósito de la saga: aislar
 * fallos paso a paso y poder compensar lo ya confirmado en vez de depender
 * de un rollback global.
 */
@ApplicationScoped
public class SagaOrquestador {

    private static final int MAX_REINTENTOS_RESERVA = 3;

    @Inject
    private PedidoCommandService pedidoCommandService;

    @Inject
    private InventarioCommandService inventarioCommandService;

    @Inject
    private PagoCommandService pagoCommandService;

    @Inject
    private PedidoRepository pedidoRepository;

    public Pedido ejecutar(CrearPedidoRequest request) {
        // Paso 1: CREAR (transacción local corta nº1)
        Pedido pedido = pedidoCommandService.crearPendiente(request.clienteId(), request.items());
        Long pedidoId = pedido.getId();

        // Paso 2: RESERVAR, con reintentos ante conflictos de bloqueo optimista
        // (dos pedidos compitiendo por el mismo producto al mismo tiempo).
        boolean reservado = false;
        int intentos = 0;
        while (!reservado) {
            intentos++;
            try {
                inventarioCommandService.reservar(pedidoId, request.items());
                reservado = true;
            } catch (StockInsuficienteException e) {
                // No hay nada que compensar (la reserva no llegó a confirmarse):
                // compensamos el paso CREAR cancelando el pedido.
                pedidoCommandService.cancelar(pedidoId, e.getMessage());
                return pedidoRepository.buscarPorId(pedidoId);
            } catch (OptimisticLockException e) {
                if (intentos >= MAX_REINTENTOS_RESERVA) {
                    String error = "No se pudo reservar inventario tras " + intentos
                        + " intentos por conflictos de concurrencia";
                    try {
                        pedidoCommandService.cancelar(pedidoId, error);
                    } catch (RuntimeException compensacionFallida) {
                        pedidoCommandService.marcarSagaFallida(pedidoId, compensacionFallida.getMessage());
                    }
                    throw new ConflictoConcurrenciaException(error);
                }
                // reintenta: la siguiente llamada abre una transacción NUEVA
            }
        }

        // Paso 3: PAGAR (única transacción XA/2PC: MySQL + PostgreSQL)
        Pago pago = pagoCommandService.procesarPago(pedidoId, pedido.getTotal());

        if (pago.getEstado() == EstadoPago.RECHAZADO) {
            // Compensación: liberar inventario reservado y cancelar el pedido.
            compensarPorPagoRechazado(pedidoId);
            return pedidoRepository.buscarPorId(pedidoId);
        }

        // Paso 4: CONFIRMAR (camino feliz)
        try {
            inventarioCommandService.confirmarReservas(pedidoId);
            pedidoCommandService.confirmar(pedidoId);
        } catch (RuntimeException e) {
            pedidoCommandService.marcarSagaFallida(pedidoId, e.getMessage());
            throw e;
        }

        return pedidoRepository.buscarPorId(pedidoId);
    }

    private void compensarPorPagoRechazado(Long pedidoId) {
        try {
            // Idempotente: liberar algo ya liberado no falla.
            inventarioCommandService.liberarReservas(pedidoId);
            pedidoCommandService.cancelar(pedidoId, "Pago rechazado: supera el límite permitido");
        } catch (RuntimeException e) {
            pedidoCommandService.marcarSagaFallida(pedidoId, e.getMessage());
        }
    }
}
