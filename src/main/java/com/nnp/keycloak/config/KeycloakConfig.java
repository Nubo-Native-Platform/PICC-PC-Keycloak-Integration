package com.nnp.keycloak.config;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.nnp.keycloak.utils.LogUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nnp.keycloak.model.AuthTokenRequest;
import com.nnp.keycloak.model.KeycloakAccessToken;
import com.nnp.keycloak.service.feign.KeycloakClient;

import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author AC
 */
@Configuration
@Slf4j
@Getter
@Setter
public class KeycloakConfig {

    private KeycloakAccessToken tokenResponse = obtainTokens();

    private KeycloakClient keycloakClient;

    @Value("${keycloak.grant_type_client_credentials}")
    private String grantTypeClientCredentials;

    @Value("${keycloak.grant_type_refresh_token}")
    private String grantTypeRefreshToken;

    @Value("${keycloak.client_id}")
    private String clientId;

    @Value("${keycloak.client_secret}")
    private String clientSecret;
    @Value("${keycloak.master.realm}")
    private String masterRealm;

    private void clearTokens() {
        tokenResponse = null;
    }

    @Bean
    public Encoder encoder(ObjectFactory<HttpMessageConverters> converters) {
        return new SpringFormEncoder(new SpringEncoder(converters));
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new KeycloakCustomErrorDecoder();
    }

    public final KeycloakClient getKeycloakClient() {
        return keycloakClient;
    }

    @Autowired
    public void setKeycloakClient(@Lazy KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    private boolean isAccessTokenValid(String encodedJWT) {
        DecodedJWT decodedJWT = JWT.decode(encodedJWT);
        if (decodedJWT.getExpiresAt().compareTo(new Date()) < 0) {
            return false;
        }
        return true;
    }

    @Cacheable("accessToken")
    private KeycloakAccessToken obtainTokens() {
        AuthTokenRequest authTokenRequest = new AuthTokenRequest();
        authTokenRequest.setClient_id(clientId);
        authTokenRequest.setClient_secret(clientSecret);
        authTokenRequest.setGrant_type(grantTypeClientCredentials);
        return getKeycloakClient().getAccessKey(authTokenRequest, masterRealm).getBody();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            try {


                log.debug("=========================================");
                log.debug("masterRealm --> {}", LogUtils.sanitizeForLog(masterRealm));
                log.debug("=========================================");
                if (requestTemplate.url().equals("/realms/" + masterRealm + "/protocol/openid-connect/token")) {
                    log.debug(LogUtils.sanitizeForLog(requestTemplate.request().toString()));
                    log.debug("This request does not require auth header");
                } else {
                    log.debug("Adding Authorization header bearer token");

                    try (ExecutorService executor = Executors.newSingleThreadExecutor()) {

                        Future<String> future = executor.submit(() -> {

                            if (tokenResponse == null) {
                                tokenResponse = obtainTokens();
                            }

                            try {
                                String encodedJWT = tokenResponse.getAccessToken();

                                if (isAccessTokenValid(encodedJWT)) {
                                    return encodedJWT;
                                } else {
                                    log.debug("Access token is expired.");
                                }
                            } catch (Exception cause) {
                                clearTokens();
                                throw new RuntimeException("Failed to parse access token", cause);
                            }
                            try {
                                tokenResponse = tryRefreshToken();
                            } catch (Exception e) {
                                log.debug("Refresh token is expired.");
                                tokenResponse = obtainTokens();
                                log.info(LogUtils.sanitizeForLog(e.getMessage()), LogUtils.sanitizeForLog(e.getCause().fillInStackTrace().toString()));
                            }
                            return tokenResponse.getAccessToken();

                        });

                        try {
                            String accessToken = future.get(); // wait for a thread to complete
                            log.debug(LogUtils.sanitizeForLog(accessToken));
                            requestTemplate.header("Authorization", "Bearer " + accessToken);
                        } catch (ExecutionException | InterruptedException e) {
                            log.error(LogUtils.sanitizeForLog(e.getMessage()));
                        }
                        executor.shutdown();
                    }

                }

                log.info("Inside requestInterceptor");
            } catch ( Exception e) {
               log.error(LogUtils.sanitizeForLog(e.getMessage()));
            }
        };
    }

    private KeycloakAccessToken tryRefreshToken() {
        AuthTokenRequest authTokenRequest = new AuthTokenRequest();
        authTokenRequest.setClient_id(clientId);
        authTokenRequest.setClient_secret(clientSecret);
        authTokenRequest.setGrant_type(grantTypeRefreshToken);
        authTokenRequest.setRefresh_token(tokenResponse.getRefreshToken());
        return getKeycloakClient().getAccessKey(authTokenRequest, masterRealm).getBody();
    }

}
