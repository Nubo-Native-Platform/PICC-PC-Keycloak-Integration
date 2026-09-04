package com.nnp.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author AC
 *
 */
@SpringBootApplication
@EnableFeignClients
public class KeycloakIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloakIntegrationApplication.class, args);
	}

}
