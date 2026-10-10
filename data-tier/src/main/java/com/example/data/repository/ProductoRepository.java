package com.example.data.repository;

import com.example.data.entities.Producto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class ProductoRepository {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    public Producto guardar(Producto producto) {
        em.persist(producto);
        em.flush();
        return producto;
    }

    public Producto actualizar(Producto producto) {
        return em.merge(producto);
    }

    public Producto buscarPorId(Long id) {
        return em.find(Producto.class, id);
    }

    public List<Producto> listarTodos() {
        return em.createQuery("select p from Producto p order by p.id", Producto.class)
            .getResultList();
    }
}
