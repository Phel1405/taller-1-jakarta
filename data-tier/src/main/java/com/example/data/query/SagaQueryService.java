package com.example.data.query;

import com.example.data.dto.SagaDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SagaQueryService {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    @Transactional(Transactional.TxType.SUPPORTS)
    public SagaDTO obtenerPorPedido(Long pedidoId) {
        try {
            return em.createQuery(
                    "select new com.example.data.dto.SagaDTO(" +
                        "s.id, s.pedido.id, cast(s.pasoActual as string), cast(s.estado as string), " +
                        "s.error, s.fechaInicio, s.fechaFin) " +
                        "from SagaPedido s where s.pedido.id = :pedidoId",
                    SagaDTO.class)
                .setParameter("pedidoId", pedidoId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
