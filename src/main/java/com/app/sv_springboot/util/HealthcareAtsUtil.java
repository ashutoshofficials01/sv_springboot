package com.app.sv_springboot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
public class HealthcareAtsUtil {

//	private final AtsGenParamDto atsGenParamDto;

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

	private List<Map<String, Object>> generalHealthcareATSParameterList;

	private Map<String, Map<String, Object>> generalHealthcareATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadHealthcare_ATSParameter() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Healthcare_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalHealthcareATSParameterList = (List<Map<String, Object>>) jsonData.get("Healthcare_ATSParameter");

			// Create fast-lookup map
			generalHealthcareATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalHealthcareATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalHealthcareATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalHealthcareATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Healthcare_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalHealthcareATSParameterList :: " + generalHealthcareATSParameterList);
		return generalHealthcareATSParameterList;
	}

	public Map<String, Object> getGeneralHealthcareATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralHealthcareATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalHealthcareATSParameterMap :: " + generalHealthcareATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalHealthcareATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	public AtsListDto calculateATS211(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts211 = new AtsListDto();
		AtsGenParamDto calAtsGen211 = new AtsGenParamDto();
		long ats211_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats211ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);
			if (ats211ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats211ParamData.get("logic_description");

			List<String> keywords = (List<String>) logicDescription.get("keywords");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_keyword_match_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_keyword_match_for_full_score")).longValue();

//	     4️ Read resume content
//			String resumeText = new String(file.getBytes());
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = resumeText.replaceAll("\\s+", " ").trim();

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

//	     5️ Keyword matching
			long matchCount = 0;

			for (String keyword : keywords) {
				String key = caseSensitive ? keyword : keyword.toLowerCase();
				if (resumeText.contains(key)) {
					System.out.println("ATS-211 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats211_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats211_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats211_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats211_points == max) {
				paramType = "positive";
			} else if (ats211_points == partial) {
				paramType = "partial";
			} else if (ats211_points == 0) {
				paramType = "negative";
			}

			calAtsGen211.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen211.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen211.setCategory(genATSParamData.getCategory());
			calAtsGen211.setDescription(genATSParamData.getDescription());
			calAtsGen211.setMax_points(genATSParamData.getMax_points());
			calAtsGen211.setParameter(genATSParamData.getParameter());
			calAtsGen211.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen211.setTotal_points(genATSParamData.getTotal_points());

			calAts211.setAtsGeneralId(atsGeneralId);
			calAts211.setAtsGeneralParamDto(calAtsGen211);
			calAts211.setAtsParamData(ats211ParamData);
			calAts211.setAtsParamId(atsParamId);
			calAts211.setAtsParamType(paramType);
			calAts211.setAtsScore(ats211_points);

			return calAts211;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts211;
		}
	}

	public AtsListDto calculateATS212(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts212 = new AtsListDto();
		AtsGenParamDto calAtsGen212 = new AtsGenParamDto();
		long ats212_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats212ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats212ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats212ParamData.get("logic_description");

			Map<String, List<String>> skillCategories = (Map<String, List<String>>) logicDescription
					.get("skill_categories");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_category_match_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_category_match_for_full_score")).longValue();

			// 4️ Read resume content
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = resumeText.replaceAll("\\s+", " ").trim();

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5️ Category matching
			long matchedCategories = 0;

			for (Map.Entry<String, List<String>> entry : skillCategories.entrySet()) {
				for (String keyword : entry.getValue()) {

					String key = caseSensitive ? keyword : keyword.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-212 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats212_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats212_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats212_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats212_points == max) {
				paramType = "positive";
			} else if (ats212_points == partial) {
				paramType = "partial";
			} else if (ats212_points == 0) {
				paramType = "negative";
			}

			calAtsGen212.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen212.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen212.setCategory(genATSParamData.getCategory());
			calAtsGen212.setDescription(genATSParamData.getDescription());
			calAtsGen212.setMax_points(genATSParamData.getMax_points());
			calAtsGen212.setParameter(genATSParamData.getParameter());
			calAtsGen212.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen212.setTotal_points(genATSParamData.getTotal_points());

			calAts212.setAtsGeneralId(atsGeneralId);
			calAts212.setAtsGeneralParamDto(calAtsGen212);
			calAts212.setAtsParamData(ats212ParamData);
			calAts212.setAtsParamId(atsParamId);
			calAts212.setAtsParamType(paramType);
			calAts212.setAtsScore(ats212_points);

			return calAts212;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts212;
		}
	}

	public AtsListDto calculateATS213(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts213 = new AtsListDto();
		AtsGenParamDto calAtsGen213 = new AtsGenParamDto();
		long ats213_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats213ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats213ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats213ParamData.get("logic_description");

			Map<String, List<String>> keywordVariations = (Map<String, List<String>>) logicDescription
					.get("keyword_variations");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_variation_match_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_variation_match_for_full_score")).longValue();

			// 4 Read resume content
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = (resumeText.replaceAll("\\s+", " ").trim());

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5
			long matchedVariationGroups = 0;

			for (Map.Entry<String, List<String>> entry : keywordVariations.entrySet()) {

				List<String> variants = entry.getValue();

				if (variants == null || variants.size() < 2) {
					continue;
				}

				String correctWord = caseSensitive ? variants.get(0) : variants.get(0).toLowerCase();

				boolean canonicalPresent = resumeText.contains(correctWord);

				boolean alternatePresent = false;

				for (int i = 1; i < variants.size(); i++) {
					String alternate = caseSensitive ? variants.get(i) : variants.get(i).toLowerCase();
					if (resumeText.contains(" " + alternate + " ")) {
						System.out.println("ATS-213 :: " + alternate);
						alternatePresent = true;
						break;
					}
				}

				if (alternatePresent) {
					matchedVariationGroups++;
				}
			}

			// 6
			if (matchedVariationGroups > minFull) {
				ats213_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats213_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats213_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats213_points == max) {
				paramType = "positive";
			} else if (ats213_points == partial) {
				paramType = "partial";
			} else if (ats213_points == 0) {
				paramType = "negative";
			}

			calAtsGen213.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen213.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen213.setCategory(genATSParamData.getCategory());
			calAtsGen213.setDescription(genATSParamData.getDescription());
			calAtsGen213.setMax_points(genATSParamData.getMax_points());
			calAtsGen213.setParameter(genATSParamData.getParameter());
			calAtsGen213.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen213.setTotal_points(genATSParamData.getTotal_points());

			calAts213.setAtsGeneralId(atsGeneralId);
			calAts213.setAtsGeneralParamDto(calAtsGen213);
			calAts213.setAtsParamData(ats213ParamData);
			calAts213.setAtsParamId(atsParamId);
			calAts213.setAtsParamType(paramType);
			calAts213.setAtsScore(ats213_points);

			return calAts213;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts213;
		}
	}

	public AtsListDto calculateATS214(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts214 = new AtsListDto();
		AtsGenParamDto calAtsGen214 = new AtsGenParamDto();
		long ats214_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats214ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats214ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats214ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-214 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats214_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats214_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats214_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats214_points == max) {
				paramType = "positive";
			} else if (ats214_points == partial) {
				paramType = "partial";
			} else if (ats214_points == 0) {
				paramType = "negative";
			}

			calAtsGen214.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen214.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen214.setCategory(genATSParamData.getCategory());
			calAtsGen214.setDescription(genATSParamData.getDescription());
			calAtsGen214.setMax_points(genATSParamData.getMax_points());
			calAtsGen214.setParameter(genATSParamData.getParameter());
			calAtsGen214.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen214.setTotal_points(genATSParamData.getTotal_points());

			calAts214.setAtsGeneralId(atsGeneralId);
			calAts214.setAtsGeneralParamDto(calAtsGen214);
			calAts214.setAtsParamData(ats214ParamData);
			calAts214.setAtsParamId(atsParamId);
			calAts214.setAtsParamType(paramType);
			calAts214.setAtsScore(ats214_points);

			return calAts214;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts214;
		}
	}

	public AtsListDto calculateATS215(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts215 = new AtsListDto();
		AtsGenParamDto calAtsGen215 = new AtsGenParamDto();
		long ats215_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats215ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats215ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats215ParamData.get("logic_description");

			Map<String, List<String>> layoutIndicators = (Map<String, List<String>>) logicDescription
					.get("layout_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			long maxFull = ((Number) matchingRules.get("max_allowed_layout_violations_for_full_score")).longValue();

			long maxPartial = ((Number) matchingRules.get("max_allowed_layout_violations_for_partial_score"))
					.longValue();

			// 4️ Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = resumeText.replaceAll("\\s+", " ").trim();

			// 5️ Count layout violations
			long layoutViolations = 0;

			for (List<String> indicators : layoutIndicators.values()) {
				for (String indicator : indicators) {
					if (resumeText.contains(indicator)) {
						System.out.println("ATS-215 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats215_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats215_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats215_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats215_points == max) {
				paramType = "positive";
			} else if (ats215_points == partial) {
				paramType = "partial";
			} else if (ats215_points == 0) {
				paramType = "negative";
			}

			calAtsGen215.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen215.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen215.setCategory(genATSParamData.getCategory());
			calAtsGen215.setDescription(genATSParamData.getDescription());
			calAtsGen215.setMax_points(genATSParamData.getMax_points());
			calAtsGen215.setParameter(genATSParamData.getParameter());
			calAtsGen215.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen215.setTotal_points(genATSParamData.getTotal_points());

			calAts215.setAtsGeneralId(atsGeneralId);
			calAts215.setAtsGeneralParamDto(calAtsGen215);
			calAts215.setAtsParamData(ats215ParamData);
			calAts215.setAtsParamId(atsParamId);
			calAts215.setAtsParamType(paramType);
			calAts215.setAtsScore(ats215_points);

			return calAts215;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts215;
		}
	}

	public AtsListDto calculateATS216(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts216 = new AtsListDto();
		AtsGenParamDto calAtsGen216 = new AtsGenParamDto();
		long ats216_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats216ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats216ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats216ParamData.get("logic_description");

			Map<String, Object> fontRules = (Map<String, Object>) logicDescription.get("font_rules");

			List<String> standardFonts = (List<String>) fontRules.get("standard_fonts");

			List<String> nonStandardFonts = (List<String>) fontRules.get("non_standard_fonts");

			// 4 Extract matching_rules
			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			boolean fullScoreIfStandard = (boolean) matchingRules.get("full_score_if_only_standard_fonts");

			boolean partialScoreIfMixed = (boolean) matchingRules.get("partial_score_if_mixed_fonts");

			boolean zeroScoreIfNonStandard = (boolean) matchingRules.get("zero_score_if_only_non_standard_fonts");

			long fullScore = genATSParamData.getMax_points();
			long partialScore = genATSParamData.getPenalty_points();
			long zeroScore = 0;

			// 5 Extract fonts from resume
			Set<String> detectedFonts = extraATSUtil.extractFontsFromResume(file);

			System.out.println("ATS-216 :: " + detectedFonts);

			// 6 Fonts cannot be detected
			if (detectedFonts == null || detectedFonts.isEmpty()) {
				AtsListDto calAtsNull = new AtsListDto();
				AtsGenParamDto calAtsGenNull = new AtsGenParamDto();

				calAtsGenNull.setAtsGeneralId(genATSParamData.getAtsGeneralId());
				calAtsGenNull.setAtsParamId(genATSParamData.getAtsParamId());
				calAtsGenNull.setCategory(genATSParamData.getCategory());
				calAtsGenNull.setDescription(genATSParamData.getDescription());
				calAtsGenNull.setMax_points(genATSParamData.getMax_points());
				calAtsGenNull.setParameter(genATSParamData.getParameter());
				calAtsGenNull.setPenalty_points(genATSParamData.getPenalty_points());
				calAtsGenNull.setTotal_points(genATSParamData.getTotal_points());

				calAtsNull.setAtsGeneralId(atsGeneralId);
				calAtsNull.setAtsGeneralParamDto(calAtsGenNull);
				calAtsNull.setAtsParamData(ats216ParamData);
				calAtsNull.setAtsParamId(atsParamId);
				calAtsNull.setAtsParamType("negative");
				calAtsNull.setAtsScore(0);

				return calAtsNull;
			}

			boolean hasStandard = false;
			boolean hasNonStandard = false;

			for (String font : detectedFonts) {
				String fontName = caseSensitive ? font : font.toLowerCase();

				if (standardFonts.contains(fontName)) {
					hasStandard = true;
				}

				if (nonStandardFonts.contains(fontName)) {
					hasNonStandard = true;
				}
			}

			// 7️ Scoring logic (CONFIG DRIVEN)
			if (hasNonStandard && !hasStandard && zeroScoreIfNonStandard) {
				ats216_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats216_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats216_points = fullScore;
			} else {
				ats216_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats216_points == max) {
				paramType = "positive";
			} else if (ats216_points == partial) {
				paramType = "partial";
			} else if (ats216_points == 0) {
				paramType = "negative";
			}

			calAtsGen216.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen216.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen216.setCategory(genATSParamData.getCategory());
			calAtsGen216.setDescription(genATSParamData.getDescription());
			calAtsGen216.setMax_points(genATSParamData.getMax_points());
			calAtsGen216.setParameter(genATSParamData.getParameter());
			calAtsGen216.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen216.setTotal_points(genATSParamData.getTotal_points());

			calAts216.setAtsGeneralId(atsGeneralId);
			calAts216.setAtsGeneralParamDto(calAtsGen216);
			calAts216.setAtsParamData(ats216ParamData);
			calAts216.setAtsParamId(atsParamId);
			calAts216.setAtsParamType(paramType);
			calAts216.setAtsScore(ats216_points);

			return calAts216;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts216;
		}
	}

	public AtsListDto calculateATS217(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts217 = new AtsListDto();
		AtsGenParamDto calAtsGen217 = new AtsGenParamDto();
		long ats217_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats217ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats217ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats217ParamData.get("logic_description");

			List<String> sections = (List<String>) logicDescription.get("sections");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_sections_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_sections_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Detect sections
			Set<String> matchedSections = new HashSet<>();

			for (String section : sections) {
				String sectionKey = caseSensitive ? section : section.toLowerCase();

				// Match section as heading or line start
				if (resumeText.contains("\n" + sectionKey) || resumeText.startsWith(sectionKey)
						|| resumeText.contains(sectionKey + "\n") || resumeText.startsWith(" " + sectionKey + " ")
						|| resumeText.startsWith("\n" + sectionKey + "\n")) {
					System.out.println("ATS-217 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats217_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats217_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats217_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats217_points == max) {
				paramType = "positive";
			} else if (ats217_points == partial) {
				paramType = "partial";
			} else if (ats217_points == 0) {
				paramType = "negative";
			}

			calAtsGen217.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen217.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen217.setCategory(genATSParamData.getCategory());
			calAtsGen217.setDescription(genATSParamData.getDescription());
			calAtsGen217.setMax_points(genATSParamData.getMax_points());
			calAtsGen217.setParameter(genATSParamData.getParameter());
			calAtsGen217.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen217.setTotal_points(genATSParamData.getTotal_points());

			calAts217.setAtsGeneralId(atsGeneralId);
			calAts217.setAtsGeneralParamDto(calAtsGen217);
			calAts217.setAtsParamData(ats217ParamData);
			calAts217.setAtsParamId(atsParamId);
			calAts217.setAtsParamType(paramType);
			calAts217.setAtsScore(ats217_points);

			return calAts217;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts217;
		}
	}

	public AtsListDto calculateATS218(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts218 = new AtsListDto();
		AtsGenParamDto calAtsGen218 = new AtsGenParamDto();
		long ats218_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats218ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats218ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats218ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long maxFull = ((Number) matchingRules.get("max_order_violations_for_full_score")).longValue();

			long maxPartial = ((Number) matchingRules.get("max_order_violations_for_partial_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Detect section positions (ONE per section group)
			List<Long> detectedSectionPositions = new ArrayList<>();

			for (Map.Entry<String, List<String>> sectionGroup : acceptedFormats.entrySet()) {

				long earliestIndexForGroup = -1;

				for (String heading : sectionGroup.getValue()) {

					String key = caseSensitive ? heading : heading.toLowerCase();
					long index = resumeText.indexOf("\n" + key);

					// Handle heading at very beginning
					if (index == -1 && resumeText.startsWith(key)) {
						System.out.println("ATS-218 :: " + key);
						index = 0;
					}

					if (index != -1) {
						if (earliestIndexForGroup == -1 || index < earliestIndexForGroup) {
							earliestIndexForGroup = index;
						}
					}
				}

				// Add ONLY ONE position per section group
				if (earliestIndexForGroup != -1) {
					detectedSectionPositions.add(earliestIndexForGroup);
				}
			}

			// Less than 2 detected logical sections → ordering meaningless
			if (detectedSectionPositions.size() < 2) {
				return null;
			}

			// 6️ Count ordering violations
			long violations = 0;
			long previousIndex = detectedSectionPositions.get(0);

			for (int i = 1; i < detectedSectionPositions.size(); i++) {
				long currentIndex = detectedSectionPositions.get(i);

				if (currentIndex < previousIndex) {
					violations++;
				}

				previousIndex = currentIndex;
			}

			// 7️ Scoring
			if (violations <= maxFull) {
				ats218_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats218_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats218_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats218_points == max) {
				paramType = "positive";
			} else if (ats218_points == partial) {
				paramType = "partial";
			} else if (ats218_points == 0) {
				paramType = "negative";
			}

			calAtsGen218.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen218.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen218.setCategory(genATSParamData.getCategory());
			calAtsGen218.setDescription(genATSParamData.getDescription());
			calAtsGen218.setMax_points(genATSParamData.getMax_points());
			calAtsGen218.setParameter(genATSParamData.getParameter());
			calAtsGen218.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen218.setTotal_points(genATSParamData.getTotal_points());

			calAts218.setAtsGeneralId(atsGeneralId);
			calAts218.setAtsGeneralParamDto(calAtsGen218);
			calAts218.setAtsParamData(ats218ParamData);
			calAts218.setAtsParamId(atsParamId);
			calAts218.setAtsParamType(paramType);
			calAts218.setAtsScore(ats218_points);

			return calAts218;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts218;
		}
	}

	public AtsListDto calculateATS219(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts219 = new AtsListDto();
		AtsGenParamDto calAtsGen219 = new AtsGenParamDto();
		long ats219_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats219ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats219ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats219ParamData.get("logic_description");

			Map<String, List<String>> experienceIndicators = (Map<String, List<String>>) logicDescription
					.get("experience_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_matches_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_matches_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count experience matches
			long matchCount = 0;

			for (List<String> keywords : experienceIndicators.values()) {
				for (String keyword : keywords) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();
					if (resumeText.contains(key)) {
						System.out.println("ATS-219 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats219_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats219_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats219_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats219_points == max) {
				paramType = "positive";
			} else if (ats219_points == partial) {
				paramType = "partial";
			} else if (ats219_points == 0) {
				paramType = "negative";
			}

			calAtsGen219.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen219.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen219.setCategory(genATSParamData.getCategory());
			calAtsGen219.setDescription(genATSParamData.getDescription());
			calAtsGen219.setMax_points(genATSParamData.getMax_points());
			calAtsGen219.setParameter(genATSParamData.getParameter());
			calAtsGen219.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen219.setTotal_points(genATSParamData.getTotal_points());

			calAts219.setAtsGeneralId(atsGeneralId);
			calAts219.setAtsGeneralParamDto(calAtsGen219);
			calAts219.setAtsParamData(ats219ParamData);
			calAts219.setAtsParamId(atsParamId);
			calAts219.setAtsParamType(paramType);
			calAts219.setAtsScore(ats219_points);

			return calAts219;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts219;
		}
	}

	public AtsListDto calculateATS220(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts220 = new AtsListDto();
		AtsGenParamDto calAtsGen220 = new AtsGenParamDto();
		long ats220_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats220ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats220ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats220ParamData.get("logic_description");

			List<String> recognizedTitles = (List<String>) logicDescription.get("recognized_titles");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_title_match_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_title_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count distinct job title matches
			long titleMatchCount = 0;

			for (String title : recognizedTitles) {
				String key = caseSensitive ? title : title.toLowerCase();
				if (resumeText.contains(key)) {
					System.out.println("ATS-220 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats220_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats220_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats220_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats220_points == max) {
				paramType = "positive";
			} else if (ats220_points == partial) {
				paramType = "partial";
			} else if (ats220_points == 0) {
				paramType = "negative";
			}

			calAtsGen220.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen220.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen220.setCategory(genATSParamData.getCategory());
			calAtsGen220.setDescription(genATSParamData.getDescription());
			calAtsGen220.setMax_points(genATSParamData.getMax_points());
			calAtsGen220.setParameter(genATSParamData.getParameter());
			calAtsGen220.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen220.setTotal_points(genATSParamData.getTotal_points());

			calAts220.setAtsGeneralId(atsGeneralId);
			calAts220.setAtsGeneralParamDto(calAtsGen220);
			calAts220.setAtsParamData(ats220ParamData);
			calAts220.setAtsParamId(atsParamId);
			calAts220.setAtsParamType(paramType);
			calAts220.setAtsScore(ats220_points);

			return calAts220;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts220;
		}
	}

	public AtsListDto calculateATS221(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts221 = new AtsListDto();
		AtsGenParamDto calAtsGen221 = new AtsGenParamDto();
		long ats221_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats221ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats221ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats221ParamData.get("logic_description");

			List<String> datePatterns = (List<String>) logicDescription.get("date_patterns");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minMatches = ((Number) matchingRules.get("minimum_date_matches_for_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Regex patterns for common date formats
			List<String> regexPatterns = Arrays.asList(
					"(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+\\d{4}", "(\\d{2}/\\d{4})",
					"(\\d{4}-\\d{2})", "\\b\\d{4}\\b", "(\\d{4}\\s*-\\s*\\d{4})", "(\\d{4}\\s*-\\s*present)",
					"((jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+\\d{4}\\s*-\\s*(present|(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+\\d{4}))");

			long dateMatchCount = 0;

			for (String regex : regexPatterns) {
				if (resumeText.matches("(?s).*" + regex + ".*")) {
					System.out.println("ATS-221 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats221_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats221_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats221_points == max) {
				paramType = "positive";
			} else if (ats221_points == partial) {
				paramType = "partial";
			} else if (ats221_points == 0) {
				paramType = "negative";
			}

			calAtsGen221.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen221.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen221.setCategory(genATSParamData.getCategory());
			calAtsGen221.setDescription(genATSParamData.getDescription());
			calAtsGen221.setMax_points(genATSParamData.getMax_points());
			calAtsGen221.setParameter(genATSParamData.getParameter());
			calAtsGen221.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen221.setTotal_points(genATSParamData.getTotal_points());

			calAts221.setAtsGeneralId(atsGeneralId);
			calAts221.setAtsGeneralParamDto(calAtsGen221);
			calAts221.setAtsParamData(ats221ParamData);
			calAts221.setAtsParamId(atsParamId);
			calAts221.setAtsParamType(paramType);
			calAts221.setAtsScore(ats221_points);

			return calAts221;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts221;
		}
	}

	public AtsListDto calculateATS222(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts222 = new AtsListDto();
		AtsGenParamDto calAtsGen222 = new AtsGenParamDto();
		long ats222_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats222ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats222ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats222ParamData.get("logic_description");

			Map<String, List<String>> impactIndicators = (Map<String, List<String>>) logicDescription
					.get("impact_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_matches_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_matches_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count quantified impact matches
			long impactMatchCount = 0;

			for (List<String> indicators : impactIndicators.values()) {
				for (String indicator : indicators) {
					String key = caseSensitive ? indicator : indicator.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-222 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 521ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats222_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats222_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats222_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats222_points == max) {
				paramType = "positive";
			} else if (ats222_points == partial) {
				paramType = "partial";
			} else if (ats222_points == 0) {
				paramType = "negative";
			}

			calAtsGen222.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen222.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen222.setCategory(genATSParamData.getCategory());
			calAtsGen222.setDescription(genATSParamData.getDescription());
			calAtsGen222.setMax_points(genATSParamData.getMax_points());
			calAtsGen222.setParameter(genATSParamData.getParameter());
			calAtsGen222.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen222.setTotal_points(genATSParamData.getTotal_points());

			calAts222.setAtsGeneralId(atsGeneralId);
			calAts222.setAtsGeneralParamDto(calAtsGen222);
			calAts222.setAtsParamData(ats222ParamData);
			calAts222.setAtsParamId(atsParamId);
			calAts222.setAtsParamType(paramType);
			calAts222.setAtsScore(ats222_points);

			return calAts222;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts222;
		}
	}

	public AtsListDto calculateATS223(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts223 = new AtsListDto();
		AtsGenParamDto calAtsGen223 = new AtsGenParamDto();
		long ats223_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats223ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats223ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats223ParamData.get("logic_description");

			Map<String, List<String>> skillGroups = (Map<String, List<String>>) logicDescription.get("skill_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched skill groups (NOT individual keywords)
			long matchedGroups = 0;

			for (List<String> skills : skillGroups.values()) {
				boolean groupMatched = false;

				for (String skill : skills) {
					String key = caseSensitive ? skill : skill.toLowerCase();
					if (resumeText.contains(key)) {
						System.out.println("ATS-223 :: " + key);
						groupMatched = true;
						break; // only count this group once
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats223_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats223_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats223_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats223_points == max) {
				paramType = "positive";
			} else if (ats223_points == partial) {
				paramType = "partial";
			} else if (ats223_points == 0) {
				paramType = "negative";
			}

			calAtsGen223.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen223.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen223.setCategory(genATSParamData.getCategory());
			calAtsGen223.setDescription(genATSParamData.getDescription());
			calAtsGen223.setMax_points(genATSParamData.getMax_points());
			calAtsGen223.setParameter(genATSParamData.getParameter());
			calAtsGen223.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen223.setTotal_points(genATSParamData.getTotal_points());

			calAts223.setAtsGeneralId(atsGeneralId);
			calAts223.setAtsGeneralParamDto(calAtsGen223);
			calAts223.setAtsParamData(ats223ParamData);
			calAts223.setAtsParamId(atsParamId);
			calAts223.setAtsParamType(paramType);
			calAts223.setAtsScore(ats223_points);

			return calAts223;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts223;
		}
	}

	public AtsListDto calculateATS224(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts224 = new AtsListDto();
		AtsGenParamDto calAtsGen224 = new AtsGenParamDto();
		long ats224_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats224ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats224ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats224ParamData.get("logic_description");

			Map<String, List<String>> practiceGroups = (Map<String, List<String>>) logicDescription
					.get("practice_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (Boolean) matchingRules.getOrDefault("case_sensitive", false);

			long minPartial = ((Number) matchingRules.get("minimum_practice_group_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_practice_group_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Detect matched practice groups (each group counted once)
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> group : practiceGroups.entrySet()) {
				for (String keyword : group.getValue()) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();
					if (resumeText.contains(key)) {
						System.out.println("ATS-224 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats224_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats224_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats224_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats224_points == max) {
				paramType = "positive";
			} else if (ats224_points == partial) {
				paramType = "partial";
			} else if (ats224_points == 0) {
				paramType = "negative";
			}

			calAtsGen224.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen224.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen224.setCategory(genATSParamData.getCategory());
			calAtsGen224.setDescription(genATSParamData.getDescription());
			calAtsGen224.setMax_points(genATSParamData.getMax_points());
			calAtsGen224.setParameter(genATSParamData.getParameter());
			calAtsGen224.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen224.setTotal_points(genATSParamData.getTotal_points());

			calAts224.setAtsGeneralId(atsGeneralId);
			calAts224.setAtsGeneralParamDto(calAtsGen224);
			calAts224.setAtsParamData(ats224ParamData);
			calAts224.setAtsParamId(atsParamId);
			calAts224.setAtsParamType(paramType);
			calAts224.setAtsScore(ats224_points);

			return calAts224;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts224;
		}
	}

	public AtsListDto calculateATS225(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts225 = new AtsListDto();
		AtsGenParamDto calAtsGen225 = new AtsGenParamDto();
		long ats225_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats225ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats225ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats225ParamData.get("logic_description");

			Map<String, List<String>> educationGroups = (Map<String, List<String>>) logicDescription
					.get("education_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartialGroups = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();

			long minFullGroups = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched education groups
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> groupEntry : educationGroups.entrySet()) {
				boolean groupMatched = false;

				for (String keyword : groupEntry.getValue()) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-225 :: " + key);
						groupMatched = true;
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats225_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats225_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats225_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats225_points == max) {
				paramType = "positive";
			} else if (ats225_points == partial) {
				paramType = "partial";
			} else if (ats225_points == 0) {
				paramType = "negative";
			}

			calAtsGen225.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen225.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen225.setCategory(genATSParamData.getCategory());
			calAtsGen225.setDescription(genATSParamData.getDescription());
			calAtsGen225.setMax_points(genATSParamData.getMax_points());
			calAtsGen225.setParameter(genATSParamData.getParameter());
			calAtsGen225.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen225.setTotal_points(genATSParamData.getTotal_points());

			calAts225.setAtsGeneralId(atsGeneralId);
			calAts225.setAtsGeneralParamDto(calAtsGen225);
			calAts225.setAtsParamData(ats225ParamData);
			calAts225.setAtsParamId(atsParamId);
			calAts225.setAtsParamType(paramType);
			calAts225.setAtsScore(ats225_points);

			return calAts225;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts225;
		}
	}

	public AtsListDto calculateATS226(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts226 = new AtsListDto();
		AtsGenParamDto calAtsGen226 = new AtsGenParamDto();
		long ats226_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats226ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats226ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats226ParamData.get("logic_description");

			Map<String, List<String>> clarityGroups = (Map<String, List<String>>) logicDescription
					.get("clarity_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartialGroups = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();

			long minFullGroups = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched clarity groups
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> groupEntry : clarityGroups.entrySet()) {
				boolean groupMatched = false;

				for (String keyword : groupEntry.getValue()) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-226 :: " + key);
						groupMatched = true;
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats226_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats226_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats226_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats226_points == max) {
				paramType = "positive";
			} else if (ats226_points == partial) {
				paramType = "partial";
			} else if (ats226_points == 0) {
				paramType = "negative";
			}

			calAtsGen226.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen226.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen226.setCategory(genATSParamData.getCategory());
			calAtsGen226.setDescription(genATSParamData.getDescription());
			calAtsGen226.setMax_points(genATSParamData.getMax_points());
			calAtsGen226.setParameter(genATSParamData.getParameter());
			calAtsGen226.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen226.setTotal_points(genATSParamData.getTotal_points());

			calAts226.setAtsGeneralId(atsGeneralId);
			calAts226.setAtsGeneralParamDto(calAtsGen226);
			calAts226.setAtsParamData(ats226ParamData);
			calAts226.setAtsParamId(atsParamId);
			calAts226.setAtsParamType(paramType);
			calAts226.setAtsScore(ats226_points);

			return calAts226;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts226;
		}
	}

	public AtsListDto calculateATS227(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts227 = new AtsListDto();
		AtsGenParamDto calAtsGen227 = new AtsGenParamDto();
		long ats227_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats227ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats227ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats227ParamData.get("logic_description");

			Map<String, List<String>> actionVerbGroups = (Map<String, List<String>>) logicDescription
					.get("action_verb_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = Boolean.TRUE.equals(matchingRules.get("case_sensitive"));

			long minPartialGroups = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();

			long minFullGroups = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.trim().isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched action-verb groups (max 1 per group)
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> groupEntry : actionVerbGroups.entrySet()) {

				boolean groupMatched = false;

				for (String verb : groupEntry.getValue()) {
					String keyword = caseSensitive ? verb : verb.toLowerCase();

					// word-boundary safe match
					if (resumeText.matches("(?s).*\\b" + Pattern.quote(keyword) + "\\b.*")) {
						groupMatched = true;
						System.out.println("ATS-227 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats227_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats227_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats227_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats227_points == max) {
				paramType = "positive";
			} else if (ats227_points == partial) {
				paramType = "partial";
			} else if (ats227_points == 0) {
				paramType = "negative";
			}

			calAtsGen227.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen227.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen227.setCategory(genATSParamData.getCategory());
			calAtsGen227.setDescription(genATSParamData.getDescription());
			calAtsGen227.setMax_points(genATSParamData.getMax_points());
			calAtsGen227.setParameter(genATSParamData.getParameter());
			calAtsGen227.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen227.setTotal_points(genATSParamData.getTotal_points());

			calAts227.setAtsGeneralId(atsGeneralId);
			calAts227.setAtsGeneralParamDto(calAtsGen227);
			calAts227.setAtsParamData(ats227ParamData);
			calAts227.setAtsParamId(atsParamId);
			calAts227.setAtsParamType(paramType);
			calAts227.setAtsScore(ats227_points);

			return calAts227;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts227;
		}
	}

	public AtsListDto calculateATS228(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts228 = new AtsListDto();
		AtsGenParamDto calAtsGen228 = new AtsGenParamDto();
		long ats228_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats228ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats228ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats228ParamData.get("logic_description");
			Map<String, Object> errorRules = (Map<String, Object>) logicDesc.get("error_detection_rules");
			Map<String, Object> scoringRules = (Map<String, Object>) logicDesc.get("scoring_rules");

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			resumeText = resumeText.toLowerCase();

			// 5 Count all error patterns
			long totalErrors = 0;

			for (Object ruleListObj : errorRules.values()) {
				List<String> ruleList = (List<String>) ruleListObj;
				for (String errorPattern : ruleList) {
					if (resumeText.contains(" " + errorPattern.toLowerCase() + " ")) {
						System.out.println("ATS-228 :: " + errorPattern.toLowerCase());
						totalErrors++;
					}
				}
			}

			// 6 Read scoring thresholds
			long fullAllowed = ((Number) scoringRules.get("max_allowed_errors_for_full_score")).longValue();
			long partialAllowed = ((Number) scoringRules.get("max_allowed_errors_for_partial_score")).longValue();
			long zeroAbove = ((Number) scoringRules.get("zero_score_if_errors_above")).longValue();

			// 7 Final scoring decision
			if (totalErrors <= fullAllowed) {
				ats228_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats228_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats228_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats228_points == max) {
				paramType = "positive";
			} else if (ats228_points == partial) {
				paramType = "partial";
			} else if (ats228_points == 0) {
				paramType = "negative";
			}

			calAtsGen228.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen228.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen228.setCategory(genATSParamData.getCategory());
			calAtsGen228.setDescription(genATSParamData.getDescription());
			calAtsGen228.setMax_points(genATSParamData.getMax_points());
			calAtsGen228.setParameter(genATSParamData.getParameter());
			calAtsGen228.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen228.setTotal_points(genATSParamData.getTotal_points());

			calAts228.setAtsGeneralId(atsGeneralId);
			calAts228.setAtsGeneralParamDto(calAtsGen228);
			calAts228.setAtsParamData(ats228ParamData);
			calAts228.setAtsParamId(atsParamId);
			calAts228.setAtsParamType(paramType);
			calAts228.setAtsScore(ats228_points);

			return calAts228;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts228;
		}
	}

	public AtsListDto calculateATS229(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts229 = new AtsListDto();
		AtsGenParamDto calAtsGen229 = new AtsGenParamDto();
		long ats229_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats229ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats229ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			resumeText = resumeText.toLowerCase();

			// 4 Read indicator groups
			Map<String, Object> logicDesc = (Map<String, Object>) ats229ParamData.get("logic_description");

			Map<String, Object> nonTextIndicators = (Map<String, Object>) logicDesc.get("non_text_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDesc.get("matching_rules");

			long fullScoreLimit = ((Number) matchingRules.get("max_allowed_non_text_elements_for_full_score"))
					.longValue();

			long partialScoreLimit = ((Number) matchingRules.get("max_allowed_non_text_elements_for_partial_score"))
					.longValue();

			long zeroScoreLimit = ((Number) matchingRules.get("zero_score_if_non_text_elements_above")).longValue();

			// 5 Count non-text violations

			long totalViolations = 0;

			for (Object indicatorListObj : nonTextIndicators.values()) {
				List<String> indicators = (List<String>) indicatorListObj;

				for (String indicator : indicators) {
					if (resumeText.contains(indicator.toLowerCase())) {
						System.out.println("ATS-229 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats229_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats229_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats229_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats229_points == max) {
				paramType = "positive";
			} else if (ats229_points == partial) {
				paramType = "partial";
			} else if (ats229_points == 0) {
				paramType = "negative";
			}

			calAtsGen229.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen229.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen229.setCategory(genATSParamData.getCategory());
			calAtsGen229.setDescription(genATSParamData.getDescription());
			calAtsGen229.setMax_points(genATSParamData.getMax_points());
			calAtsGen229.setParameter(genATSParamData.getParameter());
			calAtsGen229.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen229.setTotal_points(genATSParamData.getTotal_points());

			calAts229.setAtsGeneralId(atsGeneralId);
			calAts229.setAtsGeneralParamDto(calAtsGen229);
			calAts229.setAtsParamData(ats229ParamData);
			calAts229.setAtsParamId(atsParamId);
			calAts229.setAtsParamType(paramType);
			calAts229.setAtsScore(ats229_points);

			return calAts229;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts229;
		}
	}

	public AtsListDto calculateATS230(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts230 = new AtsListDto();
		AtsGenParamDto calAtsGen230 = new AtsGenParamDto();

		long ats230_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats230ParamData = getGeneralHealthcareATSParam(atsGeneralId, atsParamId);

			if (ats230ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			resumeText = resumeText.toLowerCase();

			// 4 Read JSON logic
			Map<String, Object> logicDesc = (Map<String, Object>) ats230ParamData.get("logic_description");

			Map<String, Object> criticalInfoIndicators = (Map<String, Object>) logicDesc
					.get("critical_information_indicators");

			List<String> headerFooterIndicators = (List<String>) logicDesc.get("header_footer_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDesc.get("matching_rules");

			long fullScoreLimit = ((Number) matchingRules.get("max_allowed_header_footer_violations_for_full_score"))
					.longValue();

			long partialScoreLimit = ((Number) matchingRules
					.get("max_allowed_header_footer_violations_for_partial_score")).longValue();

			long zeroScoreLimit = ((Number) matchingRules.get("zero_score_if_violations_above")).longValue();

			// 5 Detect header/footer related critical info
			long totalViolations = 0;

			for (String headerFooterKeyword : headerFooterIndicators) {
				if (resumeText.contains(headerFooterKeyword.toLowerCase())) {

					// If header/footer exists, check for critical info presence
					for (Object indicatorGroupObj : criticalInfoIndicators.values()) {
						List<String> indicators = (List<String>) indicatorGroupObj;

						for (String indicator : indicators) {
							if (resumeText.contains(indicator.toLowerCase())) {
								System.out.println("ATS-230 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats230_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats230_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats230_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats230_points == max) {
				paramType = "positive";
			} else if (ats230_points == partial) {
				paramType = "partial";
			} else if (ats230_points == 0) {
				paramType = "negative";
			}

			calAtsGen230.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen230.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen230.setCategory(genATSParamData.getCategory());
			calAtsGen230.setDescription(genATSParamData.getDescription());
			calAtsGen230.setMax_points(genATSParamData.getMax_points());
			calAtsGen230.setParameter(genATSParamData.getParameter());
			calAtsGen230.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen230.setTotal_points(genATSParamData.getTotal_points());

			calAts230.setAtsGeneralId(atsGeneralId);
			calAts230.setAtsGeneralParamDto(calAtsGen230);
			calAts230.setAtsParamData(ats230ParamData);
			calAts230.setAtsParamId(atsParamId);
			calAts230.setAtsParamType(paramType);
			calAts230.setAtsScore(ats230_points);

			return calAts230;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts230;
		}
	}

}
