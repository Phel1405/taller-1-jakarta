package com.example.data.repository;

import com.example.data.entities.Inventario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class InventarioRepository {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    public Inventario guardar(Inventario inventario) {
        em.persist(inventario);
        em.flush();
        return inventario;
    }

    public Inventario buscarPorProducto(Long productoId) {
        try {
            return em.createQuery(
                    "select i from Inventario i where i.producto.id = :productoId",
                    Inventario.class)
                .setParameter("productoId", productoId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /** Lectura con bloqueo optimista explícito, para el paso de reserva de la saga. */
    public Inventario buscarPorProductoConBloqueoOptimista(Long productoId) {
        Inventario inventario = buscarPorProducto(productoId);
        if (inventario != null) {
            em.lock(inventario, LockModeType.OPTIMISTIC);
        }
        return inventario;
    }

    /**
     * Fuerza el flush para que, si hay conflicto de @Version, la
     * OptimisticLockException se lance de forma síncrona dentro del
     * método de negocio (y no durante el commit del contenedor), de modo
     * que el llamador la pueda capturar y decidir si reintenta.
     */
    public void flush() {
        em.flush();
    }

    public List<Inventario> listarTodos() {
        return em.createQuery("select i from Inventario i order by i.id", Inventario.class)
            .getResultList();
    }
}
