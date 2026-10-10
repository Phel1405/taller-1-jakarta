package com.example.data.query;

import com.example.data.dto.ProductoDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Lado "consulta" de CQRS: nunca devuelve entidades, siempre proyecciones
 * JPQL "SELECT NEW ..." a records inmutables. TxType.SUPPORTS: si ya hay
 * una transacción la usa (lectura dentro de ella), pero nunca abre una
 * transacción de escritura propia.
 */
@ApplicationScoped
public class ProductoQueryService {

    private static final String PROYECCION =
        "select new com.example.data.dto.ProductoDTO(" +
            "p.id, p.nombre, p.descripcion, p.precio, p.disponible, " +
            "coalesce(i.cantidadDisponible, 0), coalesce(i.cantidadReservada, 0), " +
            "p.fechaCreacion, p.fechaActualizacion) " +
        "from Producto p left join Inventario i on i.producto.id = p.id ";

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ProductoDTO> listarTodos() {
        return em.createQuery(PROYECCION + "order by p.id", ProductoDTO.class).getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public ProductoDTO obtenerPorId(Long id) {
        try {
            return em.createQuery(PROYECCION + "where p.id = :id", ProductoDTO.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
