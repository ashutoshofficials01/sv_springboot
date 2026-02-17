package com.app.sv_springboot.dtos;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
public class ATSResumeListDto {

	@JsonProperty("fileName")
	private String fileName;

	@JsonProperty("userId")
	private long userId;

	@JsonProperty("activeStatus")
	private long activeStatus;

	@JsonProperty("resModifiedOn")
	private String resModifiedOn;

	@JsonProperty("resCreatedOn")
	private String resCreatedOn;

	@JsonProperty("atsScore")
	private long atsScore;

	@JsonProperty("resUploadId")
	private long resUploadId;

	@JsonProperty("resumeUploadCount")
	private long resumeUploadCount;

}
