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
public class BusinessAtsUtil {

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

	private List<Map<String, Object>> generalBusinessATSParameterList;

	private Map<String, Map<String, Object>> generalBusinessATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadBusiness_ATSParameter() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Business_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalBusinessATSParameterList = (List<Map<String, Object>>) jsonData.get("Business_ATSParameter");

			// Create fast-lookup map
			generalBusinessATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalBusinessATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalBusinessATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalBusinessATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Business_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalBusinessATSParameterList :: " + generalBusinessATSParameterList);
		return generalBusinessATSParameterList;
	}

	public Map<String, Object> getGeneralBusinessATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralBusinessATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalBusinessATSParameterMap :: " + generalBusinessATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalBusinessATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}
	
	public AtsListDto calculateATS031(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts031 = new AtsListDto();
		AtsGenParamDto calAtsGen031 = new AtsGenParamDto();
		long ats031_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats031ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);
			if (ats031ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats031ParamData.get("logic_description");

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
					System.out.println("ATS-031 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats031_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats031_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats031_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats031_points == max) {
				paramType = "positive";
			} else if (ats031_points == partial) {
				paramType = "partial";
			} else if (ats031_points == 0) {
				paramType = "negative";
			}

			calAtsGen031.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen031.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen031.setCategory(genATSParamData.getCategory());
			calAtsGen031.setDescription(genATSParamData.getDescription());
			calAtsGen031.setMax_points(genATSParamData.getMax_points());
			calAtsGen031.setParameter(genATSParamData.getParameter());
			calAtsGen031.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen031.setTotal_points(genATSParamData.getTotal_points());

			calAts031.setAtsGeneralId(atsGeneralId);
			calAts031.setAtsGeneralParamDto(calAtsGen031);
			calAts031.setAtsParamData(ats031ParamData);
			calAts031.setAtsParamId(atsParamId);
			calAts031.setAtsParamType(paramType);
			calAts031.setAtsScore(ats031_points);

			return calAts031;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts031;
		}
	}

	public AtsListDto calculateATS032(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts032 = new AtsListDto();
		AtsGenParamDto calAtsGen032 = new AtsGenParamDto();
		long ats032_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats032ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats032ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats032ParamData.get("logic_description");

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
						System.out.println("ATS-032 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats032_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats032_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats032_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats032_points == max) {
				paramType = "positive";
			} else if (ats032_points == partial) {
				paramType = "partial";
			} else if (ats032_points == 0) {
				paramType = "negative";
			}

			calAtsGen032.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen032.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen032.setCategory(genATSParamData.getCategory());
			calAtsGen032.setDescription(genATSParamData.getDescription());
			calAtsGen032.setMax_points(genATSParamData.getMax_points());
			calAtsGen032.setParameter(genATSParamData.getParameter());
			calAtsGen032.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen032.setTotal_points(genATSParamData.getTotal_points());

			calAts032.setAtsGeneralId(atsGeneralId);
			calAts032.setAtsGeneralParamDto(calAtsGen032);
			calAts032.setAtsParamData(ats032ParamData);
			calAts032.setAtsParamId(atsParamId);
			calAts032.setAtsParamType(paramType);
			calAts032.setAtsScore(ats032_points);

			return calAts032;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts032;
		}
	}

	public AtsListDto calculateATS033(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts033 = new AtsListDto();
		AtsGenParamDto calAtsGen033 = new AtsGenParamDto();
		long ats033_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats033ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats033ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats033ParamData.get("logic_description");

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
						System.out.println("ATS-033 :: " + alternate);
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
				ats033_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats033_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats033_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats033_points == max) {
				paramType = "positive";
			} else if (ats033_points == partial) {
				paramType = "partial";
			} else if (ats033_points == 0) {
				paramType = "negative";
			}

			calAtsGen033.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen033.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen033.setCategory(genATSParamData.getCategory());
			calAtsGen033.setDescription(genATSParamData.getDescription());
			calAtsGen033.setMax_points(genATSParamData.getMax_points());
			calAtsGen033.setParameter(genATSParamData.getParameter());
			calAtsGen033.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen033.setTotal_points(genATSParamData.getTotal_points());

			calAts033.setAtsGeneralId(atsGeneralId);
			calAts033.setAtsGeneralParamDto(calAtsGen033);
			calAts033.setAtsParamData(ats033ParamData);
			calAts033.setAtsParamId(atsParamId);
			calAts033.setAtsParamType(paramType);
			calAts033.setAtsScore(ats033_points);

			return calAts033;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts033;
		}
	}

	public AtsListDto calculateATS034(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts034 = new AtsListDto();
		AtsGenParamDto calAtsGen034 = new AtsGenParamDto();
		long ats034_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats034ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats034ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats034ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-034 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats034_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats034_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats034_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats034_points == max) {
				paramType = "positive";
			} else if (ats034_points == partial) {
				paramType = "partial";
			} else if (ats034_points == 0) {
				paramType = "negative";
			}

			calAtsGen034.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen034.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen034.setCategory(genATSParamData.getCategory());
			calAtsGen034.setDescription(genATSParamData.getDescription());
			calAtsGen034.setMax_points(genATSParamData.getMax_points());
			calAtsGen034.setParameter(genATSParamData.getParameter());
			calAtsGen034.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen034.setTotal_points(genATSParamData.getTotal_points());

			calAts034.setAtsGeneralId(atsGeneralId);
			calAts034.setAtsGeneralParamDto(calAtsGen034);
			calAts034.setAtsParamData(ats034ParamData);
			calAts034.setAtsParamId(atsParamId);
			calAts034.setAtsParamType(paramType);
			calAts034.setAtsScore(ats034_points);

			return calAts034;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts034;
		}
	}

	public AtsListDto calculateATS035(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts035 = new AtsListDto();
		AtsGenParamDto calAtsGen035 = new AtsGenParamDto();
		long ats035_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats035ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats035ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats035ParamData.get("logic_description");

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
						System.out.println("ATS-035 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats035_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats035_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats035_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats035_points == max) {
				paramType = "positive";
			} else if (ats035_points == partial) {
				paramType = "partial";
			} else if (ats035_points == 0) {
				paramType = "negative";
			}

			calAtsGen035.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen035.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen035.setCategory(genATSParamData.getCategory());
			calAtsGen035.setDescription(genATSParamData.getDescription());
			calAtsGen035.setMax_points(genATSParamData.getMax_points());
			calAtsGen035.setParameter(genATSParamData.getParameter());
			calAtsGen035.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen035.setTotal_points(genATSParamData.getTotal_points());

			calAts035.setAtsGeneralId(atsGeneralId);
			calAts035.setAtsGeneralParamDto(calAtsGen035);
			calAts035.setAtsParamData(ats035ParamData);
			calAts035.setAtsParamId(atsParamId);
			calAts035.setAtsParamType(paramType);
			calAts035.setAtsScore(ats035_points);

			return calAts035;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts035;
		}
	}

	public AtsListDto calculateATS036(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts036 = new AtsListDto();
		AtsGenParamDto calAtsGen036 = new AtsGenParamDto();
		long ats036_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats036ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats036ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats036ParamData.get("logic_description");

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

			System.out.println("ATS-036 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats036ParamData);
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
				ats036_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats036_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats036_points = fullScore;
			} else {
				ats036_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats036_points == max) {
				paramType = "positive";
			} else if (ats036_points == partial) {
				paramType = "partial";
			} else if (ats036_points == 0) {
				paramType = "negative";
			}

			calAtsGen036.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen036.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen036.setCategory(genATSParamData.getCategory());
			calAtsGen036.setDescription(genATSParamData.getDescription());
			calAtsGen036.setMax_points(genATSParamData.getMax_points());
			calAtsGen036.setParameter(genATSParamData.getParameter());
			calAtsGen036.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen036.setTotal_points(genATSParamData.getTotal_points());

			calAts036.setAtsGeneralId(atsGeneralId);
			calAts036.setAtsGeneralParamDto(calAtsGen036);
			calAts036.setAtsParamData(ats036ParamData);
			calAts036.setAtsParamId(atsParamId);
			calAts036.setAtsParamType(paramType);
			calAts036.setAtsScore(ats036_points);

			return calAts036;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts036;
		}
	}

	public AtsListDto calculateATS037(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts037 = new AtsListDto();
		AtsGenParamDto calAtsGen037 = new AtsGenParamDto();
		long ats037_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats037ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats037ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats037ParamData.get("logic_description");

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
					System.out.println("ATS-037 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats037_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats037_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats037_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats037_points == max) {
				paramType = "positive";
			} else if (ats037_points == partial) {
				paramType = "partial";
			} else if (ats037_points == 0) {
				paramType = "negative";
			}

			calAtsGen037.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen037.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen037.setCategory(genATSParamData.getCategory());
			calAtsGen037.setDescription(genATSParamData.getDescription());
			calAtsGen037.setMax_points(genATSParamData.getMax_points());
			calAtsGen037.setParameter(genATSParamData.getParameter());
			calAtsGen037.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen037.setTotal_points(genATSParamData.getTotal_points());

			calAts037.setAtsGeneralId(atsGeneralId);
			calAts037.setAtsGeneralParamDto(calAtsGen037);
			calAts037.setAtsParamData(ats037ParamData);
			calAts037.setAtsParamId(atsParamId);
			calAts037.setAtsParamType(paramType);
			calAts037.setAtsScore(ats037_points);

			return calAts037;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts037;
		}
	}

	public AtsListDto calculateATS038(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts038 = new AtsListDto();
		AtsGenParamDto calAtsGen038 = new AtsGenParamDto();
		long ats038_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats038ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats038ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats038ParamData.get("logic_description");

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
						System.out.println("ATS-038 :: " + key);
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
				ats038_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats038_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats038_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats038_points == max) {
				paramType = "positive";
			} else if (ats038_points == partial) {
				paramType = "partial";
			} else if (ats038_points == 0) {
				paramType = "negative";
			}

			calAtsGen038.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen038.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen038.setCategory(genATSParamData.getCategory());
			calAtsGen038.setDescription(genATSParamData.getDescription());
			calAtsGen038.setMax_points(genATSParamData.getMax_points());
			calAtsGen038.setParameter(genATSParamData.getParameter());
			calAtsGen038.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen038.setTotal_points(genATSParamData.getTotal_points());

			calAts038.setAtsGeneralId(atsGeneralId);
			calAts038.setAtsGeneralParamDto(calAtsGen038);
			calAts038.setAtsParamData(ats038ParamData);
			calAts038.setAtsParamId(atsParamId);
			calAts038.setAtsParamType(paramType);
			calAts038.setAtsScore(ats038_points);

			return calAts038;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts038;
		}
	}

	public AtsListDto calculateATS039(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts039 = new AtsListDto();
		AtsGenParamDto calAtsGen039 = new AtsGenParamDto();
		long ats039_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats039ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats039ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats039ParamData.get("logic_description");

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
						System.out.println("ATS-039 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats039_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats039_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats039_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats039_points == max) {
				paramType = "positive";
			} else if (ats039_points == partial) {
				paramType = "partial";
			} else if (ats039_points == 0) {
				paramType = "negative";
			}

			calAtsGen039.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen039.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen039.setCategory(genATSParamData.getCategory());
			calAtsGen039.setDescription(genATSParamData.getDescription());
			calAtsGen039.setMax_points(genATSParamData.getMax_points());
			calAtsGen039.setParameter(genATSParamData.getParameter());
			calAtsGen039.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen039.setTotal_points(genATSParamData.getTotal_points());

			calAts039.setAtsGeneralId(atsGeneralId);
			calAts039.setAtsGeneralParamDto(calAtsGen039);
			calAts039.setAtsParamData(ats039ParamData);
			calAts039.setAtsParamId(atsParamId);
			calAts039.setAtsParamType(paramType);
			calAts039.setAtsScore(ats039_points);

			return calAts039;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts039;
		}
	}

	public AtsListDto calculateATS040(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts040 = new AtsListDto();
		AtsGenParamDto calAtsGen040 = new AtsGenParamDto();
		long ats040_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats040ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats040ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats040ParamData.get("logic_description");

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
					System.out.println("ATS-040 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats040_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats040_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats040_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats040_points == max) {
				paramType = "positive";
			} else if (ats040_points == partial) {
				paramType = "partial";
			} else if (ats040_points == 0) {
				paramType = "negative";
			}

			calAtsGen040.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen040.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen040.setCategory(genATSParamData.getCategory());
			calAtsGen040.setDescription(genATSParamData.getDescription());
			calAtsGen040.setMax_points(genATSParamData.getMax_points());
			calAtsGen040.setParameter(genATSParamData.getParameter());
			calAtsGen040.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen040.setTotal_points(genATSParamData.getTotal_points());

			calAts040.setAtsGeneralId(atsGeneralId);
			calAts040.setAtsGeneralParamDto(calAtsGen040);
			calAts040.setAtsParamData(ats040ParamData);
			calAts040.setAtsParamId(atsParamId);
			calAts040.setAtsParamType(paramType);
			calAts040.setAtsScore(ats040_points);

			return calAts040;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts040;
		}
	}

	public AtsListDto calculateATS041(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts041 = new AtsListDto();
		AtsGenParamDto calAtsGen041 = new AtsGenParamDto();
		long ats041_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats041ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats041ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats041ParamData.get("logic_description");

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
					System.out.println("ATS-041 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats041_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats041_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats041_points == max) {
				paramType = "positive";
			} else if (ats041_points == partial) {
				paramType = "partial";
			} else if (ats041_points == 0) {
				paramType = "negative";
			}

			calAtsGen041.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen041.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen041.setCategory(genATSParamData.getCategory());
			calAtsGen041.setDescription(genATSParamData.getDescription());
			calAtsGen041.setMax_points(genATSParamData.getMax_points());
			calAtsGen041.setParameter(genATSParamData.getParameter());
			calAtsGen041.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen041.setTotal_points(genATSParamData.getTotal_points());

			calAts041.setAtsGeneralId(atsGeneralId);
			calAts041.setAtsGeneralParamDto(calAtsGen041);
			calAts041.setAtsParamData(ats041ParamData);
			calAts041.setAtsParamId(atsParamId);
			calAts041.setAtsParamType(paramType);
			calAts041.setAtsScore(ats041_points);

			return calAts041;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts041;
		}
	}

	public AtsListDto calculateATS042(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts042 = new AtsListDto();
		AtsGenParamDto calAtsGen042 = new AtsGenParamDto();
		long ats042_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats042ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats042ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats042ParamData.get("logic_description");

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
						System.out.println("ATS-042 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 503ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats042_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats042_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats042_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats042_points == max) {
				paramType = "positive";
			} else if (ats042_points == partial) {
				paramType = "partial";
			} else if (ats042_points == 0) {
				paramType = "negative";
			}

			calAtsGen042.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen042.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen042.setCategory(genATSParamData.getCategory());
			calAtsGen042.setDescription(genATSParamData.getDescription());
			calAtsGen042.setMax_points(genATSParamData.getMax_points());
			calAtsGen042.setParameter(genATSParamData.getParameter());
			calAtsGen042.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen042.setTotal_points(genATSParamData.getTotal_points());

			calAts042.setAtsGeneralId(atsGeneralId);
			calAts042.setAtsGeneralParamDto(calAtsGen042);
			calAts042.setAtsParamData(ats042ParamData);
			calAts042.setAtsParamId(atsParamId);
			calAts042.setAtsParamType(paramType);
			calAts042.setAtsScore(ats042_points);

			return calAts042;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts042;
		}
	}

	public AtsListDto calculateATS043(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts043 = new AtsListDto();
		AtsGenParamDto calAtsGen043 = new AtsGenParamDto();
		long ats043_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats043ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats043ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats043ParamData.get("logic_description");

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
						System.out.println("ATS-043 :: " + key);
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
				ats043_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats043_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats043_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats043_points == max) {
				paramType = "positive";
			} else if (ats043_points == partial) {
				paramType = "partial";
			} else if (ats043_points == 0) {
				paramType = "negative";
			}

			calAtsGen043.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen043.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen043.setCategory(genATSParamData.getCategory());
			calAtsGen043.setDescription(genATSParamData.getDescription());
			calAtsGen043.setMax_points(genATSParamData.getMax_points());
			calAtsGen043.setParameter(genATSParamData.getParameter());
			calAtsGen043.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen043.setTotal_points(genATSParamData.getTotal_points());

			calAts043.setAtsGeneralId(atsGeneralId);
			calAts043.setAtsGeneralParamDto(calAtsGen043);
			calAts043.setAtsParamData(ats043ParamData);
			calAts043.setAtsParamId(atsParamId);
			calAts043.setAtsParamType(paramType);
			calAts043.setAtsScore(ats043_points);

			return calAts043;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts043;
		}
	}

	public AtsListDto calculateATS044(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts044 = new AtsListDto();
		AtsGenParamDto calAtsGen044 = new AtsGenParamDto();
		long ats044_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats044ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats044ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats044ParamData.get("logic_description");

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
						System.out.println("ATS-044 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats044_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats044_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats044_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats044_points == max) {
				paramType = "positive";
			} else if (ats044_points == partial) {
				paramType = "partial";
			} else if (ats044_points == 0) {
				paramType = "negative";
			}

			calAtsGen044.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen044.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen044.setCategory(genATSParamData.getCategory());
			calAtsGen044.setDescription(genATSParamData.getDescription());
			calAtsGen044.setMax_points(genATSParamData.getMax_points());
			calAtsGen044.setParameter(genATSParamData.getParameter());
			calAtsGen044.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen044.setTotal_points(genATSParamData.getTotal_points());

			calAts044.setAtsGeneralId(atsGeneralId);
			calAts044.setAtsGeneralParamDto(calAtsGen044);
			calAts044.setAtsParamData(ats044ParamData);
			calAts044.setAtsParamId(atsParamId);
			calAts044.setAtsParamType(paramType);
			calAts044.setAtsScore(ats044_points);

			return calAts044;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts044;
		}
	}

	public AtsListDto calculateATS045(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts045 = new AtsListDto();
		AtsGenParamDto calAtsGen045 = new AtsGenParamDto();
		long ats045_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats045ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats045ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats045ParamData.get("logic_description");

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
						System.out.println("ATS-045 :: " + key);
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
				ats045_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats045_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats045_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats045_points == max) {
				paramType = "positive";
			} else if (ats045_points == partial) {
				paramType = "partial";
			} else if (ats045_points == 0) {
				paramType = "negative";
			}

			calAtsGen045.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen045.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen045.setCategory(genATSParamData.getCategory());
			calAtsGen045.setDescription(genATSParamData.getDescription());
			calAtsGen045.setMax_points(genATSParamData.getMax_points());
			calAtsGen045.setParameter(genATSParamData.getParameter());
			calAtsGen045.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen045.setTotal_points(genATSParamData.getTotal_points());

			calAts045.setAtsGeneralId(atsGeneralId);
			calAts045.setAtsGeneralParamDto(calAtsGen045);
			calAts045.setAtsParamData(ats045ParamData);
			calAts045.setAtsParamId(atsParamId);
			calAts045.setAtsParamType(paramType);
			calAts045.setAtsScore(ats045_points);

			return calAts045;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts045;
		}
	}

	public AtsListDto calculateATS046(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts046 = new AtsListDto();
		AtsGenParamDto calAtsGen046 = new AtsGenParamDto();
		long ats046_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats046ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats046ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats046ParamData.get("logic_description");

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
						System.out.println("ATS-046 :: " + key);
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
				ats046_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats046_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats046_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats046_points == max) {
				paramType = "positive";
			} else if (ats046_points == partial) {
				paramType = "partial";
			} else if (ats046_points == 0) {
				paramType = "negative";
			}

			calAtsGen046.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen046.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen046.setCategory(genATSParamData.getCategory());
			calAtsGen046.setDescription(genATSParamData.getDescription());
			calAtsGen046.setMax_points(genATSParamData.getMax_points());
			calAtsGen046.setParameter(genATSParamData.getParameter());
			calAtsGen046.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen046.setTotal_points(genATSParamData.getTotal_points());

			calAts046.setAtsGeneralId(atsGeneralId);
			calAts046.setAtsGeneralParamDto(calAtsGen046);
			calAts046.setAtsParamData(ats046ParamData);
			calAts046.setAtsParamId(atsParamId);
			calAts046.setAtsParamType(paramType);
			calAts046.setAtsScore(ats046_points);

			return calAts046;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts046;
		}
	}

	public AtsListDto calculateATS047(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts047 = new AtsListDto();
		AtsGenParamDto calAtsGen047 = new AtsGenParamDto();
		long ats047_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats047ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats047ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats047ParamData.get("logic_description");

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
						System.out.println("ATS-047 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats047_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats047_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats047_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats047_points == max) {
				paramType = "positive";
			} else if (ats047_points == partial) {
				paramType = "partial";
			} else if (ats047_points == 0) {
				paramType = "negative";
			}

			calAtsGen047.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen047.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen047.setCategory(genATSParamData.getCategory());
			calAtsGen047.setDescription(genATSParamData.getDescription());
			calAtsGen047.setMax_points(genATSParamData.getMax_points());
			calAtsGen047.setParameter(genATSParamData.getParameter());
			calAtsGen047.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen047.setTotal_points(genATSParamData.getTotal_points());

			calAts047.setAtsGeneralId(atsGeneralId);
			calAts047.setAtsGeneralParamDto(calAtsGen047);
			calAts047.setAtsParamData(ats047ParamData);
			calAts047.setAtsParamId(atsParamId);
			calAts047.setAtsParamType(paramType);
			calAts047.setAtsScore(ats047_points);

			return calAts047;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts047;
		}
	}

	public AtsListDto calculateATS048(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts048 = new AtsListDto();
		AtsGenParamDto calAtsGen048 = new AtsGenParamDto();
		long ats048_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats048ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats048ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats048ParamData.get("logic_description");
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
						System.out.println("ATS-048 :: " + errorPattern.toLowerCase());
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
				ats048_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats048_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats048_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats048_points == max) {
				paramType = "positive";
			} else if (ats048_points == partial) {
				paramType = "partial";
			} else if (ats048_points == 0) {
				paramType = "negative";
			}

			calAtsGen048.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen048.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen048.setCategory(genATSParamData.getCategory());
			calAtsGen048.setDescription(genATSParamData.getDescription());
			calAtsGen048.setMax_points(genATSParamData.getMax_points());
			calAtsGen048.setParameter(genATSParamData.getParameter());
			calAtsGen048.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen048.setTotal_points(genATSParamData.getTotal_points());

			calAts048.setAtsGeneralId(atsGeneralId);
			calAts048.setAtsGeneralParamDto(calAtsGen048);
			calAts048.setAtsParamData(ats048ParamData);
			calAts048.setAtsParamId(atsParamId);
			calAts048.setAtsParamType(paramType);
			calAts048.setAtsScore(ats048_points);

			return calAts048;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts048;
		}
	}

	public AtsListDto calculateATS049(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts049 = new AtsListDto();
		AtsGenParamDto calAtsGen049 = new AtsGenParamDto();
		long ats049_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats049ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats049ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats049ParamData.get("logic_description");

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
						System.out.println("ATS-049 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats049_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats049_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats049_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats049_points == max) {
				paramType = "positive";
			} else if (ats049_points == partial) {
				paramType = "partial";
			} else if (ats049_points == 0) {
				paramType = "negative";
			}

			calAtsGen049.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen049.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen049.setCategory(genATSParamData.getCategory());
			calAtsGen049.setDescription(genATSParamData.getDescription());
			calAtsGen049.setMax_points(genATSParamData.getMax_points());
			calAtsGen049.setParameter(genATSParamData.getParameter());
			calAtsGen049.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen049.setTotal_points(genATSParamData.getTotal_points());

			calAts049.setAtsGeneralId(atsGeneralId);
			calAts049.setAtsGeneralParamDto(calAtsGen049);
			calAts049.setAtsParamData(ats049ParamData);
			calAts049.setAtsParamId(atsParamId);
			calAts049.setAtsParamType(paramType);
			calAts049.setAtsScore(ats049_points);

			return calAts049;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts049;
		}
	}

	public AtsListDto calculateATS050(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts050 = new AtsListDto();
		AtsGenParamDto calAtsGen050 = new AtsGenParamDto();

		long ats050_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats050ParamData = getGeneralBusinessATSParam(atsGeneralId, atsParamId);

			if (ats050ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats050ParamData.get("logic_description");

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
								System.out.println("ATS-050 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats050_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats050_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats050_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats050_points == max) {
				paramType = "positive";
			} else if (ats050_points == partial) {
				paramType = "partial";
			} else if (ats050_points == 0) {
				paramType = "negative";
			}

			calAtsGen050.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen050.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen050.setCategory(genATSParamData.getCategory());
			calAtsGen050.setDescription(genATSParamData.getDescription());
			calAtsGen050.setMax_points(genATSParamData.getMax_points());
			calAtsGen050.setParameter(genATSParamData.getParameter());
			calAtsGen050.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen050.setTotal_points(genATSParamData.getTotal_points());

			calAts050.setAtsGeneralId(atsGeneralId);
			calAts050.setAtsGeneralParamDto(calAtsGen050);
			calAts050.setAtsParamData(ats050ParamData);
			calAts050.setAtsParamId(atsParamId);
			calAts050.setAtsParamType(paramType);
			calAts050.setAtsScore(ats050_points);

			return calAts050;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts050;
		}
	}


}
