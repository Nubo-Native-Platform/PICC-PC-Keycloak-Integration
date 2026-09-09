package com.nnp.keycloak.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.nnp.keycloak.utils.LogUtils;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnp.keycloak.exception.KeycloakExceptionMessage;
import com.nnp.keycloak.exception.KeycloakIntegrationException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author AC
 */
@Component
@Slf4j
public class KeycloakCustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        Reader reader = null;
        Map<String, Object> reqCompSpecMap = new HashMap<String, Object>();
        KeycloakExceptionMessage keycloakExceptionMessage = new KeycloakExceptionMessage();

        // capturing error message from response body.
        if (response.body() != null && response.body().length() != 0) {
            try {

                reader = response.body().asReader(StandardCharsets.UTF_8);
                String result = IOUtils.toString(reader);
                log.error("result --> {}", LogUtils.sanitizeForLog(result));
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                reqCompSpecMap = mapper.readValue(result, new TypeReference<Map<String, Object>>() {
                });
                log.error("reqCompSpecMap --> {}", LogUtils.sanitizeForLog(reqCompSpecMap.toString()));
                log.error("status code --> {}", LogUtils.sanitizeForLog(String.valueOf( response.status())));
                keycloakExceptionMessage.setCode(String.valueOf(response.status()));
                if (reqCompSpecMap.get("error") != null) {
                    keycloakExceptionMessage.setMessage(reqCompSpecMap.get("error").toString());
                } else if (reqCompSpecMap.get("errorMessage") != null) {
                    keycloakExceptionMessage.setMessage(reqCompSpecMap.get("errorMessage").toString());
                } else {
                    keycloakExceptionMessage.setMessage("keycloak call failed");
                }

            } catch (IOException e) {
                log.error("IO Exception on reading exception message feign client {}",LogUtils.sanitizeForLog(e.getMessage()));
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    log.error("IO Exception on reading exception message feign client {}", LogUtils.sanitizeForLog(e.getMessage()));
                }
            }
        }

        // END DECODING ORIGINAL ERROR MESSAGE

        return switch (response.status()) {
            case 400 -> {
                log.error("Bad Request - {}", LogUtils.sanitizeForLog(keycloakExceptionMessage.toString()));
                yield new KeycloakIntegrationException(keycloakExceptionMessage);
                // handle exception
            }
            case 401 -> {
                log.error("Unauthorized - {}", LogUtils.sanitizeForLog(keycloakExceptionMessage.toString()));
                yield new KeycloakIntegrationException(keycloakExceptionMessage);
                // handle exception
            }
            case 404 -> {
                log.error("Not Found - {} ", LogUtils.sanitizeForLog(keycloakExceptionMessage.toString()));
                yield new KeycloakIntegrationException(keycloakExceptionMessage);
                // handle exception
            }
            case 409 -> {
                log.error("Value Already Exists - {} ", LogUtils.sanitizeForLog(keycloakExceptionMessage.toString()));
                yield new KeycloakIntegrationException(keycloakExceptionMessage);
                // handle exception
            }
            default -> {
                log.error("Internal Error - {}", LogUtils.sanitizeForLog(keycloakExceptionMessage.toString()));
                yield new KeycloakIntegrationException(keycloakExceptionMessage);
                // handle exception
            }
        };
    }

}
