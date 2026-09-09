package com.nnp.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential {
    private Integer hashIterations = 1;
    private String type;
    private String value;
    private boolean temporary;
}
