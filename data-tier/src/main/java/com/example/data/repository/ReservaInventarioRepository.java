package com.example.data.repository;

import com.example.data.entities.ReservaInventario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class ReservaInventarioRepository {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    public ReservaInventario guardar(ReservaInventario reserva) {
        em.persist(reserva);
        em.flush();
        return reserva;
    }

    public ReservaInventario actualizar(ReservaInventario reserva) {
        return em.merge(reserva);
    }

    public List<ReservaInventario> listarPorPedido(Long pedidoId) {
        return em.createQuery(
                "select r from ReservaInventario r where r.pedido.id = :pedidoId",
                ReservaInventario.class)
            .setParameter("pedidoId", pedidoId)
            .getResultList();
    }
}
