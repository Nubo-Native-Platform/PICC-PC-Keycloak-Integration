package com.nnp.keycloak.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nnp.keycloak.model.AuthTokenRequest;
import com.nnp.keycloak.model.KeycloakAccessToken;

import feign.Headers;

/**
 * @author AC
 *
 */
@FeignClient(name = "keycloakClientCaller", url = "http://localhost:8082/keycloak")
public interface KeycloakClientCaller {
	@PostMapping(path = "/realms/master/protocol/openid-connect/token",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@Headers("Content-Type: application/x-www-form-urlencoded")
	public ResponseEntity<KeycloakAccessToken> getAccessKey(@RequestBody AuthTokenRequest authTokenRequest);
}
