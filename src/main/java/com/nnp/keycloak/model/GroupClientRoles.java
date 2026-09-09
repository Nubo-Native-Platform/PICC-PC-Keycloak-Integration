package com.nnp.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupClientRoles {
	@JsonProperty("realm-management")
	private List<String> realmManagement;
}
