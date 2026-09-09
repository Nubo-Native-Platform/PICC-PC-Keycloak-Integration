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
public class ClientCreationAttributes {

	@JsonProperty("oauth2.device.authorization.grant.enabled")
	private boolean oauth2DeviceAuthGrantEnabled;
	@JsonProperty("oidc.ciba.grant.enabled")
	private boolean oidcCibaGrantEnabled;
	@JsonProperty("use.refresh.tokens")
	private boolean useRefreshToken;
	@JsonProperty("client_credentials.use_refresh_token")
	private boolean clientCredUseFRefreshToken;

}
