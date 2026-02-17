package com.app.sv_springboot.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.sv_springboot.dao.ResScoreRepo;
import com.app.sv_springboot.dao.UserDataRepo;
import com.app.sv_springboot.dtos.ATSDataListDto;
import com.app.sv_springboot.dtos.ATSResumeListDto;
import com.app.sv_springboot.dtos.ATSUserListDto;
import com.app.sv_springboot.entities.Res_Score_Entity;
import com.app.sv_springboot.entities.User_Data_Entity;
import com.app.sv_springboot.services.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	UserDataRepo userRepo;

	@Autowired
	ResScoreRepo resRepo;

	@Override
	public ATSDataListDto fetchATSData() {

		List<User_Data_Entity> userList = new ArrayList<>();
		List<Res_Score_Entity> resList = new ArrayList<>();

		List<ATSResumeListDto> rList = new ArrayList<>();
		ATSResumeListDto rDto = new ATSResumeListDto();

		List<ATSUserListDto> uList = new ArrayList<>();
		ATSUserListDto uDto = new ATSUserListDto();

		ATSDataListDto dataDto = new ATSDataListDto();

		try {
			userList = userRepo.findAll();
			resList = resRepo.findAll();

			for (User_Data_Entity usr : userList) {
				if (usr.getActiveStatus() == 1) {
					uDto = new ATSUserListDto();

					uDto.setActiveStatus(usr.getActiveStatus());
					uDto.setEmailId(usr.getEmailId());
					uDto.setFullName(usr.getFullName());
					uDto.setStatusId(usr.getStatusId());
					uDto.setRole(usr.getRole());
					uDto.setUserCreatedOn(formatDateTime(usr.getCreatedOn()));
					uDto.setUserModifiedOn(formatDateTime(usr.getModifiedOn()));
					uDto.setUserName(usr.getUserName());
					uDto.setUserRegistrationCount(0);

					uList.add(uDto);
				}
			}

			for (Res_Score_Entity res : resList) {
				if (res.getActiveStatus() == 1) {
					rDto = new ATSResumeListDto();

					rDto.setActiveStatus(res.getActiveStatus());
					rDto.setAtsScore(res.getAtsScore());
					rDto.setFileName(res.getFileName());
					rDto.setResumeUploadCount(0);
					rDto.setResUploadId(res.getResUploadId());
					rDto.setResCreatedOn(formatDateTime(res.getCreatedOn()));
					rDto.setResModifiedOn(formatDateTime(res.getModifiedOn()));
					rDto.setUserId(res.getUserId());

					rList.add(rDto);
				}
			}

			dataDto.setResumeDataList(rList);
			dataDto.setUserDataList(uList);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataDto;
	}

	private String formatDateTime(LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return dateTime.format(formatter);
	}

}
