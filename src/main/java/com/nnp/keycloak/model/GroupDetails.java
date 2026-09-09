package com.nnp.keycloak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupDetails {
	private String id;
	private String name;
	private String path;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, List<String>> attributes;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<String> realmRoles;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, List<String>> clientRoles;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private GroupAccess access;
}
