package com.app.sv_springboot.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dao.ATSGeneralParamRepo;
import com.app.sv_springboot.dao.ResScoreRepo;
import com.app.sv_springboot.dao.UserDataRepo;
import com.app.sv_springboot.dtos.AtsListDto;
import com.app.sv_springboot.dtos.SendResponseDto;
import com.app.sv_springboot.entities.ATS_General_Param_Entity;
import com.app.sv_springboot.entities.Res_Score_Entity;
import com.app.sv_springboot.services.ScoreService;
import com.app.sv_springboot.util.GeneralATSUtil;

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
	ResScoreRepo resScoreRepo;

	@Autowired
	UserDataRepo userDataRepo;

	@Autowired
	ATSGeneralParamRepo atsGeneralParamRepo;

	@Autowired
	GeneralATSUtil genATSUtil;

	@Override
	public SendResponseDto resumeUpload(MultipartFile file, String userId) {
		Map<String, Object> response = new HashMap<>();
		List<AtsListDto> recievedRes = new ArrayList<>();

		SendResponseDto resp = new SendResponseDto();

//		Res_Score_Entity resExist = ResScoreRepo.findByUserId(userId);
		Res_Score_Entity saveRes = new Res_Score_Entity();
		String fileName = "";
		Path filePath;
		long atsScore = 0;
		Path uploadPath;

		try {

			if (file.isEmpty()) {
				resp.setAtsDataList(recievedRes);
				resp.setAtsScore(0L);
				resp.setFilename("Empty file");
				resp.setMessage("Empty file");
				resp.setPath("Empty path");
				resp.setSavedAs("Empty file");
				resp.setSuccess(false);

				return resp;
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
//			atsScore = genATSUtil.calculateATSScore(fileName, file);
			recievedRes = genATSUtil.calculateIT_ATSScore(fileName, file);

			for (AtsListDto rev : recievedRes) {
				if (rev.getAtsGeneralId() == 21) {
					atsScore = rev.getAtsScore();
				}
			}

			System.out.println("ATS SCORE :: " + atsScore);
			long rowsUpdated = resScoreRepo.updateFileNameByResUploadId(resStore.getResUploadId(), atsScore, fileName);

//			response.put("ATS_Data_List", recievedRes);
//			response.put("success", true);
//			response.put("filename", file.getOriginalFilename());
//			response.put("savedAs", fileName);
//			response.put("path", filePath.toString());
//			response.put("atsScore", atsScore);
//			response.put("message", "Upload & scoring complete!");

			resp.setAtsDataList(recievedRes);
			resp.setAtsScore(atsScore);
			resp.setFilename(file.getOriginalFilename());
			resp.setMessage("Upload & scoring complete!");
			resp.setPath(filePath.toString());
			resp.setSavedAs(fileName);
			resp.setSuccess(true);
			System.out.println("RESPONSE : " + resp);
			return resp;

		} catch (IOException e) {

			resp.setAtsDataList(null);
			resp.setAtsScore(0L);
			resp.setFilename("Error file");
			resp.setMessage("File save failed: " + e.getMessage());
			resp.setPath("Error path");
			resp.setSavedAs("Empty file");
			resp.setSuccess(false);

//			response.put("success", false);
//			response.put("message", "File save failed: " + e.getMessage());
			System.out.println("RESPONSE : " + resp);
			return resp;
		}

	}
}
