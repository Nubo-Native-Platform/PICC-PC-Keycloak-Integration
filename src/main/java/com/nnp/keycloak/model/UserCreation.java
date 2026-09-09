package com.nnp.keycloak.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreation {
	
	private String username;
	private String email;
	private String firstName;
	private String lastName;
	//private String emailVerified;
	private boolean emailVerified;
	private List<String> requiredActions;
	private List<String> groups;
	private boolean enabled;
	private List<UserCredential> credentials;
	private UserAttributes attributes;
	private UserAccess access;
	
}
