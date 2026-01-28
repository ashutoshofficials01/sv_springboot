package com.app.sv_springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.sv_springboot.entities.Res_Score_Entity;

@Repository
public interface ResScoreRepo extends JpaRepository<Res_Score_Entity, Long> {

	@Query(value = "select * from sv_res_score_data where userId=?1 and activeStatus=1", nativeQuery = true)
	Res_Score_Entity findByUserId(Long userId);

	@Modifying
	@Transactional
	@Query(" UPDATE Res_Score_Entity r SET r.atsScore = :atsScore, r.fileName = :fileName, r.modifiedOn = CURRENT_TIMESTAMP WHERE r.resUploadId = :resUploadId ")
	long updateFileNameByResUploadId(long resUploadId, long atsScore, String fileName);

}
