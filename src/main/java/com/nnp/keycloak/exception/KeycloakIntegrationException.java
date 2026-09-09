package com.nnp.keycloak.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Custom runtime exception wrapping Keycloak IAM API error responses.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class KeycloakIntegrationException extends RuntimeException {

	private static final long serialVersionUID = -4859132162190230954L;

	private KeycloakExceptionMessage keycloakExceptionMessage;


    public KeycloakIntegrationException(KeycloakExceptionMessage keycloakExceptionMessage) {
		super(keycloakExceptionMessage.getMessage());
		this.keycloakExceptionMessage = keycloakExceptionMessage;
	}

	public KeycloakIntegrationException(KeycloakExceptionMessage keycloakExceptionMessage, Exception ex) {
		super(keycloakExceptionMessage.getMessage(), ex);
		this.keycloakExceptionMessage = keycloakExceptionMessage;
	}


}
