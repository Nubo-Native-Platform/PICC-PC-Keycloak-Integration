package com.nnp.keycloak.controller;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nnp.keycloak.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nnp.keycloak.exception.KeycloakExceptionMessage;
import com.nnp.keycloak.exception.KeycloakIntegrationException;
import com.nnp.keycloak.model.AuthTokenRequest;
import com.nnp.keycloak.model.ClientCreationRequest;
import com.nnp.keycloak.model.ClientDetails;
import com.nnp.keycloak.model.ClientRoleDetails;
import com.nnp.keycloak.model.ClientUpdateRequest;
import com.nnp.keycloak.model.CreateClientRoleRequest;
import com.nnp.keycloak.model.CreateRealmRequest;
import com.nnp.keycloak.model.GroupDetails;
import com.nnp.keycloak.model.KeycloakAccessToken;
import com.nnp.keycloak.model.RealmDetails;
import com.nnp.keycloak.model.RealmRoleDetails;
import com.nnp.keycloak.model.UserCreation;
import com.nnp.keycloak.model.UserCredential;
import com.nnp.keycloak.model.UserDetails;
import com.nnp.keycloak.model.UserUpdate;
import com.nnp.keycloak.rest.model.UserVO;
import com.nnp.keycloak.service.KeycloakClientService;
import com.nnp.keycloak.service.feign.KeycloakAdminClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @author AC
 */
@Tag(name = "Keycloak Integration", description = "Endpoints for managing Keycloak Realms, Clients, Users, Roles, and Groups")
@RestController
@Slf4j
public class KeycloakIntegrationController {


    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    @Autowired
    private KeycloakClientService keycloakClientService;

    @Operation(summary = "Create Client", description = "Creates a new client inside the specified Keycloak realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid client creation request"),
            @ApiResponse(responseCode = "404", description = "Realm not found")
    })
    @PostMapping(path = "/admin/realms/{realmName}/clients", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createClient(
            @RequestBody ClientCreationRequest clientCreationRequest,
            @Parameter(description = "Name of the realm", required = true, example = "master") @PathVariable("realmName") String realmName) {
        keycloakAdminClient.createClient(clientCreationRequest, realmName);
    }

    @Operation(summary = "Create Realm", description = "Creates a new realm in Keycloak.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Realm created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid realm creation payload")
    })
    @PostMapping(path = "/admin/realms", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createRealm(@RequestBody CreateRealmRequest createRealmRequest) {
        keycloakAdminClient.createRealm(createRealmRequest);
    }

    @Operation(summary = "Create User", description = "Creates a new user in the specified Keycloak realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user creation data"),
            @ApiResponse(responseCode = "404", description = "Realm not found")
    })
    @PostMapping(path = "/admin/realms/{realmName}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createUser(
            @RequestBody UserCreation userCreationRequest,
            @Parameter(description = "Name of the realm", required = true, example = "master") @PathVariable("realmName") String realmName) {
        keycloakAdminClient.createUser(userCreationRequest, realmName);
    }

    @Operation(summary = "Update User", description = "Updates details of an existing user identified by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User or Realm not found")
    })
    @PutMapping(path = "/admin/realms/{realmName}/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateUser(
            @RequestBody UserUpdate userUpdateRequest,
            @Parameter(description = "Name of the realm", required = true, example = "master") @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId) {

        UserDetails details = resolveUserByUsername(realmName, userId, "update");

        log.debug("user details --> {}", LogUtils.sanitizeForLog(details.toString()));

        keycloakAdminClient.updateUser(userUpdateRequest, realmName, details.getId().toString());

    }

    @Operation(summary = "Add User to Group", description = "Adds the specified user to a group within the realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "404", description = "User or Group not found")
    })
    @PutMapping(path = "/admin/realms/{realmName}/users/{userId}/groups/{groupName}")
    public void updateUserAddGroupMembership(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName) {
        UserDetails userDetails = getUsersByUserName(realmName, userId).stream()
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "update-user-AddGroupMembership-user not found")));
        log.info("updateUserAddGroupMembership--getUsersByUserName call successful");

        GroupDetails groupDetails = getGroupsByGroupName(realmName, groupName);
        log.info("updateUserAddGroupMembership--getGroupsByGroupName call successful");

        keycloakAdminClient.updateUserAddGroupMembership(realmName, userDetails.getId().toString(), groupDetails.getId());
    }

    @Operation(summary = "Send Password Reset Email", description = "Triggers an action email to reset password for the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset password email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping(path = "/admin/realms/{realmName}/users/{userId}/execute-actions-email/forgotpass")
    public void executeActionsEmailResetPass(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId) {
        UserDetails userDetails = getUsersByUserName(realmName, userId).stream()
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "executeActionsEmailResetPass-user not found")));
        log.info("executeActionsEmailResetPass--getUsersByUserName call successful");
        String entity = "[\"UPDATE_PASSWORD\"]";

        keycloakAdminClient.executeActionsEmail(entity, realmName, userDetails.getId().toString());
    }

    @Operation(summary = "Send Email Verification", description = "Triggers a verification email for the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping(path = "/admin/realms/{realmName}/users/{userId}/execute-actions-email/verifyemail")
    public void executeActionsEmailVerifyEmail(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId) {
        UserDetails userDetails = getUsersByUserName(realmName, userId).stream()
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "executeActionsEmailVerifyEmail-user not found")));
        log.info("executeActionsEmailVerifyEmail--getUsersByUserName call successful");
        String entity = "[\"VERIFY_EMAIL\"]";

        keycloakAdminClient.executeActionsEmail(entity, realmName, userDetails.getId().toString());
    }

    @Operation(summary = "Remove User from Group", description = "Removes group membership for the given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
            @ApiResponse(responseCode = "404", description = "User or Group not found")
    })
    @DeleteMapping(path = "/admin/realms/{realmName}/users/{userId}/groups/{groupName}")
    public void updateUserRemoveGroupMembership(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName) {
        UserDetails userDetails = getUsersByUserName(realmName, userId).stream()
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "update-user-RemoveGroupMembership-user not found")));
        log.info("updateUserRemoveGroupMembership--getUsersByUserName call successful");

        GroupDetails groupDetails = getGroupsByGroupName(realmName, groupName);
        log.info("updateUserRemoveGroupMembership--getGroupsByGroupName call successful");

        keycloakAdminClient.updateUserRemoveGroupMembership(realmName, userDetails.getId().toString(), groupDetails.getId());
    }

    @Operation(summary = "Reset User Password", description = "Sets/resets credentials password for the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping(path = "/admin/realms/{realmName}/users/{userId}/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void resetUserPassword(
            @RequestBody UserCredential userCredential,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId) {

        UserDetails details = resolveUserByUsername(realmName, userId, "reset-user-password");

        log.debug("user details --> {}",LogUtils.sanitizeForLog(details.toString()));

        keycloakAdminClient.resetUserPassword(userCredential, realmName, details.getId().toString());

    }

    @Operation(summary = "Delete User", description = "Deletes a user from the specified realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping(path = "/admin/realms/{realmName}/users/{userId}")
    public void deleteUser(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId) {

        UserDetails details = resolveUserByUsername(realmName, userId, "delete");

        log.debug("user details --> {}", LogUtils.sanitizeForLog(details.toString()));

        keycloakAdminClient.deleteUser(realmName, details.getId().toString());

    }

    @Operation(summary = "Get Access Token", description = "Requests an OpenID Connect access token from Keycloak for the specified realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials")
    })
    @PostMapping(path = "/realms/{realmName}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public KeycloakAccessToken getAccessKey(
            AuthTokenRequest authTokenRequest,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        return keycloakAdminClient.getAccessKey(authTokenRequest, realmName).getBody();
    }


    @Operation(summary = "Get All Clients", description = "Fetches all clients configured in the given realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of clients returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}/clients")
    public List<ClientDetails> getAllClientsByRealmName(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        return keycloakAdminClient.getAllClientsByRealmName(realmName);
    }

    @Operation(summary = "Get All Realms", description = "Fetches details of all realms in Keycloak.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of realms returned successfully")
    })
    @GetMapping(path = "/admin/realms")
    public List<RealmDetails> getAllRealmDetails() {
        return keycloakAdminClient.getAllRealmDetails();
    }

    @Operation(summary = "Get Realm Details", description = "Fetches realm details by its name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Realm details returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}")
    public List<RealmDetails> getRealmDetailsByRealmName(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        List<RealmDetails> allRealmDetails = keycloakAdminClient.getAllRealmDetails();
        log.debug("getRealmDetailsByRealmName--getAllRealmDetails call successful");
        return allRealmDetails.stream()
                .filter(realmDetails -> (realmDetails.getRealm() != null)
                        && (realmDetails.getId() != null)
                        && (realmDetails.getRealm().equals(realmName)))
                .toList();
    }

    @Operation(summary = "Get Client by Client ID", description = "Fetches details of a client by client ID within the realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client details returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}/clients/{clientId}")
    public List<ClientDetails> getClientsByClientId(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Client identifier (e.g. nnp-portal)", required = true) @PathVariable("clientId") String clientId) {
        List<ClientDetails> allClientsByRealmName = getAllClientsByRealmName(realmName);
        log.debug("getClientsByClientId--getAllClientsByRealmName call successful");
        return allClientsByRealmName.stream()
                .filter(clientDetails -> (clientDetails.getClientId() != null)
                        && (clientDetails.getId() != null)
                        && (clientDetails.getClientId().equals(clientId)))
                .toList();
    }

    @Operation(summary = "Update Client", description = "Updates settings and configuration of an existing client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client updated successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PutMapping(path = "/admin/realms/{realmName}/clients/{clientId}")
    public void updateClient(
            @RequestBody ClientUpdateRequest clientUpdateRequest,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Client identifier", required = true) @PathVariable("clientId") String clientId) {
        ClientDetails details = getAllClientsByRealmName(realmName).stream()
                .filter(clientDetails -> (clientDetails.getClientId() != null)
                        && (clientDetails.getId() != null)
                        && (clientDetails.getClientId().equals(clientId)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "updateClient-getAllClientsByRealmName-client not found")));

        log.debug("details.getId() --> {}", LogUtils.sanitizeForLog(details.getId()));
        log.debug("clientUpdateRequest --> {}", LogUtils.sanitizeForLog(clientUpdateRequest.toString()));
        keycloakAdminClient.updateClient(clientUpdateRequest, realmName, details.getId());
    }

    @Operation(summary = "Delete Client", description = "Deletes a client by client ID from the given realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @DeleteMapping(path = "/admin/realms/{realmName}/clients/{clientId}")
    public void deleteClient(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Client identifier", required = true) @PathVariable("clientId") String clientId) {
        List<ClientDetails> allClientsByRealmName = getAllClientsByRealmName(realmName);
        log.debug("deleteClient--allClientsByRealmName call successful");
        ClientDetails details = allClientsByRealmName.stream()
                .filter(clientDetails -> (clientDetails.getClientId() != null)
                        && (clientDetails.getId() != null)
                        && (clientDetails.getClientId().equals(clientId)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "deleteClient-getAllClientsByRealmName-client not found")));

        keycloakAdminClient.deleteClient(realmName, details.getId());
    }


    @Operation(summary = "Get All Users", description = "Fetches users from the given realm (up to 200).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}/users")
    public List<UserDetails> getAllUsersByRealmName(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        return keycloakAdminClient.getAllUsersByRealmName(realmName,200);
    }

    @Operation(summary = "Get Users by Username", description = "Fetches user details matching the given username in the realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of matching users returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}/users/{userId}")
    public List<UserDetails> getUsersByUserName(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Username of the user", required = true) @PathVariable("userId") String userId) {
        int userCount = keycloakAdminClient.getCountUsersByRealmName(realmName);
        log.info("User Count for the realm - {} in keycloak is : {}", LogUtils.sanitizeForLog(realmName),LogUtils.sanitizeForLog(String.valueOf(userCount)));
        List<UserDetails> allUsersByRealmName = keycloakAdminClient.getAllUsersByRealmName(realmName,userCount);
        log.debug("getAllUsersByRealmName call successful");
        return allUsersByRealmName.stream()
                .filter(userDetails -> (userDetails.getUsername() != null)
                        && (userDetails.getId() != null)
                        && (userDetails.getUsername().equalsIgnoreCase(userId)))
                .toList();
    }

    /**
     * Resolves a single user by username within a realm.
     *
     * NOTE: this replaces the old pattern (still used in a couple of other
     * endpoints below) of calling the local {@code getAllUsersByRealmName(realmName)}
     * helper, which silently caps results at 200 users (see its {@code max=200}
     * default) and matched on exact case. In realms with 200+ users, or with any
     * username differing only in case from what was stored, that pattern could
     * fail to find a user that genuinely exists — including a user that was
     * *just* created moments earlier in the same request chain — and would throw
     * a 404 "not found" even though the user was really there.
     *
     * This helper mirrors {@link #getUsersByUserName(String, String)}: it fetches
     * the realm's *actual* user count first via {@code getCountUsersByRealmName}
     * and requests exactly that many, then matches case-insensitively, so it
     * can never silently truncate results the way the capped lookup could.
     *
     * @param realmName    the Keycloak realm (tenant)
     * @param userId       the username to resolve (NOT the internal UUID)
     * @param operationTag short tag identifying the calling operation, used only
     *                     to make the 404 error message traceable to its caller
     *                     (e.g. "update", "delete", "reset-user-password")
     */
    private UserDetails resolveUserByUsername(String realmName, String userId, String operationTag) {
        int userCount = keycloakAdminClient.getCountUsersByRealmName(realmName);
        List<UserDetails> allUsersByRealmName = keycloakAdminClient.getAllUsersByRealmName(realmName, userCount);
        log.debug("{}--getAllUsersByRealmName (uncapped) call successful, {} users in realm {}",
                LogUtils.sanitizeForLog(operationTag),LogUtils.sanitizeForLog(String.valueOf( allUsersByRealmName.size())), LogUtils.sanitizeForLog(realmName));
        return allUsersByRealmName.stream()
                .filter(userDetails -> (userDetails.getUsername() != null)
                        && (userDetails.getId() != null)
                        && (userDetails.getUsername().equalsIgnoreCase(userId)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(
                        new KeycloakExceptionMessage("404", operationTag + "-user not found: " + userId + " in realm " + realmName)));
    }

    @Operation(summary = "Get Client Roles", description = "Fetches role details for the given client inside the realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client role details returned successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping(path = "/admin/realms/{realmName}/clients/{clientId}/roles")
    public List<ClientRoleDetails> getClientRoleDetails(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Client identifier", required = true) @PathVariable("clientId") String clientId) {
        List<ClientDetails> clientDetails = getAllClientsByRealmName(realmName);
        log.debug("getAllClientsByRealmName successful");
        clientDetails.forEach(client -> {
            if (client.getClientId() != null && client.getClientId().equals(clientId)) {
                log.debug("ClientId --> {} Id --> {} clientId --> {}" ,LogUtils.sanitizeForLog(client.getClientId()) ,LogUtils.sanitizeForLog(client.getId()),LogUtils.sanitizeForLog( clientId));
            }
        });

        return clientDetails.stream()
                .filter(client -> client.getClientId() != null && client.getClientId().equals(clientId))
                .map(client -> keycloakAdminClient.getClientRoleDetails(realmName, client.getId()))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "getClientRoleDetails--client not found")));
    }

    @Operation(summary = "Get Realm Roles", description = "Fetches all roles defined at the realm level.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of realm roles returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}/roles")
    public List<RealmRoleDetails> getRealmRoleDetails(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        return keycloakAdminClient.getRealmRoleDetails(realmName);
    }

    @Operation(summary = "Create Client Role", description = "Creates a new role within a client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client role created successfully")
    })
    @PostMapping(path = "/admin/realms/{realmName}/clients/{clientId}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createClientRole(
            @RequestBody CreateClientRoleRequest createClientRequest,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Client identifier", required = true) @PathVariable("clientId") String clientId) {
        List<ClientDetails> clientDetails = getAllClientsByRealmName(realmName);
        clientDetails.forEach(client -> {
            if (client.getClientId() != null && client.getClientId().equals(clientId)) {
                keycloakAdminClient.createClientRole(createClientRequest, realmName, client.getId());
            }
        });
    }

    @Operation(summary = "Get All Groups", description = "Fetches all groups defined in the realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of groups returned successfully")
    })
    @GetMapping(path = "/admin/realms/{realmName}/groups")
    public List<GroupDetails> getAllGroupsByRealmName(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        return keycloakAdminClient.getAllGroupsByRealmName(realmName);
    }

    @Operation(summary = "Get Group by Group Name", description = "Fetches details of a specific group by its name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group details returned successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping(path = "/admin/realms/{realmName}/groups/{groupName}")
    public GroupDetails getGroupsByGroupName(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName) {

        return getAllGroupsByRealmName(realmName).stream()
                .filter(groupDetails -> (groupDetails.getId() != null)
                        && (groupDetails.getName() != null)
                        && (groupDetails.getName().equals(groupName)))
                .findFirst()
                .map(groupDetails -> keycloakAdminClient.getGroupDetailsByGroupId(realmName, groupDetails.getId()))
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "getGroupsByGroupName--group not found")));
    }

    @Operation(summary = "Get Group Client Role Details", description = "Fetches client role mappings for a specific group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group client role details returned successfully"),
            @ApiResponse(responseCode = "404", description = "Group or Client not found")
    })
    @GetMapping(path = "/admin/realms/{realmName}/groups/{groupName}/role-mappings/clients/{clientId}")
    public List<ClientRoleDetails> getGroupClientRoleDetails(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName,
            @Parameter(description = "Client identifier", required = true) @PathVariable("clientId") String clientId) {
        GroupDetails grpDetails = getAllGroupsByRealmName(realmName).stream()
                .filter(groupDetails -> (groupDetails.getId() != null)
                        && (groupDetails.getName() != null)
                        && (groupDetails.getName().equals(groupName)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "getGroupClientRoleDetails--group not found")));

        ClientDetails clDetails = getAllClientsByRealmName(realmName).stream()
                .filter(clientDetails -> (clientDetails.getId() != null)
                        && (clientDetails.getClientId() != null)
                        && (clientDetails.getClientId().equals(clientId)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "getGroupClientRoleDetails--client not found")));

        return keycloakAdminClient.getGroupClientRoleDetails(realmName, grpDetails.getId(), clDetails.getId());

    }

    @Operation(summary = "Get Group Realm Role Details", description = "Fetches realm role mappings for a specific group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group realm role mappings returned successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping(path = "/admin/realms/{realmName}/groups/{groupName}/role-mappings/realm")
    public List<RealmRoleDetails> getGroupRealmRoleDetails(
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName) {
        GroupDetails grpDetails = getAllGroupsByRealmName(realmName).stream()
                .filter(groupDetails -> (groupDetails.getId() != null)
                        && (groupDetails.getName() != null)
                        && (groupDetails.getName().equals(groupName)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "getGroupClientRoleDetails--group not found")));

        return keycloakAdminClient.getGroupRealmRoleDetails(realmName, grpDetails.getId());
    }

    @Operation(summary = "Assign Client Roles to Group", description = "Assigns specified client roles to a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles assigned to group successfully"),
            @ApiResponse(responseCode = "404", description = "Group or Client not found")
    })
    @PostMapping(path = "/admin/realms/{realmName}/groups/{groupName}/role-mappings/clients/{clientId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void assignClientRolesToGroup(
            @RequestBody List<ClientRoleDetails> clientRoleDetailsList,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName,
            @Parameter(description = "Client identifier", required = true) @PathVariable("clientId") String clientId) {
        GroupDetails grpDetails = getAllGroupsByRealmName(realmName).stream()
                .filter(groupDetails -> (groupDetails.getId() != null)
                        && (groupDetails.getName() != null)
                        && (groupDetails.getName().equals(groupName)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "assignClientRolesToGroup--group not found")));

        ClientDetails clDetails = getAllClientsByRealmName(realmName).stream()
                .filter(clientDetails -> (clientDetails.getId() != null)
                        && (clientDetails.getClientId() != null)
                        && (clientDetails.getClientId().equals(clientId)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "assignClientRolesToGroup--client not found")));

        Set<String> roleNameSet = clientRoleDetailsList.stream()
                .map(ClientRoleDetails::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ClientRoleDetails> roleDetailsList = getClientRoleDetails(realmName, clientId).stream()
                .filter(clientRoleDetails -> (clientRoleDetails.getId() != null)
                        && (clientRoleDetails.getName() != null)
                        && (roleNameSet.contains(clientRoleDetails.getName())))
                .toList();

        log.debug("assignClientRolesToGroup--roleDetails --> {}", LogUtils.sanitizeForLog(roleDetailsList.toString()));

        keycloakAdminClient.assignClientRolesToGroup(roleDetailsList, realmName, grpDetails.getId(), clDetails.getId());

    }

    @Operation(summary = "Assign Realm Roles to Group", description = "Assigns specified realm roles to a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Realm roles assigned to group successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping(path = "/admin/realms/{realmName}/groups/{groupName}/role-mappings/realm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void assignRealmRolesToGroup(
            @RequestBody List<RealmRoleDetails> realmRoleDetailsList,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName,
            @Parameter(description = "Name of the group", required = true) @PathVariable("groupName") String groupName) {
        GroupDetails grpDetails = getAllGroupsByRealmName(realmName).stream()
                .filter(groupDetails -> (groupDetails.getId() != null)
                        && (groupDetails.getName() != null)
                        && (groupDetails.getName().equals(groupName)))
                .findFirst()
                .orElseThrow(() -> new KeycloakIntegrationException(new KeycloakExceptionMessage("404", "assignRealmRolesToGroup--group not found")));

        Set<String> roleNameSet = realmRoleDetailsList.stream()
                .map(RealmRoleDetails::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<RealmRoleDetails> roleDetailsList = getRealmRoleDetails(realmName).stream()
                .filter(realmRoleDetails -> (realmRoleDetails.getId() != null)
                        && (realmRoleDetails.getName() != null)
                        && (roleNameSet.contains(realmRoleDetails.getName())))
                .toList();

        log.debug("assignRealmRolesToGroup--roleDetails --> {}", LogUtils.sanitizeForLog(roleDetailsList.toString()));

        keycloakAdminClient.assignRealmRolesToGroup(roleDetailsList, realmName, grpDetails.getId());
    }

    @Operation(summary = "Create Group", description = "Creates a new group in the specified realm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group payload")
    })
    @PostMapping(path = "/admin/realms/{realmName}/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createGroup(
            @RequestBody GroupDetails groupDetails,
            @Parameter(description = "Name of the realm", required = true) @PathVariable("realmName") String realmName) {
        log.debug("GroupDetails --> {}", LogUtils.sanitizeForLog(groupDetails.toString()));
        keycloakAdminClient.createGroup(groupDetails, realmName);
    }

    //Environment Replication code starts here.
    //THIS IS THE CODE USED FOR SHARED COMPONENT SELECTION.
    //THIS SIMILER SERVICE MUST EXIST IN REDMINE AND OTHER INTEGRATION SERVICE AS THOSE ARE ALSO SHARED COMPONENT.
    @Operation(summary = "Create Realm for Environment Replication", description = "Creates a realm for an environment code and configures the initial admin user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Realm replicated successfully for environment")
    })
    @PostMapping(path="/envrep/{envCode}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createRealmForEnvironment(
            @Parameter(description = "Environment code", required = true, example = "DEV") @PathVariable String envCode,
            @RequestBody UserVO user) {
        keycloakClientService.createRealm(envCode, user);
        return ResponseEntity.ok("Success");

    }

}