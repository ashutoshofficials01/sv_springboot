package com.app.sv_springboot.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dao.ResScoreRepo;
import com.app.sv_springboot.entities.Res_Score_Entity;
import com.app.sv_springboot.services.ScoreService;

import jakarta.annotation.PostConstruct;

@Service
public class ScoreServiceImpl implements ScoreService {

	// Inject property from application.properties
	@Value("${resUploadDir}")
	private String resUploadDir;

	@PostConstruct
	public void checkPath() {
		System.out.println("UPLOAD DIR = " + resUploadDir);
	}

	@Override
	public Map<String, Object> resumeUpload(MultipartFile file, String userId) {
		Map<String, Object> response = new HashMap<>();
//		Res_Score_Entity resExist = ResScoreRepo.findByUserId(userId);

		try {

			if (file.isEmpty()) {
				response.put("success", false);
				response.put("message", "Empty file");
				return response;
			}

			// Create upload directory if not exists
			Path uploadPath = Paths.get(resUploadDir).toAbsolutePath().normalize();
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// Unique filename
			String fileName = file.getOriginalFilename();
			Path filePath = uploadPath.resolve(fileName);

			// Save file
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// Calculate ATS score (your JSON logic)
			long atsScore = calculateATSScore(fileName);

			response.put("success", true);
			response.put("filename", file.getOriginalFilename());
			response.put("savedAs", fileName);
			response.put("path", filePath.toString());
			response.put("atsScore", atsScore);
			response.put("message", "Upload & scoring complete!");

		} catch (IOException e) {
			response.put("success", false);
			response.put("message", "File save failed: " + e.getMessage());
		}

		System.out.println("RESPONSE : " + response);

		// TODO Auto-generated method stub
		return response;
	}

	// TODO: Load JSON rules & score file content
	private long calculateATSScore(String fileName) {
		// Placeholder - parse saved file + apply JSON rules
		// Load your ats_scoring_rules.json
		return 85; // Mock for now
	}

}
