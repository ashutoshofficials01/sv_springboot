package com.app.sv_springboot.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dtos.SendResponseDto;
import com.app.sv_springboot.services.ScoreService;

@CrossOrigin(origins = "*")
@RestController
public class ScoreController {

	private static Logger logger = LoggerFactory.getLogger(ScoreController.class);

	@Autowired
	ScoreService scoreService;

	@PostMapping("/resumeUploadIT")
	public ResponseEntity<SendResponseDto> resumeUpload(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUpload(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUpload(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

}
