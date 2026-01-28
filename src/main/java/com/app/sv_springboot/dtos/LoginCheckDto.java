package com.app.sv_springboot.dtos;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@Component
public class LoginCheckDto {
	
	@JsonProperty("userId")
	private long userId;

	@JsonProperty("emailId")
	private String emailId;

	@JsonProperty("password")
	private String password;

	@JsonProperty("role")
	private String role;

	@JsonProperty("activeStatus")
	private long activeStatus;

	@JsonProperty("fullName")
	private String fullName;

	@JsonProperty("statusId")
	private long statusId;

	@JsonProperty("userName")
	private String userName;

}
