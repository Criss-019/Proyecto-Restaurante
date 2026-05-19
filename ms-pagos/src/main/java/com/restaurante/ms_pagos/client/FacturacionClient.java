package com.restaurante.ms_pagos.client;

import com.restaurante.ms_pagos.client.dto.FacturaRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-facturacion", path = "/api/facturacion")
public interface FacturacionClient {

    @PostMapping
    void emitirFactura(@RequestBody FacturaRequestDTO request);
}
