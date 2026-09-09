package com.nnp.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccess {

	private boolean manageGroupMembership;
	private boolean view;
	private boolean mapRoles;
	private boolean impersonate;
	private boolean manage;

}
