package com.app.sv_springboot.dtos;

import java.util.*;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
public class AtsListDto {

	@JsonProperty("atsScore")
	private long atsScore;

	@JsonProperty("atsGeneralId")
	private long atsGeneralId;

	@JsonProperty("atsParamId")
	private String atsParamId;

	@JsonProperty("atsParamData")
	private Map<String, Object> atsParamData;
	
	@JsonProperty("atsParamType")
	private String atsParamType;

	@JsonProperty("atsGeneralParamDto")
	private AtsGenParamDto atsGeneralParamDto;

}
