package com.nnp.keycloak.rest.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserAccessVO implements Serializable {
	private String userAccountId;
	private String userId;
	private String userEmail;
	private String contactNo;
	private String password;
	
	@Override
	public boolean equals(Object obj) {
		
		return this.userId.equalsIgnoreCase(((UserAccessVO)obj).userId);
	}
	
	@Override
	public int hashCode() {
		
		return this.userId.hashCode();
	}
	
	
}
