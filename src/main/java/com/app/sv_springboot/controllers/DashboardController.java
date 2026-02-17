package com.app.sv_springboot.controllers;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import org.json.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dtos.ATSDataListDto;
import com.app.sv_springboot.dtos.SendResponseDto;
import com.app.sv_springboot.entities.User_Data_Entity;
import com.app.sv_springboot.services.DashboardService;

@RestController
public class DashboardController {

	private static Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	DashboardService dashService;

	@GetMapping("/fetchATSData")
	public ATSDataListDto fetchATSData() {
		try {
			return dashService.fetchATSData();
		} catch (Exception e) {
			ATSDataListDto userData = new ATSDataListDto();
			return userData;
		}
	}

}
