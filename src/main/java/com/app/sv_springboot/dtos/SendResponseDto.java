package com.app.sv_springboot.dtos;

import java.util.*;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@Component
public class SendResponseDto {
	
	@JsonProperty("atsDataList")
	private List<AtsListDto> atsDataList;

	@JsonProperty("success")
	private boolean success;

	@JsonProperty("filename")
	private String filename;

	@JsonProperty("savedAs")
	private String savedAs;
	
	@JsonProperty("path")
	private String path;
	
	@JsonProperty("atsScore")
	private long atsScore;

	@JsonProperty("message")
	private String message;

}
