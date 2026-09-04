package com.nnp.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientCreationRequest {
	
	private String protocol;
	private String clientId;
	private String name;
	private String description;
	private boolean publicClient;
	private boolean authorizationServicesEnabled;
	private boolean serviceAccountsEnabled;
	private boolean implicitFlowEnabled;
	private boolean directAccessGrantsEnabled; //--needs to be set to true for account client for login application
	private boolean frontchannelLogout;
	private boolean alwaysDisplayInConsole;
	private boolean surrogateAuthRequired;
	private boolean enabled;
	private  String clientAuthenticatorType;
	private int notBefore;
	private boolean bearerOnly;
	private boolean consentRequired;
	private boolean standardFlowEnabled;
	private ClientCreationAttributes attributes;
	
	

}
