package com.nnp.keycloak.service.feign;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nnp.keycloak.config.KeycloakConfig;
import com.nnp.keycloak.model.*;

import java.util.List;

/**
 * @author ebasusa
 */
@FeignClient(name = "keycloakClient", primary = false, url = "${keycloak.url}", configuration = KeycloakConfig.class)
public interface KeycloakAdminClient {

    @PostMapping(path = "/admin/realms/{realmName}/clients", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void createClient(@RequestBody ClientCreationRequest clientCreationRequest,
            @PathVariable("realmName") String realmName);

    @PostMapping(path = "/admin/realms", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void createRealm(@RequestBody CreateRealmRequest createRealmRequest);

    @PostMapping(path = "/admin/realms/{realmName}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void createUser(@RequestBody UserCreation userCreationRequest, @PathVariable("realmName") String realmName);

    @PutMapping(path = "/admin/realms/{realmName}/users/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void updateUser(@RequestBody UserUpdate userUpdateRequest, @PathVariable("realmName") String realmName,
            @PathVariable("id") String id);

    @DeleteMapping(path = "/admin/realms/{realmName}/users/{id}")
    public void deleteUser(@PathVariable("realmName") String realmName, @PathVariable("id") String id);

    @PutMapping(path = "/admin/realms/{realmName}/users/{id}/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void resetUserPassword(@RequestBody UserCredential userCredential,
            @PathVariable("realmName") String realmName, @PathVariable("id") String id);

    @PutMapping(path = "/admin/realms/{realmName}/users/{id}/groups/{groupId}")
    public void updateUserAddGroupMembership(@PathVariable("realmName") String realmName, @PathVariable("id") String id,
            @PathVariable("groupId") String groupId);

    @DeleteMapping(path = "/admin/realms/{realmName}/users/{id}/groups/{groupId}")
    public void updateUserRemoveGroupMembership(@PathVariable("realmName") String realmName,
            @PathVariable("id") String id, @PathVariable("groupId") String groupId);

    @PostMapping(path = "/realms/{realmName}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    public ResponseEntity<KeycloakAccessToken> getAccessKey(@RequestBody AuthTokenRequest authTokenRequest,
            @PathVariable("realmName") String realmName);

    @GetMapping(path = "/admin/realms/{realmName}/clients")
    public List<ClientDetails> getAllClientsByRealmName(@PathVariable("realmName") String realmName);

    @GetMapping(path = "/admin/realms")
    public List<RealmDetails> getAllRealmDetails();

    @GetMapping(path = "/admin/realms/{realmName}")
    public RealmDetails getRealmDetailsByRealmName(@PathVariable("realmName") String realmName);

    @PutMapping(path = "/admin/realms/{realmName}/clients/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void updateClient(@RequestBody ClientUpdateRequest clientUpdateRequest,
            @PathVariable("realmName") String realmName, @PathVariable("id") String id);

    @PutMapping(path = "/admin/realms/{realmName}/users/{id}/execute-actions-email", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void executeActionsEmail(@RequestBody String action, @PathVariable("realmName") String realmName,
            @PathVariable("id") String id);

    @DeleteMapping(path = "/admin/realms/{realmName}/clients/{id}")
    public void deleteClient(@PathVariable("realmName") String realmName, @PathVariable("id") String id);

    @GetMapping(path = "/admin/realms/{realmName}/users/count")
    public Integer getCountUsersByRealmName(@PathVariable("realmName") String realmName);

    @GetMapping(path = "/admin/realms/{realmName}/users")
    public List<UserDetails> getAllUsersByRealmName(@PathVariable("realmName") String realmName,
            @RequestParam(required = false, defaultValue = "100", name = "max") Integer max);

    @GetMapping(path = "/admin/realms/{realmName}/groups")
    public List<GroupDetails> getAllGroupsByRealmName(@PathVariable("realmName") String realmName);

    @GetMapping(path = "/admin/realms/{realmName}/groups/{id}")
    public GroupDetails getGroupDetailsByGroupId(@PathVariable("realmName") String realmName,
            @PathVariable("id") String id);

    @PostMapping(path = "/admin/realms/{realmName}/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    public void createGroup(@RequestBody GroupDetails groupDetails, @PathVariable("realmName") String realmName);

    @GetMapping(path = "/admin/realms/{realmName}/clients/{id}/roles")
    public List<ClientRoleDetails> getClientRoleDetails(@PathVariable("realmName") String realmName,
            @PathVariable("id") String id);

    @GetMapping(path = "/admin/realms/{realmName}/groups/{groupId}/role-mappings/clients/{id}")
    public List<ClientRoleDetails> getGroupClientRoleDetails(@PathVariable("realmName") String realmName,
            @PathVariable("groupId") String groupId, @PathVariable("id") String id);

    @GetMapping(path = "/admin/realms/{realmName}/groups/{groupId}/role-mappings/realm")
    public List<RealmRoleDetails> getGroupRealmRoleDetails(@PathVariable("realmName") String realmName,
            @PathVariable("groupId") String groupId);

    @GetMapping(path = "/admin/realms/{realmName}/roles")
    public List<RealmRoleDetails> getRealmRoleDetails(@PathVariable("realmName") String realmName);

    @PostMapping(path = "/admin/realms/{realmName}/groups/{groupId}/role-mappings/clients/{id}")
    @Headers("Content-Type: application/json")
    public void assignClientRolesToGroup(@RequestBody List<ClientRoleDetails> clientRoleDetailsList,
            @PathVariable("realmName") String realmName, @PathVariable("groupId") String groupId,
            @PathVariable("id") String id);

    @PostMapping(path = "/admin/realms/{realmName}/groups/{groupId}/role-mappings/realm")
    @Headers("Content-Type: application/json")
    public void assignRealmRolesToGroup(@RequestBody List<RealmRoleDetails> realmRoleDetailsList,
            @PathVariable("realmName") String realmName, @PathVariable("groupId") String groupId);

    @PostMapping(path = "/admin/realms/{realmName}/clients/{id}/roles")
    @Headers("Content-Type: application/json")
    public void createClientRole(@RequestBody CreateClientRoleRequest createClientRequest,
            @PathVariable("realmName") String realmName, @PathVariable("id") String id);

    @GetMapping(path = "/admin/realms/{realm}/users")
    public List<UserDetails> getUserByRealmAndUserId(@PathVariable("realm") String realm,
            @RequestParam("username") String username);

    @GetMapping(path = "/admin/realms/{realm}/clients")
    public List<ClientDetails> getClientRealmManagement(@PathVariable("realm") String realmName);

    @GetMapping(path = "/admin/realms/{realm}/clients/{realm-mgmt-client-id}/roles/realm-admin")
    public RoleRepresentation getRealmManagementAdmin(@PathVariable("realm") String realmName,
            @PathVariable("realm-mgmt-client-id") String realmManagementClientId);

    @PostMapping(path = "/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientId}")
    public void assignRoleToUserOfRealm(@PathVariable("realm") String realm, @PathVariable("userId") String userId,
            @PathVariable("clientId") String clientId, @RequestBody List<RoleRepresentation> roleRepresentations);
}
