package com.nnp.keycloak.rest.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnvironmentVO implements Serializable {

	private long envId;
	private String envCode;
	private String envName;
	private String envType;
	private String envCustId;
	private String envCustName;
	private String envDesc;
	private String envTenantId;
	private String envFapId;
	private String envFatNo;
	private String envEmail;
	private String envEmailServerIp;
	private String envEmailServerPort;
	private String envStatus;

	private Set<UserAccessVO> userList = new HashSet<UserAccessVO>();

}
