package com.example.data.repository;

import com.example.data.entities.Pago;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class PagoRepository {

    /** Unidad de persistencia distinta (MySQL): no hay join posible con pedidosPU. */
    @PersistenceContext(unitName = "pagosPU")
    private EntityManager em;

    public Pago guardar(Pago pago) {
        em.persist(pago);
        em.flush();
        return pago;
    }
}
