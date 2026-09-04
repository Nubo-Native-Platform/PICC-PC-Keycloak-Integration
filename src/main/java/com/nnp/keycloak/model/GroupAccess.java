package com.nnp.keycloak.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupAccess {
	private boolean view;
	private boolean manage;
	private boolean manageMembership;
}
