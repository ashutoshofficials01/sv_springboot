package com.app.sv_springboot.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dao.ResScoreRepo;
import com.app.sv_springboot.dao.UserDataRepo;
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

	@Autowired
	ResScoreRepo resScoreRepo;

	@Autowired
	UserDataRepo userDataRepo;

	@Override
	public Map<String, Object> resumeUpload(MultipartFile file, String userId) {
		Map<String, Object> response = new HashMap<>();
//		Res_Score_Entity resExist = ResScoreRepo.findByUserId(userId);
		Res_Score_Entity saveRes = new Res_Score_Entity();
		String fileName = "";
		Path filePath;
		long atsScore = 0;
		Path uploadPath;

		try {

			if (file.isEmpty()) {
				response.put("success", false);
				response.put("message", "Empty file");
				return response;
			}

			long usrId = Long.parseLong(userId);

			// Create upload directory if not exists
			uploadPath = Paths.get(resUploadDir).toAbsolutePath().normalize();
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			saveRes.setActiveStatus(1);
			saveRes.setAtsScore(atsScore);
			saveRes.setCreatedOn(LocalDateTime.now());
			saveRes.setModifiedOn(LocalDateTime.now());
			saveRes.setUserId(usrId);

			Res_Score_Entity resStore = resScoreRepo.save(saveRes);

			// Unique filename
			fileName = file.getOriginalFilename();
			fileName = resStore.getResUploadId() + "_" + fileName;
			filePath = uploadPath.resolve(fileName);

			// Save file
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// Calculate ATS score (your JSON logic)
			atsScore = calculateATSScore(fileName);

			long rowsUpdated = resScoreRepo.updateFileNameByResUploadId(resStore.getResUploadId(), atsScore, fileName);

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
		return response;
	}

	
	private long calculateATSScore(String fileName) {
		// Placeholder - parse saved file + apply JSON rules
		// Load your ats_scoring_rules.json
		return 85; // Mock for now
	}

}
