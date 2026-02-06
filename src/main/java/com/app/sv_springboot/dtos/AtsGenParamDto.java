package com.app.sv_springboot.dtos;

import org.springframework.stereotype.Component;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
public class AtsGenParamDto {

	@JsonProperty("atsGeneralId")
	private long atsGeneralId;

	@JsonProperty("atsParamId")
	private String atsParamId;

	@JsonProperty("category")
	private String category;

	@JsonProperty("description")
	private String description;

	@JsonProperty("max_points")
	private long max_points;

	@JsonProperty("parameter")
	private String parameter;

	@JsonProperty("penalty_points")
	private long penalty_points;

	@JsonProperty("total_points")
	private long total_points;

}
