package com.nnp.keycloak.exception;

/**
 * @author AC
 *
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakExceptionMessage {
	private String code;
	private String message;
}
