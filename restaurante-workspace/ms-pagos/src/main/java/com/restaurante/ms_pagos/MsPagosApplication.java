package com.restaurante.ms_pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // Habilita la comunicación hacia otros microservicios
public class MsPagosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsPagosApplication.class, args);
	}

}
