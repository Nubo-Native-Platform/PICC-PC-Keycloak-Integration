package com.nnp.keycloak.exception;

import com.nnp.keycloak.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class KeycloakExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleException(RuntimeException exception) {
        log.error("exception --> {}", LogUtils.sanitizeForLog(exception.getMessage()));
        KeycloakExceptionMessage keycloakExceptionMessage = new KeycloakExceptionMessage();
        if (exception instanceof ResponseStatusException) {
            keycloakExceptionMessage.setCode(String.valueOf(((ResponseStatusException) exception).getStatusCode()));
            keycloakExceptionMessage.setMessage(exception.getMessage());
        } else if (exception instanceof KeycloakIntegrationException) {
            keycloakExceptionMessage
                    .setCode(((KeycloakIntegrationException) exception).getKeycloakExceptionMessage().getCode());
            keycloakExceptionMessage
                    .setMessage(((KeycloakIntegrationException) exception).getKeycloakExceptionMessage().getMessage());
        } else {
            keycloakExceptionMessage.setCode("500");
            keycloakExceptionMessage.setMessage(exception.getMessage());
        }
        return new ResponseEntity<>(keycloakExceptionMessage,
                HttpStatusCode.valueOf(Integer.parseInt(keycloakExceptionMessage.getCode())));
    }

}
