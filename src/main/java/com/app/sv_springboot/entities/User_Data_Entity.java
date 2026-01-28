package com.app.sv_springboot.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "sv_user_data")
public class User_Data_Entity {

	@Id
	@SequenceGenerator(name = "zseq_sv_user_data")
	@GeneratedValue(generator = "zseq_sv_user_data", strategy = GenerationType.SEQUENCE)
	private long userUniqueId;

	private LocalDateTime createdOn;
	private LocalDateTime modifiedOn;
	private String fullName;
	private String emailId;
	private String userName;
	private String role;
	private String password;
	private long statusId;
	private long activeStatus;

}
