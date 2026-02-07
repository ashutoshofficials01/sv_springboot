package com.app.sv_springboot.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sv_ats_general_param_master")
public class ATS_General_Param_Entity {

	@Id
	private long atsGeneralId;

	private String atsParamId;
	private String category;
	private String parameter;
	private String description;
	private long max_points;
	private long penalty_points;
	private long total_points;
	private long resumeId;

}
