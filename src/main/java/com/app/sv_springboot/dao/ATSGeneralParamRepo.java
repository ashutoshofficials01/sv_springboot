package com.app.sv_springboot.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.sv_springboot.entities.ATS_General_Param_Entity;
import com.app.sv_springboot.entities.Res_Score_Entity;


public interface ATSGeneralParamRepo extends JpaRepository<ATS_General_Param_Entity, Long> {
	
	@Query(value = "select * from sv_ats_general_param_master where atsGeneralId=?1", nativeQuery = true)
	ATS_General_Param_Entity findByAtsGeneralId(Long atsGeneralId);
	
	@Query(value = "select * from sv_ats_general_param_master where resumeId=?1", nativeQuery = true)
	List<ATS_General_Param_Entity> findAllByResumeId(Long resumeId);

}
