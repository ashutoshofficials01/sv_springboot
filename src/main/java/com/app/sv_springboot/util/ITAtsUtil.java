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
public class ITAtsUtil {

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

	private List<Map<String, Object>> generalATSParameterList;

	private Map<String, Map<String, Object>> generalATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

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

	public AtsListDto calculateATS001(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts001 = new AtsListDto();
		AtsGenParamDto calAtsGen001 = new AtsGenParamDto();
		long ats001_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats001ParamData = getGeneralATSParam(atsGeneralId, atsParamId);
			if (ats001ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats001ParamData.get("logic_description");

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
					System.out.println("ATS-001 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats001_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats001_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats001_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats001_points == max) {
				paramType = "positive";
			} else if (ats001_points == partial) {
				paramType = "partial";
			} else if (ats001_points == 0) {
				paramType = "negative";
			}

			calAtsGen001.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen001.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen001.setCategory(genATSParamData.getCategory());
			calAtsGen001.setDescription(genATSParamData.getDescription());
			calAtsGen001.setMax_points(genATSParamData.getMax_points());
			calAtsGen001.setParameter(genATSParamData.getParameter());
			calAtsGen001.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen001.setTotal_points(genATSParamData.getTotal_points());

			calAts001.setAtsGeneralId(atsGeneralId);
			calAts001.setAtsGeneralParamDto(calAtsGen001);
			calAts001.setAtsParamData(ats001ParamData);
			calAts001.setAtsParamId(atsParamId);
			calAts001.setAtsParamType(paramType);
			calAts001.setAtsScore(ats001_points);

			return calAts001;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts001;
		}
	}

	public AtsListDto calculateATS002(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts002 = new AtsListDto();
		AtsGenParamDto calAtsGen002 = new AtsGenParamDto();
		long ats002_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats002ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats002ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats002ParamData.get("logic_description");

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
						System.out.println("ATS-002 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats002_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats002_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats002_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats002_points == max) {
				paramType = "positive";
			} else if (ats002_points == partial) {
				paramType = "partial";
			} else if (ats002_points == 0) {
				paramType = "negative";
			}

			calAtsGen002.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen002.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen002.setCategory(genATSParamData.getCategory());
			calAtsGen002.setDescription(genATSParamData.getDescription());
			calAtsGen002.setMax_points(genATSParamData.getMax_points());
			calAtsGen002.setParameter(genATSParamData.getParameter());
			calAtsGen002.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen002.setTotal_points(genATSParamData.getTotal_points());

			calAts002.setAtsGeneralId(atsGeneralId);
			calAts002.setAtsGeneralParamDto(calAtsGen002);
			calAts002.setAtsParamData(ats002ParamData);
			calAts002.setAtsParamId(atsParamId);
			calAts002.setAtsParamType(paramType);
			calAts002.setAtsScore(ats002_points);

			return calAts002;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts002;
		}
	}

	public AtsListDto calculateATS003(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts003 = new AtsListDto();
		AtsGenParamDto calAtsGen003 = new AtsGenParamDto();
		long ats003_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats003ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats003ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats003ParamData.get("logic_description");

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
						System.out.println("ATS-003 :: " + alternate);
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
				ats003_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats003_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats003_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats003_points == max) {
				paramType = "positive";
			} else if (ats003_points == partial) {
				paramType = "partial";
			} else if (ats003_points == 0) {
				paramType = "negative";
			}

			calAtsGen003.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen003.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen003.setCategory(genATSParamData.getCategory());
			calAtsGen003.setDescription(genATSParamData.getDescription());
			calAtsGen003.setMax_points(genATSParamData.getMax_points());
			calAtsGen003.setParameter(genATSParamData.getParameter());
			calAtsGen003.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen003.setTotal_points(genATSParamData.getTotal_points());

			calAts003.setAtsGeneralId(atsGeneralId);
			calAts003.setAtsGeneralParamDto(calAtsGen003);
			calAts003.setAtsParamData(ats003ParamData);
			calAts003.setAtsParamId(atsParamId);
			calAts003.setAtsParamType(paramType);
			calAts003.setAtsScore(ats003_points);

			return calAts003;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts003;
		}
	}

	public AtsListDto calculateATS004(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts004 = new AtsListDto();
		AtsGenParamDto calAtsGen004 = new AtsGenParamDto();
		long ats004_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats004ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats004ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats004ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-004 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats004_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats004_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats004_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats004_points == max) {
				paramType = "positive";
			} else if (ats004_points == partial) {
				paramType = "partial";
			} else if (ats004_points == 0) {
				paramType = "negative";
			}

			calAtsGen004.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen004.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen004.setCategory(genATSParamData.getCategory());
			calAtsGen004.setDescription(genATSParamData.getDescription());
			calAtsGen004.setMax_points(genATSParamData.getMax_points());
			calAtsGen004.setParameter(genATSParamData.getParameter());
			calAtsGen004.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen004.setTotal_points(genATSParamData.getTotal_points());

			calAts004.setAtsGeneralId(atsGeneralId);
			calAts004.setAtsGeneralParamDto(calAtsGen004);
			calAts004.setAtsParamData(ats004ParamData);
			calAts004.setAtsParamId(atsParamId);
			calAts004.setAtsParamType(paramType);
			calAts004.setAtsScore(ats004_points);

			return calAts004;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts004;
		}
	}

	public AtsListDto calculateATS005(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts005 = new AtsListDto();
		AtsGenParamDto calAtsGen005 = new AtsGenParamDto();
		long ats005_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats005ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats005ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats005ParamData.get("logic_description");

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
						System.out.println("ATS-005 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats005_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats005_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats005_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats005_points == max) {
				paramType = "positive";
			} else if (ats005_points == partial) {
				paramType = "partial";
			} else if (ats005_points == 0) {
				paramType = "negative";
			}

			calAtsGen005.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen005.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen005.setCategory(genATSParamData.getCategory());
			calAtsGen005.setDescription(genATSParamData.getDescription());
			calAtsGen005.setMax_points(genATSParamData.getMax_points());
			calAtsGen005.setParameter(genATSParamData.getParameter());
			calAtsGen005.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen005.setTotal_points(genATSParamData.getTotal_points());

			calAts005.setAtsGeneralId(atsGeneralId);
			calAts005.setAtsGeneralParamDto(calAtsGen005);
			calAts005.setAtsParamData(ats005ParamData);
			calAts005.setAtsParamId(atsParamId);
			calAts005.setAtsParamType(paramType);
			calAts005.setAtsScore(ats005_points);

			return calAts005;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts005;
		}
	}

	public AtsListDto calculateATS006(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts006 = new AtsListDto();
		AtsGenParamDto calAtsGen006 = new AtsGenParamDto();
		long ats006_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats006ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats006ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats006ParamData.get("logic_description");

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

			System.out.println("ATS-006 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats006ParamData);
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
				ats006_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats006_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats006_points = fullScore;
			} else {
				ats006_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats006_points == max) {
				paramType = "positive";
			} else if (ats006_points == partial) {
				paramType = "partial";
			} else if (ats006_points == 0) {
				paramType = "negative";
			}

			calAtsGen006.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen006.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen006.setCategory(genATSParamData.getCategory());
			calAtsGen006.setDescription(genATSParamData.getDescription());
			calAtsGen006.setMax_points(genATSParamData.getMax_points());
			calAtsGen006.setParameter(genATSParamData.getParameter());
			calAtsGen006.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen006.setTotal_points(genATSParamData.getTotal_points());

			calAts006.setAtsGeneralId(atsGeneralId);
			calAts006.setAtsGeneralParamDto(calAtsGen006);
			calAts006.setAtsParamData(ats006ParamData);
			calAts006.setAtsParamId(atsParamId);
			calAts006.setAtsParamType(paramType);
			calAts006.setAtsScore(ats006_points);

			return calAts006;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts006;
		}
	}

	public AtsListDto calculateATS007(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts007 = new AtsListDto();
		AtsGenParamDto calAtsGen007 = new AtsGenParamDto();
		long ats007_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats007ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats007ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats007ParamData.get("logic_description");

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
					System.out.println("ATS-007 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats007_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats007_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats007_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats007_points == max) {
				paramType = "positive";
			} else if (ats007_points == partial) {
				paramType = "partial";
			} else if (ats007_points == 0) {
				paramType = "negative";
			}

			calAtsGen007.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen007.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen007.setCategory(genATSParamData.getCategory());
			calAtsGen007.setDescription(genATSParamData.getDescription());
			calAtsGen007.setMax_points(genATSParamData.getMax_points());
			calAtsGen007.setParameter(genATSParamData.getParameter());
			calAtsGen007.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen007.setTotal_points(genATSParamData.getTotal_points());

			calAts007.setAtsGeneralId(atsGeneralId);
			calAts007.setAtsGeneralParamDto(calAtsGen007);
			calAts007.setAtsParamData(ats007ParamData);
			calAts007.setAtsParamId(atsParamId);
			calAts007.setAtsParamType(paramType);
			calAts007.setAtsScore(ats007_points);

			return calAts007;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts007;
		}
	}

	public AtsListDto calculateATS008(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts008 = new AtsListDto();
		AtsGenParamDto calAtsGen008 = new AtsGenParamDto();
		long ats008_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats008ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats008ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats008ParamData.get("logic_description");

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
						System.out.println("ATS-008 :: " + key);
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
				ats008_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats008_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats008_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats008_points == max) {
				paramType = "positive";
			} else if (ats008_points == partial) {
				paramType = "partial";
			} else if (ats008_points == 0) {
				paramType = "negative";
			}

			calAtsGen008.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen008.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen008.setCategory(genATSParamData.getCategory());
			calAtsGen008.setDescription(genATSParamData.getDescription());
			calAtsGen008.setMax_points(genATSParamData.getMax_points());
			calAtsGen008.setParameter(genATSParamData.getParameter());
			calAtsGen008.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen008.setTotal_points(genATSParamData.getTotal_points());

			calAts008.setAtsGeneralId(atsGeneralId);
			calAts008.setAtsGeneralParamDto(calAtsGen008);
			calAts008.setAtsParamData(ats008ParamData);
			calAts008.setAtsParamId(atsParamId);
			calAts008.setAtsParamType(paramType);
			calAts008.setAtsScore(ats008_points);

			return calAts008;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts008;
		}
	}

	public AtsListDto calculateATS009(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts009 = new AtsListDto();
		AtsGenParamDto calAtsGen009 = new AtsGenParamDto();
		long ats009_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats009ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats009ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats009ParamData.get("logic_description");

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
						System.out.println("ATS-009 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats009_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats009_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats009_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats009_points == max) {
				paramType = "positive";
			} else if (ats009_points == partial) {
				paramType = "partial";
			} else if (ats009_points == 0) {
				paramType = "negative";
			}

			calAtsGen009.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen009.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen009.setCategory(genATSParamData.getCategory());
			calAtsGen009.setDescription(genATSParamData.getDescription());
			calAtsGen009.setMax_points(genATSParamData.getMax_points());
			calAtsGen009.setParameter(genATSParamData.getParameter());
			calAtsGen009.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen009.setTotal_points(genATSParamData.getTotal_points());

			calAts009.setAtsGeneralId(atsGeneralId);
			calAts009.setAtsGeneralParamDto(calAtsGen009);
			calAts009.setAtsParamData(ats009ParamData);
			calAts009.setAtsParamId(atsParamId);
			calAts009.setAtsParamType(paramType);
			calAts009.setAtsScore(ats009_points);

			return calAts009;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts009;
		}
	}

	public AtsListDto calculateATS010(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts010 = new AtsListDto();
		AtsGenParamDto calAtsGen010 = new AtsGenParamDto();
		long ats010_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats010ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats010ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats010ParamData.get("logic_description");

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
					System.out.println("ATS-010 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats010_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats010_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats010_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats010_points == max) {
				paramType = "positive";
			} else if (ats010_points == partial) {
				paramType = "partial";
			} else if (ats010_points == 0) {
				paramType = "negative";
			}

			calAtsGen010.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen010.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen010.setCategory(genATSParamData.getCategory());
			calAtsGen010.setDescription(genATSParamData.getDescription());
			calAtsGen010.setMax_points(genATSParamData.getMax_points());
			calAtsGen010.setParameter(genATSParamData.getParameter());
			calAtsGen010.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen010.setTotal_points(genATSParamData.getTotal_points());

			calAts010.setAtsGeneralId(atsGeneralId);
			calAts010.setAtsGeneralParamDto(calAtsGen010);
			calAts010.setAtsParamData(ats010ParamData);
			calAts010.setAtsParamId(atsParamId);
			calAts010.setAtsParamType(paramType);
			calAts010.setAtsScore(ats010_points);

			return calAts010;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts010;
		}
	}

	public AtsListDto calculateATS011(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts011 = new AtsListDto();
		AtsGenParamDto calAtsGen011 = new AtsGenParamDto();
		long ats011_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats011ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats011ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats011ParamData.get("logic_description");

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
					System.out.println("ATS-011 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats011_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats011_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats011_points == max) {
				paramType = "positive";
			} else if (ats011_points == partial) {
				paramType = "partial";
			} else if (ats011_points == 0) {
				paramType = "negative";
			}

			calAtsGen011.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen011.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen011.setCategory(genATSParamData.getCategory());
			calAtsGen011.setDescription(genATSParamData.getDescription());
			calAtsGen011.setMax_points(genATSParamData.getMax_points());
			calAtsGen011.setParameter(genATSParamData.getParameter());
			calAtsGen011.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen011.setTotal_points(genATSParamData.getTotal_points());

			calAts011.setAtsGeneralId(atsGeneralId);
			calAts011.setAtsGeneralParamDto(calAtsGen011);
			calAts011.setAtsParamData(ats011ParamData);
			calAts011.setAtsParamId(atsParamId);
			calAts011.setAtsParamType(paramType);
			calAts011.setAtsScore(ats011_points);

			return calAts011;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts011;
		}
	}

	public AtsListDto calculateATS012(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts012 = new AtsListDto();
		AtsGenParamDto calAtsGen012 = new AtsGenParamDto();
		long ats012_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats012ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats012ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats012ParamData.get("logic_description");

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
						System.out.println("ATS-012 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 500ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats012_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats012_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats012_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats012_points == max) {
				paramType = "positive";
			} else if (ats012_points == partial) {
				paramType = "partial";
			} else if (ats012_points == 0) {
				paramType = "negative";
			}

			calAtsGen012.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen012.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen012.setCategory(genATSParamData.getCategory());
			calAtsGen012.setDescription(genATSParamData.getDescription());
			calAtsGen012.setMax_points(genATSParamData.getMax_points());
			calAtsGen012.setParameter(genATSParamData.getParameter());
			calAtsGen012.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen012.setTotal_points(genATSParamData.getTotal_points());

			calAts012.setAtsGeneralId(atsGeneralId);
			calAts012.setAtsGeneralParamDto(calAtsGen012);
			calAts012.setAtsParamData(ats012ParamData);
			calAts012.setAtsParamId(atsParamId);
			calAts012.setAtsParamType(paramType);
			calAts012.setAtsScore(ats012_points);

			return calAts012;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts012;
		}
	}

	public AtsListDto calculateATS013(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts013 = new AtsListDto();
		AtsGenParamDto calAtsGen013 = new AtsGenParamDto();
		long ats013_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats013ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats013ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats013ParamData.get("logic_description");

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
						System.out.println("ATS-013 :: " + key);
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
				ats013_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats013_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats013_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats013_points == max) {
				paramType = "positive";
			} else if (ats013_points == partial) {
				paramType = "partial";
			} else if (ats013_points == 0) {
				paramType = "negative";
			}

			calAtsGen013.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen013.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen013.setCategory(genATSParamData.getCategory());
			calAtsGen013.setDescription(genATSParamData.getDescription());
			calAtsGen013.setMax_points(genATSParamData.getMax_points());
			calAtsGen013.setParameter(genATSParamData.getParameter());
			calAtsGen013.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen013.setTotal_points(genATSParamData.getTotal_points());

			calAts013.setAtsGeneralId(atsGeneralId);
			calAts013.setAtsGeneralParamDto(calAtsGen013);
			calAts013.setAtsParamData(ats013ParamData);
			calAts013.setAtsParamId(atsParamId);
			calAts013.setAtsParamType(paramType);
			calAts013.setAtsScore(ats013_points);

			return calAts013;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts013;
		}
	}

	public AtsListDto calculateATS014(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts014 = new AtsListDto();
		AtsGenParamDto calAtsGen014 = new AtsGenParamDto();
		long ats014_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats014ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats014ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats014ParamData.get("logic_description");

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
						System.out.println("ATS-014 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats014_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats014_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats014_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats014_points == max) {
				paramType = "positive";
			} else if (ats014_points == partial) {
				paramType = "partial";
			} else if (ats014_points == 0) {
				paramType = "negative";
			}

			calAtsGen014.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen014.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen014.setCategory(genATSParamData.getCategory());
			calAtsGen014.setDescription(genATSParamData.getDescription());
			calAtsGen014.setMax_points(genATSParamData.getMax_points());
			calAtsGen014.setParameter(genATSParamData.getParameter());
			calAtsGen014.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen014.setTotal_points(genATSParamData.getTotal_points());

			calAts014.setAtsGeneralId(atsGeneralId);
			calAts014.setAtsGeneralParamDto(calAtsGen014);
			calAts014.setAtsParamData(ats014ParamData);
			calAts014.setAtsParamId(atsParamId);
			calAts014.setAtsParamType(paramType);
			calAts014.setAtsScore(ats014_points);

			return calAts014;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts014;
		}
	}

	public AtsListDto calculateATS015(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts015 = new AtsListDto();
		AtsGenParamDto calAtsGen015 = new AtsGenParamDto();
		long ats015_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats015ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats015ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats015ParamData.get("logic_description");

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
						System.out.println("ATS-015 :: " + key);
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
				ats015_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats015_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats015_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats015_points == max) {
				paramType = "positive";
			} else if (ats015_points == partial) {
				paramType = "partial";
			} else if (ats015_points == 0) {
				paramType = "negative";
			}

			calAtsGen015.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen015.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen015.setCategory(genATSParamData.getCategory());
			calAtsGen015.setDescription(genATSParamData.getDescription());
			calAtsGen015.setMax_points(genATSParamData.getMax_points());
			calAtsGen015.setParameter(genATSParamData.getParameter());
			calAtsGen015.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen015.setTotal_points(genATSParamData.getTotal_points());

			calAts015.setAtsGeneralId(atsGeneralId);
			calAts015.setAtsGeneralParamDto(calAtsGen015);
			calAts015.setAtsParamData(ats015ParamData);
			calAts015.setAtsParamId(atsParamId);
			calAts015.setAtsParamType(paramType);
			calAts015.setAtsScore(ats015_points);

			return calAts015;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts015;
		}
	}

	public AtsListDto calculateATS016(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts016 = new AtsListDto();
		AtsGenParamDto calAtsGen016 = new AtsGenParamDto();
		long ats016_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats016ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats016ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats016ParamData.get("logic_description");

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
						System.out.println("ATS-016 :: " + key);
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
				ats016_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats016_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats016_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats016_points == max) {
				paramType = "positive";
			} else if (ats016_points == partial) {
				paramType = "partial";
			} else if (ats016_points == 0) {
				paramType = "negative";
			}

			calAtsGen016.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen016.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen016.setCategory(genATSParamData.getCategory());
			calAtsGen016.setDescription(genATSParamData.getDescription());
			calAtsGen016.setMax_points(genATSParamData.getMax_points());
			calAtsGen016.setParameter(genATSParamData.getParameter());
			calAtsGen016.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen016.setTotal_points(genATSParamData.getTotal_points());

			calAts016.setAtsGeneralId(atsGeneralId);
			calAts016.setAtsGeneralParamDto(calAtsGen016);
			calAts016.setAtsParamData(ats016ParamData);
			calAts016.setAtsParamId(atsParamId);
			calAts016.setAtsParamType(paramType);
			calAts016.setAtsScore(ats016_points);

			return calAts016;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts016;
		}
	}

	public AtsListDto calculateATS017(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts017 = new AtsListDto();
		AtsGenParamDto calAtsGen017 = new AtsGenParamDto();
		long ats017_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats017ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats017ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats017ParamData.get("logic_description");

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
						System.out.println("ATS-017 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats017_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats017_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats017_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats017_points == max) {
				paramType = "positive";
			} else if (ats017_points == partial) {
				paramType = "partial";
			} else if (ats017_points == 0) {
				paramType = "negative";
			}

			calAtsGen017.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen017.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen017.setCategory(genATSParamData.getCategory());
			calAtsGen017.setDescription(genATSParamData.getDescription());
			calAtsGen017.setMax_points(genATSParamData.getMax_points());
			calAtsGen017.setParameter(genATSParamData.getParameter());
			calAtsGen017.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen017.setTotal_points(genATSParamData.getTotal_points());

			calAts017.setAtsGeneralId(atsGeneralId);
			calAts017.setAtsGeneralParamDto(calAtsGen017);
			calAts017.setAtsParamData(ats017ParamData);
			calAts017.setAtsParamId(atsParamId);
			calAts017.setAtsParamType(paramType);
			calAts017.setAtsScore(ats017_points);

			return calAts017;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts017;
		}
	}

	public AtsListDto calculateATS018(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts018 = new AtsListDto();
		AtsGenParamDto calAtsGen018 = new AtsGenParamDto();
		long ats018_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats018ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats018ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats018ParamData.get("logic_description");
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
						System.out.println("ATS-018 :: " + errorPattern.toLowerCase());
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
				ats018_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats018_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats018_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats018_points == max) {
				paramType = "positive";
			} else if (ats018_points == partial) {
				paramType = "partial";
			} else if (ats018_points == 0) {
				paramType = "negative";
			}

			calAtsGen018.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen018.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen018.setCategory(genATSParamData.getCategory());
			calAtsGen018.setDescription(genATSParamData.getDescription());
			calAtsGen018.setMax_points(genATSParamData.getMax_points());
			calAtsGen018.setParameter(genATSParamData.getParameter());
			calAtsGen018.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen018.setTotal_points(genATSParamData.getTotal_points());

			calAts018.setAtsGeneralId(atsGeneralId);
			calAts018.setAtsGeneralParamDto(calAtsGen018);
			calAts018.setAtsParamData(ats018ParamData);
			calAts018.setAtsParamId(atsParamId);
			calAts018.setAtsParamType(paramType);
			calAts018.setAtsScore(ats018_points);

			return calAts018;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts018;
		}
	}

	public AtsListDto calculateATS019(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts019 = new AtsListDto();
		AtsGenParamDto calAtsGen019 = new AtsGenParamDto();
		long ats019_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats019ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats019ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats019ParamData.get("logic_description");

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
						System.out.println("ATS-019 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats019_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats019_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats019_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats019_points == max) {
				paramType = "positive";
			} else if (ats019_points == partial) {
				paramType = "partial";
			} else if (ats019_points == 0) {
				paramType = "negative";
			}

			calAtsGen019.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen019.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen019.setCategory(genATSParamData.getCategory());
			calAtsGen019.setDescription(genATSParamData.getDescription());
			calAtsGen019.setMax_points(genATSParamData.getMax_points());
			calAtsGen019.setParameter(genATSParamData.getParameter());
			calAtsGen019.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen019.setTotal_points(genATSParamData.getTotal_points());

			calAts019.setAtsGeneralId(atsGeneralId);
			calAts019.setAtsGeneralParamDto(calAtsGen019);
			calAts019.setAtsParamData(ats019ParamData);
			calAts019.setAtsParamId(atsParamId);
			calAts019.setAtsParamType(paramType);
			calAts019.setAtsScore(ats019_points);

			return calAts019;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts019;
		}
	}

	public AtsListDto calculateATS020(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts020 = new AtsListDto();
		AtsGenParamDto calAtsGen020 = new AtsGenParamDto();

		long ats020_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats020ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats020ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats020ParamData.get("logic_description");

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
								System.out.println("ATS-020 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats020_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats020_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats020_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats020_points == max) {
				paramType = "positive";
			} else if (ats020_points == partial) {
				paramType = "partial";
			} else if (ats020_points == 0) {
				paramType = "negative";
			}

			calAtsGen020.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen020.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen020.setCategory(genATSParamData.getCategory());
			calAtsGen020.setDescription(genATSParamData.getDescription());
			calAtsGen020.setMax_points(genATSParamData.getMax_points());
			calAtsGen020.setParameter(genATSParamData.getParameter());
			calAtsGen020.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen020.setTotal_points(genATSParamData.getTotal_points());

			calAts020.setAtsGeneralId(atsGeneralId);
			calAts020.setAtsGeneralParamDto(calAtsGen020);
			calAts020.setAtsParamData(ats020ParamData);
			calAts020.setAtsParamId(atsParamId);
			calAts020.setAtsParamType(paramType);
			calAts020.setAtsScore(ats020_points);

			return calAts020;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts020;
		}
	}

}
