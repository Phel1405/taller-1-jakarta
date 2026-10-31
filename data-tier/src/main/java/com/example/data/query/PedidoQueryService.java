package com.example.data.query;

import com.example.data.dto.DetallePedidoDTO;
import com.example.data.dto.PedidoDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PedidoQueryService {

    private static final String PROYECCION_CABECERA =
        "select new com.example.data.dto.PedidoDTO(" +
            "p.id, p.clienteId, cast(p.estado as string), p.total, p.motivoCancelacion, " +
            "p.fechaCreacion, p.fechaActualizacion, null) " +
        "from Pedido p ";

    @PersistenceContext(unitName = "pedidosPU")
    private EntityManager em;

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<PedidoDTO> listarTodos() {
        return em.createQuery(PROYECCION_CABECERA + "order by p.id", PedidoDTO.class).getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public PedidoDTO obtenerPorId(Long id) {
        PedidoDTO cabecera;
        try {
            cabecera = em.createQuery(PROYECCION_CABECERA + "where p.id = :id", PedidoDTO.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

        List<DetallePedidoDTO> detalles = em.createQuery(
                "select new com.example.data.dto.DetallePedidoDTO(" +
                    "d.producto.id, d.producto.nombre, d.cantidad, d.precioUnitario) " +
                    "from DetallePedido d where d.pedido.id = :id",
                DetallePedidoDTO.class)
            .setParameter("id", id)
            .getResultList();

        return new PedidoDTO(cabecera.id(), cabecera.clienteId(), cabecera.estado(), cabecera.total(),
            cabecera.motivoCancelacion(), cabecera.fechaCreacion(), cabecera.fechaActualizacion(), detalles);
    }
}
