package com.nnp.keycloak.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetails {
	
	private String id;
	private String clientId;
	private String name;
	private String rootUrl;
	private String baseUrl;
	private boolean enabled;
	private boolean alwaysDisplayInConsole;
	private String clientAuthenticatorType;
	
	private List<String> redirectUris;
	private List<String> webOrigins;
	private List<String> defaultClientScopes;
	private List<String> optionalClientScopes;
	
	
	private Integer notBefore;
	private boolean bearerOnly;
	
	private boolean consentRequired;
	private boolean standardFlowEnabled;
	private boolean implicitFlowEnabled;
	private boolean directAccessGrantsEnabled;
	private boolean serviceAccountsEnabled;
	private boolean fullScopeAllowed;
	
	private boolean frontchannelLogout;
	private String protocol;
	private ClientAccess access;
	
	

}
