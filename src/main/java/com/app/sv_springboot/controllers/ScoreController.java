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
	public ResponseEntity<SendResponseDto> resumeUploadIT(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadIT(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadIT(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadConstruction")
	public ResponseEntity<SendResponseDto> resumeUploadConstruction(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadConstruction(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadConstruction(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadEducation")
	public ResponseEntity<SendResponseDto> resumeUploadEducation(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadEducation(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadEducation(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadFinance")
	public ResponseEntity<SendResponseDto> resumeUploadFinance(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadFinance(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadFinance(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadBusiness")
	public ResponseEntity<SendResponseDto> resumeUploadBusiness(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadBusiness(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadBusiness(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadTourism")
	public ResponseEntity<SendResponseDto> resumeUploadTourism(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadTourism(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadTourism(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadHealthcare")
	public ResponseEntity<SendResponseDto> resumeUploadHealthcare(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadHealthcare(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadHealthcare(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadLegal")
	public ResponseEntity<SendResponseDto> resumeUploadLegal(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadLegal(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadLegal(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadGovernment")
	public ResponseEntity<SendResponseDto> resumeUploadGovernment(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadGovernment(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadGovernment(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

	@PostMapping("/resumeUploadDesign")
	public ResponseEntity<SendResponseDto> resumeUploadDesign(@RequestParam("resume") MultipartFile file,
			@RequestParam("userId") String userId) {

		try {
			SendResponseDto result = scoreService.resumeUploadDesign(file, userId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			SendResponseDto error = scoreService.resumeUploadDesign(file, userId);
			return ResponseEntity.internalServerError().body(error);
		}

	}

}
