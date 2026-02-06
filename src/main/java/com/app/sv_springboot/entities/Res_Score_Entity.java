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
@Table(name = "sv_res_score_data")
public class Res_Score_Entity {
	
	@Id
	@SequenceGenerator(name = "zseq_sv_res_score_data", allocationSize = 1, initialValue = 10)
	@GeneratedValue(generator = "zseq_sv_res_score_data", strategy = GenerationType.SEQUENCE)
	private long resUploadId;

	private LocalDateTime createdOn;
	private LocalDateTime modifiedOn;
	private String fileName;
	private long atsScore;
	private long userId;
	private long activeStatus;

}
