package com.app.sv_springboot.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sv_ats_general_resume_master")
public class ATS_General_Resume_Master_Entity {
	
	@Id
	private long resumeId;
	private String resumeTypeName;
	private String fieldName;

}
