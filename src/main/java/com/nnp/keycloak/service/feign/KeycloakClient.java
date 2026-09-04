package com.nnp.keycloak.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nnp.keycloak.model.AuthTokenRequest;
import com.nnp.keycloak.model.KeycloakAccessToken;

import feign.Headers;

/**
 * @author ebasusa
 *
 */
@FeignClient(name = "keycloakClient", primary = false, url = "${keycloak.url}")
public interface KeycloakClient {

	@PostMapping(path = "/realms/{realmName}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@Headers("Content-Type: application/x-www-form-urlencoded")
	public ResponseEntity<KeycloakAccessToken> getAccessKey(@RequestBody AuthTokenRequest authTokenRequest,
			@PathVariable("realmName") String realmName);

}
