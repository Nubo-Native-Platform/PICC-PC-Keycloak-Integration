package com.nnp.keycloak.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RealmRoleDetails {
	
	private UUID id;
	private String name;
	private String description;
	private boolean composite;
	private boolean clientRole;
	private UUID containerId;

}
