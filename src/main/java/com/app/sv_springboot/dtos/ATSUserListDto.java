package com.app.sv_springboot.dtos;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
public class ATSUserListDto {

	@JsonProperty("activeStatus")
	private long activeStatus;

	@JsonProperty("userModifiedOn")
	private String userModifiedOn;

	@JsonProperty("userCreatedOn")
	private String userCreatedOn;

	@JsonProperty("userName")
	private String userName;

	@JsonProperty("statusId")
	private long statusId;

	@JsonProperty("fullName")
	private String fullName;

	@JsonProperty("emailId")
	private String emailId;
	
	@JsonProperty("role")
	private String role;

	@JsonProperty("userRegistrationCount")
	private long userRegistrationCount;

}
