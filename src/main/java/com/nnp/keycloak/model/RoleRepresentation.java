/**
 * RoleRepresentation.java
 *
 * @author Arnab Chatterjee
 * @date 16-Jun-2025
 */
package com.nnp.keycloak.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * RoleRepresentation.java
 *
 * @author Arnab Chatterjee
 * @date 16-Jun-2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoleRepresentation {
	private String id;
	private String name;
	private String description;
	private boolean scopeParamRequired;
	private boolean clientRole;
	private String containerId;
}
