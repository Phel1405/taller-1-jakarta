package com.example.data.query;

import com.example.data.dto.InventarioDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class InventarioQueryService {

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<InventarioDTO> listarTodos() {
        return em.createQuery(
                "select new com.example.data.dto.InventarioDTO(" +
                    "i.producto.id, i.producto.nombre, i.cantidadDisponible, i.cantidadReservada) " +
                    "from Inventario i order by i.producto.id",
                InventarioDTO.class)
            .getResultList();
    }
}
