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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dao.ATSGeneralParamRepo;
import com.app.sv_springboot.dao.ResScoreRepo;
import com.app.sv_springboot.dao.UserDataRepo;
import com.app.sv_springboot.entities.ATS_General_Param_Entity;
import com.app.sv_springboot.entities.Res_Score_Entity;
import com.app.sv_springboot.services.ScoreService;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

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
	private ObjectMapper objectMapper;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	ResScoreRepo resScoreRepo;

	@Autowired
	UserDataRepo userDataRepo;

	@Autowired
	ATSGeneralParamRepo atsGeneralParamRepo;

	private List<Map<String, Object>> generalATSParameterList;

	private Map<String, Map<String, Object>> generalATSParameterMap;

	@PostConstruct
	public void loadGeneralATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:generalATSParameters.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalATSParameterList = (List<Map<String, Object>>) jsonData.get("generalATSParameter");

			// Create fast-lookup map
			generalATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalATSParameterList) {
				long atsGeneralId = (Long) param.get("atsGeneralId");
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalATSParameterMap.size());

		} catch (Exception e) {
			throw new RuntimeException("Failed to load generalATSParameters.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
		System.out.println("getAllGeneralATSParams() --> generalATSParameterList :: " + generalATSParameterList);
		return generalATSParameterList;
	}

	public Map<String, Object> getGeneralATSParam(long atsGeneralId, String atsParamId) {
		System.out.println("getGeneralATSParam(long " + atsGeneralId + ", String " + atsParamId
				+ ") --> generalATSParameterMap :: " + generalATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

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

		List<Map<String, Object>> generalAllATSParams = getAllGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAll();
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get(atsParamId));
				System.out.println("genATSParamId :: " + genATSParamId);

				switch (atsParamId) {

				case "ATS-001":
					if (atsParamId == genATSParamId) {

					}

					break;

				default:
					System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
				}
			}
		}

		// Placeholder - parse saved file + apply JSON rules
		// Load your ats_scoring_rules.json
		return 85; // Mock for now
	}

}
