package com.restaurante.ms_facturacion.service;

import com.restaurante.ms_facturacion.dto.FacturaRequestDTO;
import com.restaurante.ms_facturacion.dto.FacturaResponseDTO;
import com.restaurante.ms_facturacion.entity.Factura;
import com.restaurante.ms_facturacion.exception.ResourceNotFoundException;
import com.restaurante.ms_facturacion.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    
    // Tasa impositiva simulada (Ej: IVA del 19% o similar según región)
    private static final double PORCENTAJE_IMPUESTO = 0.19; 

    @Override
    public FacturaResponseDTO emitirFactura(FacturaRequestDTO request) {
        log.info("Iniciando emisión de factura legal para el pedido ID: {}", request.getPedidoId());
        
        // Cálculos matemáticos y fiscales automáticos aislados en la lógica de negocio
        double subtotal = request.getSubtotal();
        double impuestos = subtotal * PORCENTAJE_IMPUESTO;
        double total = subtotal + impuestos;
        
        // Generación de un número de folio fiscal aleatorio único para propósitos de simulación inicial
        String folioFiscal = "FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Factura factura = Factura.builder()
                .pedidoId(request.getPedidoId())
                .numeroFactura(folioFiscal)
                .subtotal(subtotal)
                .impuestos(impuestos)
                .total(total)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/" + folioFiscal + ".pdf") // Ruta simulada de almacenamiento
                .build();
        
        Factura guardada = facturaRepository.save(factura);
        log.info("Factura emitida y registrada ante el ente fiscal con ID: y Folio: {}", guardada.getId(), folioFiscal);
        return mapToDTO(guardada);
    }

    @Override
    public List<FacturaResponseDTO> obtenerTodas() {
        log.info("Consultando registros de todas las facturas");
        return facturaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FacturaResponseDTO obtenerPorId(Long id) {
        log.info("Consultando factura por ID: {}", id);
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));
        return mapToDTO(factura);
    }

    @Override
    public FacturaResponseDTO obtenerPorPedidoId(Long pedidoId) {
        log.info("Buscando documento fiscal del pedido ID: {}", pedidoId);
        Factura factura = facturaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un documento fiscal asociado al pedido ID: " + pedidoId));
        return mapToDTO(factura);
    }

    @Override
    public FacturaResponseDTO actualizarFactura(Long id, FacturaRequestDTO request) {
        log.info("Modificando valores base de la factura ID: {}", id);
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));
        
        // Recalculamos dinámicamente si el subtotal cambia por alguna nota de crédito o ajuste manual
        double subtotal = request.getSubtotal();
        double impuestos = subtotal * PORCENTAJE_IMPUESTO;
        
        factura.setPedidoId(request.getPedidoId());
        factura.setSubtotal(subtotal);
        factura.setImpuestos(impuestos);
        factura.setTotal(subtotal + impuestos);
        
        Factura actualizada = facturaRepository.save(factura);
        log.info("Valores financieros de la factura ID: {} actualizados", id);
        return mapToDTO(actualizada);
    }

    @Override
    public FacturaResponseDTO cambiarEstadoFiscal(Long id, String nuevoEstado) {
        log.info("Cambiando estado tributario de la factura ID: {} a {}", id, nuevoEstado);
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));
        
        factura.setEstadoFiscal(nuevoEstado);
        Factura actualizada = facturaRepository.save(factura);
        return mapToDTO(actualizada);
    }

    @Override
    public void eliminarFactura(Long id) {
        log.info("Removiendo registro de factura ID: {}", id);
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));
        facturaRepository.delete(factura);
        log.info("Registro eliminado de la base de datos local");
    }

    private FacturaResponseDTO mapToDTO(Factura factura) {
        return FacturaResponseDTO.builder()
                .id(factura.getId())
                .pedidoId(factura.getPedidoId())
                .numeroFactura(factura.getNumeroFactura())
                .subtotal(factura.getSubtotal())
                .impuestos(factura.getImpuestos())
                .total(factura.getTotal())
                .fechaEmision(factura.getFechaEmision())
                .estadoFiscal(factura.getEstadoFiscal())
                .urlPdf(factura.getUrlPdf())
                .build();
    }
}
