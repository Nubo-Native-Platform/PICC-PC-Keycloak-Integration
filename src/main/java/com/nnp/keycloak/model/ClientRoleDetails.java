package com.nnp.keycloak.model;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClientRoleDetails {

	private UUID id;
	private String name;
	private String description;
	private boolean composite;
	private boolean clientRole;
	private UUID containerId;

}
