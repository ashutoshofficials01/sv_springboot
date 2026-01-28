package com.app.sv_springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.sv_springboot.entities.User_Data_Entity;

@Repository
public interface UserDataRepo extends JpaRepository<User_Data_Entity, Long> {

	@Query(value = "select * from sv_user_data where emailId=?1 and password=?2 and role=?3", nativeQuery = true)
	User_Data_Entity findByEmailAndPassword(String emailId, String password, String role);

}
