/**
 * UserVO.java
 *
 * @author Arnab Chatterjee
 * @date 13-Jun-2025
 */
package com.nnp.keycloak.rest.model;

import lombok.Data;

/**
 * UserVO.java
 *
 * @author Arnab Chatterjee
 * @date 13-Jun-2025
 */
@Data
public class UserVO {
	
	private String userId;
	private String firstName;
	private String lastName;
	private String emailId;
	private String contactNumber;
	private String userType;
	private String userStatus;
	private String password;

}
