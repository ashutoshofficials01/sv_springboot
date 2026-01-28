package com.app.sv_springboot.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.sv_springboot.dao.UserDataRepo;
import com.app.sv_springboot.dtos.LoginCheckDto;
import com.app.sv_springboot.entities.User_Data_Entity;
import com.app.sv_springboot.services.LoginService;

@Service
public class LoginServiceImpl implements LoginService {

	private Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

	@Autowired
	UserDataRepo userDataRepo;

	@Override
	public LoginCheckDto checkLogin(String role, String emailId, String password) {

		LoginCheckDto logg = new LoginCheckDto();
		User_Data_Entity loginData = userDataRepo.findByEmailAndPassword(emailId, password, role);
		
		logger.info("loginData :: "+ loginData);

		if (loginData != null) {
			logg.setActiveStatus(loginData.getActiveStatus());
			logg.setEmailId(loginData.getEmailId());
			logg.setFullName(loginData.getFullName());
			logg.setPassword(loginData.getPassword());
			logg.setRole(loginData.getRole());
			logg.setStatusId(loginData.getStatusId());
			logg.setUserId(loginData.getUserUniqueId());
			logg.setUserName(loginData.getUserName());
		} else {
			logg.setActiveStatus(0);
			logg.setEmailId("");
			logg.setFullName("");
			logg.setPassword("");
			logg.setRole("");
			logg.setStatusId(0);
			logg.setUserId(0);
			logg.setUserName("");
		}

		return logg;
	}

	@Override
	public LoginCheckDto registerUser(LoginCheckDto regSet) {

		User_Data_Entity ud = new User_Data_Entity();
		LoginCheckDto lcDto = new LoginCheckDto();
		User_Data_Entity ude = new User_Data_Entity();

		ud.setActiveStatus(regSet.getActiveStatus());
		ud.setCreatedOn(LocalDateTime.now());
		ud.setEmailId(regSet.getEmailId());
		ud.setFullName(regSet.getFullName());
		ud.setModifiedOn(LocalDateTime.now());
		ud.setPassword(regSet.getPassword());
		ud.setRole(regSet.getRole());
		ud.setStatusId(regSet.getStatusId());
		ud.setUserName(regSet.getUserName());

		ude = userDataRepo.save(ud);

		lcDto.setActiveStatus(ude.getActiveStatus());
		lcDto.setEmailId(ude.getEmailId());
		lcDto.setFullName(ude.getFullName());
		lcDto.setPassword(ude.getPassword());
		lcDto.setRole(ude.getRole());
		lcDto.setStatusId(ude.getStatusId());
		lcDto.setUserId(ude.getUserUniqueId());
		lcDto.setUserName(ude.getUserName());

		return lcDto;
	}

}
