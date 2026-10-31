package com.example.data.repository;

import com.example.data.entities.Pedido;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class PedidoRepository {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    public Pedido guardar(Pedido pedido) {
        em.persist(pedido);
        em.flush();
        return pedido;
    }

    public Pedido actualizar(Pedido pedido) {
        return em.merge(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return em.find(Pedido.class, id);
    }

    public List<Pedido> listarTodos() {
        return em.createQuery("select p from Pedido p order by p.id", Pedido.class)
            .getResultList();
    }
}
