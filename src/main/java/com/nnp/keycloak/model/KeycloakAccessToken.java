package com.nnp.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakAccessToken {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_in")
	protected long expiresIn;

	@JsonProperty("refresh_expires_in")
	protected long refreshExpiresIn;

	@JsonProperty("refresh_token")
	protected String refreshToken;

	@JsonProperty("token_type")
	protected String tokenType;

	@JsonProperty("id_token")
	protected String idToken;

	@JsonProperty("not-before-policy")
	protected int notBeforePolicy;

	@JsonProperty("session_state")
	protected String sessionState;

}
