package com.app.sv_springboot.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
public class ATSDataListDto {

	@JsonProperty("userDataList")
	private List<ATSUserListDto> userDataList;

	@JsonProperty("resumeDataList")
	private List<ATSResumeListDto> resumeDataList;

}
