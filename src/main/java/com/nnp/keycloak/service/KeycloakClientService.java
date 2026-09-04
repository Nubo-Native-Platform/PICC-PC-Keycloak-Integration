package com.nnp.keycloak.service;

import java.util.ArrayList;
import java.util.List;

import com.nnp.keycloak.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nnp.keycloak.model.AuthTokenRequest;
import com.nnp.keycloak.model.ClientDetails;
import com.nnp.keycloak.model.CreateRealmRequest;
import com.nnp.keycloak.model.KeycloakAccessToken;
import com.nnp.keycloak.model.RoleRepresentation;
import com.nnp.keycloak.model.UserCreation;
import com.nnp.keycloak.model.UserCredential;
import com.nnp.keycloak.model.UserDetails;
import com.nnp.keycloak.rest.model.UserVO;
import com.nnp.keycloak.service.feign.KeycloakAdminClient;

@Service
@Slf4j
public class KeycloakClientService {

	@Autowired
	private KeycloakAdminClient keycloakClient;

	public void createRealm(String envCode, UserVO user) {

		// create realm
		CreateRealmRequest realmReq = new CreateRealmRequest();
		realmReq.setRealm(envCode);
		realmReq.setEnabled(true);
		realmReq.setDisplayName(envCode);
		realmReq.setLoginTheme("nnp");

		keycloakClient.createRealm(realmReq);

		// create user
		UserCreation userCreateReq = new UserCreation();
		userCreateReq.setUsername(user.getUserId());
		userCreateReq.setEnabled(true);
		userCreateReq.setEmailVerified(false);
		userCreateReq.setFirstName(user.getFirstName());
		userCreateReq.setLastName(user.getLastName());
		userCreateReq.setEmail(user.getEmailId());

		UserCredential credential = new UserCredential();
		credential.setType("password");
		credential.setValue(user.getPassword());
		credential.setTemporary(false);

		List<UserCredential> credentials = new ArrayList<>();
		credentials.add(credential);
		userCreateReq.setCredentials(credentials);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Realm provisioning delay interrupted: {}", LogUtils.sanitizeForLog(e.getMessage()));
		}
		keycloakClient.createUser(userCreateReq, envCode);

		UserDetails userDetails = keycloakClient.getUserByRealmAndUserId(envCode, user.getUserId()).get(0);
		// set realm-admin role to user
		/*
		 * 1. Get realm-management client id
		 * 2. Get Role Representation for realm-admin
		 * 3. Assign realm-admin Role to User
		 */
		List<ClientDetails> clientDeatils = keycloakClient.getClientRealmManagement(envCode/* ,"realm-management" */);
		clientDeatils.forEach(client -> {
			if ("realm-management".equalsIgnoreCase(client.getClientId())) {
				RoleRepresentation roleRep = keycloakClient.getRealmManagementAdmin(envCode, client.getId());
				List<RoleRepresentation> roleRepList = new ArrayList<>();
				roleRepList.add(roleRep);
				keycloakClient.assignRoleToUserOfRealm(envCode, userDetails.getId().toString(), client.getId(),
						roleRepList);
			}
		});
	}

	public KeycloakAccessToken getAccessKey(AuthTokenRequest authTokenRequest) {
		return keycloakClient.getAccessKey(authTokenRequest, "master").getBody();
	}

}
