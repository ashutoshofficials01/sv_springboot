package com.app.sv_springboot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dao.ATSGeneralParamRepo;
import com.app.sv_springboot.dtos.AtsGenParamDto;
import com.app.sv_springboot.dtos.AtsListDto;
import com.app.sv_springboot.entities.ATS_General_Param_Entity;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Component
public class GeneralATSUtil {

	private final AtsGenParamDto atsGenParamDto;
	private final ITAtsUtil iTAtsUtil;

	private static Logger logger = LoggerFactory.getLogger(GeneralATSUtil.class);

	@Autowired
	ATSGeneralParamRepo atsGeneralParamRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	FileConversionUtil fileConUtil;

	@Autowired
	ExtraATSUtil extraATSUtil;

//	ITAtsUtil iTAtsUtil;

	private List<Map<String, Object>> generalATSParameterList;

	private Map<String, Map<String, Object>> generalATSParameterMap;

	GeneralATSUtil(AtsGenParamDto atsGenParamDto, ITAtsUtil iTAtsUtil) {
		this.atsGenParamDto = atsGenParamDto;
		this.iTAtsUtil = iTAtsUtil;
	}

	@PostConstruct
	public void loadGeneralATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:IT_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalATSParameterList = (List<Map<String, Object>>) jsonData.get("IT_ATSParameter");

			// Create fast-lookup map
			generalATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load generalATSParameters.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalATSParameterList :: " + generalATSParameterList);
		return generalATSParameterList;
	}

	public Map<String, Object> getGeneralATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalATSParameterMap :: " + generalATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	public List<AtsListDto> calculateIT_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_001_score = 0;
		long ats_002_score = 0;
		long ats_003_score = 0;
		long ats_004_score = 0;
		long ats_005_score = 0;
		long ats_006_score = 0;
		long ats_007_score = 0;
		long ats_008_score = 0;
		long ats_009_score = 0;
		long ats_010_score = 0;
		long ats_011_score = 0;
		long ats_012_score = 0;
		long ats_013_score = 0;
		long ats_014_score = 0;
		long ats_015_score = 0;
		long ats_016_score = 0;
		long ats_017_score = 0;
		long ats_018_score = 0;
		long ats_019_score = 0;
		long ats_020_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllATSParams = getAllGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAll();
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				System.out.println("genATSParamId :: " + genATSParamId);

				switch (atsParamId) {

				case "ATS-001":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_001_obj = iTAtsUtil.calculateATS001(atsParamId, generalATSId, fileName, file);

						if (ats_001_obj != null) {
							ats_001_score = ats_001_obj.getAtsScore();
							storeJSONs.add(ats_001_obj);
						}
//						JSONObject ats_001_obj = calculateATS001(atsParamId, generalATSId, fileName, file);
//						ats_001_score = ats_001_obj.getLong("ats001_points");
//						storeJSONs.add(ats_001_obj);

//						ats_001_score = calculateATS001(atsParamId, generalATSId, fileName, file);

					}
					break;

				case "ATS-002":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_002_obj = iTAtsUtil.calculateATS002(atsParamId, generalATSId, fileName, file);

						if (ats_002_obj != null) {
							ats_002_score = ats_002_obj.getAtsScore();
							storeJSONs.add(ats_002_obj);
						}
//						ats_002_score = calculateATS002(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_002_obj = calculateATS002(atsParamId, generalATSId, fileName, file);
//
//						ats_002_score = ats_002_obj.getLong("ats002_points");
//
//						storeJSONs.add(ats_002_obj);

					}

					break;

				case "ATS-003":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_003_obj = iTAtsUtil.calculateATS003(atsParamId, generalATSId, fileName, file);

						if (ats_003_obj != null) {
							ats_003_score = ats_003_obj.getAtsScore();
							storeJSONs.add(ats_003_obj);
						}
//						ats_003_score = calculateATS003(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_003_obj = calculateATS003(atsParamId, generalATSId, fileName, file);
//
//						ats_003_score = ats_003_obj.getLong("ats003_points");
//
//						storeJSONs.add(ats_003_obj);
					}

					break;

				case "ATS-004":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_004_obj = iTAtsUtil.calculateATS004(atsParamId, generalATSId, fileName, file);

						if (ats_004_obj != null) {
							ats_004_score = ats_004_obj.getAtsScore();
							storeJSONs.add(ats_004_obj);
						}
//						ats_004_score = calculateATS004(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_004_obj = calculateATS004(atsParamId, generalATSId, fileName, file);
//
//						ats_004_score = ats_004_obj.getLong("ats004_points");
//
//						storeJSONs.add(ats_004_obj);

					}
					break;

				case "ATS-005":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_005_obj = iTAtsUtil.calculateATS005(atsParamId, generalATSId, fileName, file);

						if (ats_005_obj != null) {
							ats_005_score = ats_005_obj.getAtsScore();
							storeJSONs.add(ats_005_obj);
						}
//						ats_005_score = calculateATS005(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_005_obj = calculateATS005(atsParamId, generalATSId, fileName, file);
//
//						ats_005_score = ats_005_obj.getLong("ats005_points");
//
//						storeJSONs.add(ats_005_obj);

					}

					break;

				case "ATS-006":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_006_obj = iTAtsUtil.calculateATS006(atsParamId, generalATSId, fileName, file);

						if (ats_006_obj != null) {
							ats_006_score = ats_006_obj.getAtsScore();
							storeJSONs.add(ats_006_obj);
						}

//						ats_006_score = calculateATS006(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_006_obj = calculateATS006(atsParamId, generalATSId, fileName, file);
//
//						ats_006_score = ats_006_obj.getLong("ats006_points");
//
//						storeJSONs.add(ats_006_obj);

					}

					break;

				case "ATS-007":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_007_obj = iTAtsUtil.calculateATS007(atsParamId, generalATSId, fileName, file);

						if (ats_007_obj != null) {
							ats_007_score = ats_007_obj.getAtsScore();
							storeJSONs.add(ats_007_obj);
						}
//						ats_007_score = calculateATS007(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_007_obj = calculateATS007(atsParamId, generalATSId, fileName, file);
//
//						ats_007_score = ats_007_obj.getLong("ats007_points");
//
//						storeJSONs.add(ats_007_obj);

					}

					break;

				case "ATS-008":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_008_obj = iTAtsUtil.calculateATS008(atsParamId, generalATSId, fileName, file);

						if (ats_008_obj != null) {
							ats_008_score = ats_008_obj.getAtsScore();
							storeJSONs.add(ats_008_obj);
						}
//						ats_008_score = calculateATS008(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_008_obj = calculateATS008(atsParamId, generalATSId, fileName, file);
//
//						ats_008_score = ats_008_obj.getLong("ats008_points");
//
//						storeJSONs.add(ats_008_obj);

					}
					break;

				case "ATS-009":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_009_obj = iTAtsUtil.calculateATS009(atsParamId, generalATSId, fileName, file);

						if (ats_009_obj != null) {
							ats_009_score = ats_009_obj.getAtsScore();
							storeJSONs.add(ats_009_obj);
						}
//						ats_009_score = calculateATS009(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_009_obj = calculateATS009(atsParamId, generalATSId, fileName, file);
//
//						ats_009_score = ats_009_obj.getLong("ats009_points");
//
//						storeJSONs.add(ats_009_obj);

					}

					break;

				case "ATS-010":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_010_obj = iTAtsUtil.calculateATS010(atsParamId, generalATSId, fileName, file);
						if (ats_010_obj != null) {
							ats_010_score = ats_010_obj.getAtsScore();
							storeJSONs.add(ats_010_obj);
						}
//						ats_010_score = calculateATS010(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_010_obj = calculateATS010(atsParamId, generalATSId, fileName, file);
//
//						ats_010_score = ats_010_obj.getLong("ats010_points");
//
//						storeJSONs.add(ats_010_obj);

					}

					break;

				case "ATS-011":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_011_obj = iTAtsUtil.calculateATS011(atsParamId, generalATSId, fileName, file);
						if (ats_011_obj != null) {
							ats_011_score = ats_011_obj.getAtsScore();
							storeJSONs.add(ats_011_obj);
						}
//						ats_011_score = calculateATS011(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_011_obj = calculateATS011(atsParamId, generalATSId, fileName, file);
//
//						ats_011_score = ats_011_obj.getLong("ats011_points");
//
//						storeJSONs.add(ats_011_obj);

					}
					break;

				case "ATS-012":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_012_obj = iTAtsUtil.calculateATS012(atsParamId, generalATSId, fileName, file);

						if (ats_012_obj != null) {
							ats_012_score = ats_012_obj.getAtsScore();
							storeJSONs.add(ats_012_obj);
						}
//						ats_012_score = calculateATS012(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_012_obj = calculateATS012(atsParamId, generalATSId, fileName, file);
//
//						ats_012_score = ats_012_obj.getLong("ats012_points");
//
//						storeJSONs.add(ats_012_obj);

					}

					break;

				case "ATS-013":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_013_obj = iTAtsUtil.calculateATS013(atsParamId, generalATSId, fileName, file);

						if (ats_013_obj != null) {
							ats_013_score = ats_013_obj.getAtsScore();
							storeJSONs.add(ats_013_obj);
						}
//						ats_013_score = calculateATS013(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_013_obj = calculateATS013(atsParamId, generalATSId, fileName, file);
//
//						ats_013_score = ats_013_obj.getLong("ats013_points");
//
//						storeJSONs.add(ats_013_obj);

					}

					break;

				case "ATS-014":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_014_obj = iTAtsUtil.calculateATS014(atsParamId, generalATSId, fileName, file);

						if (ats_014_obj != null) {
							ats_014_score = ats_014_obj.getAtsScore();
							storeJSONs.add(ats_014_obj);
						}
//						ats_014_score = calculateATS014(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_014_obj = calculateATS014(atsParamId, generalATSId, fileName, file);
//
//						ats_014_score = ats_014_obj.getLong("ats014_points");
//
//						storeJSONs.add(ats_014_obj);

					}
					break;

				case "ATS-015":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_015_obj = iTAtsUtil.calculateATS015(atsParamId, generalATSId, fileName, file);

						if (ats_015_obj != null) {
							ats_015_score = ats_015_obj.getAtsScore();
							storeJSONs.add(ats_015_obj);
						}
//						ats_015_score = calculateATS015(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_015_obj = calculateATS015(atsParamId, generalATSId, fileName, file);
//
//						ats_015_score = ats_015_obj.getLong("ats015_points");
//
//						storeJSONs.add(ats_015_obj);

					}

					break;

				case "ATS-016":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_016_obj = iTAtsUtil.calculateATS016(atsParamId, generalATSId, fileName, file);

						if (ats_016_obj != null) {
							ats_016_score = ats_016_obj.getAtsScore();
							storeJSONs.add(ats_016_obj);
						}
//						ats_016_score = calculateATS016(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_016_obj = calculateATS016(atsParamId, generalATSId, fileName, file);
//
//						ats_016_score = ats_016_obj.getLong("ats016_points");
//
//						storeJSONs.add(ats_016_obj);

					}

					break;

				case "ATS-017":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_017_obj = iTAtsUtil.calculateATS017(atsParamId, generalATSId, fileName, file);

						if (ats_017_obj != null) {
							ats_017_score = ats_017_obj.getAtsScore();
							storeJSONs.add(ats_017_obj);
						}
//						ats_017_score = calculateATS017(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_017_obj = calculateATS017(atsParamId, generalATSId, fileName, file);
//
//						ats_017_score = ats_017_obj.getLong("ats017_points");
//
//						storeJSONs.add(ats_017_obj);

					}

					break;

				case "ATS-018":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_018_obj = iTAtsUtil.calculateATS018(atsParamId, generalATSId, fileName, file);

						if (ats_018_obj != null) {
							ats_018_score = ats_018_obj.getAtsScore();
							storeJSONs.add(ats_018_obj);
						}
//						ats_018_score = calculateATS018(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_018_obj = calculateATS018(atsParamId, generalATSId, fileName, file);
//
//						ats_018_score = ats_018_obj.getLong("ats018_points");
//
//						storeJSONs.add(ats_018_obj);

					}
					break;

				case "ATS-019":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_019_obj = iTAtsUtil.calculateATS019(atsParamId, generalATSId, fileName, file);

						if (ats_019_obj != null) {
							ats_019_score = ats_019_obj.getAtsScore();
							storeJSONs.add(ats_019_obj);
						}
//						ats_019_score = calculateATS019(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_019_obj = calculateATS019(atsParamId, generalATSId, fileName, file);
//
//						ats_019_score = ats_019_obj.getLong("ats019_points");
//
//						storeJSONs.add(ats_019_obj);

					}

					break;

				case "ATS-020":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						AtsListDto ats_020_obj = iTAtsUtil.calculateATS020(atsParamId, generalATSId, fileName, file);

						if (ats_020_obj != null) {
							ats_020_score = ats_020_obj.getAtsScore();
							storeJSONs.add(ats_020_obj);
						}
//						ats_020_score = calculateATS020(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_020_obj = calculateATS020(atsParamId, generalATSId, fileName, file);
//
//						ats_020_score = ats_020_obj.getLong("ats020_points");
//
//						storeJSONs.add(ats_020_obj);

					}

					break;

				default:
					System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-001 => " + ats_001_score + "\nATS-002 => " + ats_002_score
				+ "\nATS-003 => " + ats_003_score + "\nATS-004 => " + ats_004_score + "\nATS-005 => " + ats_005_score
				+ "\nATS-006 => " + ats_006_score + "\nATS-007 => " + ats_007_score + "\nATS-008 => " + ats_008_score
				+ "\nATS-009 => " + ats_009_score + "\nATS-010 => " + ats_010_score + "\nATS-011 => " + ats_011_score
				+ "\nATS-012 => " + ats_012_score + "\nATS-013 => " + ats_013_score + "\nATS-014 => " + ats_014_score
				+ "\nATS-015 => " + ats_015_score + "\nATS-016 => " + ats_016_score + "\nATS-017 => " + ats_017_score
				+ "\nATS-018 => " + ats_018_score + "\nATS-019 => " + ats_019_score + "\nATS-020 => " + ats_020_score);

		ats_Combined_Score = ats_001_score + ats_002_score + ats_003_score + ats_004_score + ats_005_score
				+ ats_006_score + ats_007_score + ats_008_score + ats_009_score + ats_010_score + ats_011_score
				+ ats_012_score + ats_013_score + ats_014_score + ats_015_score + ats_016_score + ats_017_score
				+ ats_018_score + ats_019_score + ats_020_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 21;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

}
