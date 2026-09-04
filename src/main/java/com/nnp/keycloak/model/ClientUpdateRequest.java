package com.nnp.keycloak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientUpdateRequest {

	private String protocol;
	private String clientId;
	private String name;
	private String description;
	private Boolean publicClient;
	private Boolean authorizationServicesEnabled;
	private Boolean serviceAccountsEnabled;
	private Boolean implicitFlowEnabled;
	private Boolean directAccessGrantsEnabled;
	private Boolean frontchannelLogout;
	private Boolean alwaysDisplayInConsole;
	private Boolean surrogateAuthRequired;
	private Boolean enabled;
	private String clientAuthenticatorType;
	private int notBefore;
	private Boolean bearerOnly;
	private Boolean consentRequired;
	private Boolean standardFlowEnabled;
	private ClientCreationAttributes attributes;
}
