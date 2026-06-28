package com.restaurante.ms_notificaciones.repository;

import com.restaurante.ms_notificaciones.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    // Permite al cliente o al sistema ver el historial de mensajes enviados a una persona
    List<Notificacion> findByClienteId(Long clienteId);
}

