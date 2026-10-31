package com.example.data.repository;

import com.example.data.entities.SagaPedido;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class SagaPedidoRepository {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    public SagaPedido guardar(SagaPedido saga) {
        em.persist(saga);
        em.flush();
        return saga;
    }

    public SagaPedido actualizar(SagaPedido saga) {
        return em.merge(saga);
    }

    public SagaPedido buscarPorPedido(Long pedidoId) {
        try {
            return em.createQuery(
                    "select s from SagaPedido s where s.pedido.id = :pedidoId",
                    SagaPedido.class)
                .setParameter("pedidoId", pedidoId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
