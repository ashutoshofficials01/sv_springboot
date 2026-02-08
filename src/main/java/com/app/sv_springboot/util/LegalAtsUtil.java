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
public class LegalAtsUtil {

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

	private List<Map<String, Object>> generalLegalATSParameterList;

	private Map<String, Map<String, Object>> generalLegalATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadLegal_ATSParameter() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Legal_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalLegalATSParameterList = (List<Map<String, Object>>) jsonData.get("Legal_ATSParameter");

			// Create fast-lookup map
			generalLegalATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalLegalATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalLegalATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalLegalATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Legal_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalLegalATSParameterList :: " + generalLegalATSParameterList);
		return generalLegalATSParameterList;
	}

	public Map<String, Object> getGeneralLegalATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralLegalATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalLegalATSParameterMap :: " + generalLegalATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalLegalATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}
	

	public AtsListDto calculateATS241(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts241 = new AtsListDto();
		AtsGenParamDto calAtsGen241 = new AtsGenParamDto();
		long ats241_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats241ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);
			if (ats241ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats241ParamData.get("logic_description");

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
					System.out.println("ATS-241 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats241_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats241_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats241_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats241_points == max) {
				paramType = "positive";
			} else if (ats241_points == partial) {
				paramType = "partial";
			} else if (ats241_points == 0) {
				paramType = "negative";
			}

			calAtsGen241.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen241.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen241.setCategory(genATSParamData.getCategory());
			calAtsGen241.setDescription(genATSParamData.getDescription());
			calAtsGen241.setMax_points(genATSParamData.getMax_points());
			calAtsGen241.setParameter(genATSParamData.getParameter());
			calAtsGen241.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen241.setTotal_points(genATSParamData.getTotal_points());

			calAts241.setAtsGeneralId(atsGeneralId);
			calAts241.setAtsGeneralParamDto(calAtsGen241);
			calAts241.setAtsParamData(ats241ParamData);
			calAts241.setAtsParamId(atsParamId);
			calAts241.setAtsParamType(paramType);
			calAts241.setAtsScore(ats241_points);

			return calAts241;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts241;
		}
	}

	public AtsListDto calculateATS242(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts242 = new AtsListDto();
		AtsGenParamDto calAtsGen242 = new AtsGenParamDto();
		long ats242_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats242ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats242ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats242ParamData.get("logic_description");

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
						System.out.println("ATS-242 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats242_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats242_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats242_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats242_points == max) {
				paramType = "positive";
			} else if (ats242_points == partial) {
				paramType = "partial";
			} else if (ats242_points == 0) {
				paramType = "negative";
			}

			calAtsGen242.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen242.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen242.setCategory(genATSParamData.getCategory());
			calAtsGen242.setDescription(genATSParamData.getDescription());
			calAtsGen242.setMax_points(genATSParamData.getMax_points());
			calAtsGen242.setParameter(genATSParamData.getParameter());
			calAtsGen242.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen242.setTotal_points(genATSParamData.getTotal_points());

			calAts242.setAtsGeneralId(atsGeneralId);
			calAts242.setAtsGeneralParamDto(calAtsGen242);
			calAts242.setAtsParamData(ats242ParamData);
			calAts242.setAtsParamId(atsParamId);
			calAts242.setAtsParamType(paramType);
			calAts242.setAtsScore(ats242_points);

			return calAts242;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts242;
		}
	}

	public AtsListDto calculateATS243(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts243 = new AtsListDto();
		AtsGenParamDto calAtsGen243 = new AtsGenParamDto();
		long ats243_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats243ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats243ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats243ParamData.get("logic_description");

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
						System.out.println("ATS-243 :: " + alternate);
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
				ats243_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats243_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats243_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats243_points == max) {
				paramType = "positive";
			} else if (ats243_points == partial) {
				paramType = "partial";
			} else if (ats243_points == 0) {
				paramType = "negative";
			}

			calAtsGen243.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen243.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen243.setCategory(genATSParamData.getCategory());
			calAtsGen243.setDescription(genATSParamData.getDescription());
			calAtsGen243.setMax_points(genATSParamData.getMax_points());
			calAtsGen243.setParameter(genATSParamData.getParameter());
			calAtsGen243.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen243.setTotal_points(genATSParamData.getTotal_points());

			calAts243.setAtsGeneralId(atsGeneralId);
			calAts243.setAtsGeneralParamDto(calAtsGen243);
			calAts243.setAtsParamData(ats243ParamData);
			calAts243.setAtsParamId(atsParamId);
			calAts243.setAtsParamType(paramType);
			calAts243.setAtsScore(ats243_points);

			return calAts243;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts243;
		}
	}

	public AtsListDto calculateATS244(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts244 = new AtsListDto();
		AtsGenParamDto calAtsGen244 = new AtsGenParamDto();
		long ats244_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats244ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats244ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats244ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-244 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats244_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats244_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats244_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats244_points == max) {
				paramType = "positive";
			} else if (ats244_points == partial) {
				paramType = "partial";
			} else if (ats244_points == 0) {
				paramType = "negative";
			}

			calAtsGen244.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen244.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen244.setCategory(genATSParamData.getCategory());
			calAtsGen244.setDescription(genATSParamData.getDescription());
			calAtsGen244.setMax_points(genATSParamData.getMax_points());
			calAtsGen244.setParameter(genATSParamData.getParameter());
			calAtsGen244.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen244.setTotal_points(genATSParamData.getTotal_points());

			calAts244.setAtsGeneralId(atsGeneralId);
			calAts244.setAtsGeneralParamDto(calAtsGen244);
			calAts244.setAtsParamData(ats244ParamData);
			calAts244.setAtsParamId(atsParamId);
			calAts244.setAtsParamType(paramType);
			calAts244.setAtsScore(ats244_points);

			return calAts244;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts244;
		}
	}

	public AtsListDto calculateATS245(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts245 = new AtsListDto();
		AtsGenParamDto calAtsGen245 = new AtsGenParamDto();
		long ats245_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats245ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats245ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats245ParamData.get("logic_description");

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
						System.out.println("ATS-245 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats245_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats245_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats245_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats245_points == max) {
				paramType = "positive";
			} else if (ats245_points == partial) {
				paramType = "partial";
			} else if (ats245_points == 0) {
				paramType = "negative";
			}

			calAtsGen245.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen245.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen245.setCategory(genATSParamData.getCategory());
			calAtsGen245.setDescription(genATSParamData.getDescription());
			calAtsGen245.setMax_points(genATSParamData.getMax_points());
			calAtsGen245.setParameter(genATSParamData.getParameter());
			calAtsGen245.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen245.setTotal_points(genATSParamData.getTotal_points());

			calAts245.setAtsGeneralId(atsGeneralId);
			calAts245.setAtsGeneralParamDto(calAtsGen245);
			calAts245.setAtsParamData(ats245ParamData);
			calAts245.setAtsParamId(atsParamId);
			calAts245.setAtsParamType(paramType);
			calAts245.setAtsScore(ats245_points);

			return calAts245;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts245;
		}
	}

	public AtsListDto calculateATS246(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts246 = new AtsListDto();
		AtsGenParamDto calAtsGen246 = new AtsGenParamDto();
		long ats246_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats246ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats246ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats246ParamData.get("logic_description");

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

			System.out.println("ATS-246 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats246ParamData);
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
				ats246_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats246_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats246_points = fullScore;
			} else {
				ats246_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats246_points == max) {
				paramType = "positive";
			} else if (ats246_points == partial) {
				paramType = "partial";
			} else if (ats246_points == 0) {
				paramType = "negative";
			}

			calAtsGen246.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen246.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen246.setCategory(genATSParamData.getCategory());
			calAtsGen246.setDescription(genATSParamData.getDescription());
			calAtsGen246.setMax_points(genATSParamData.getMax_points());
			calAtsGen246.setParameter(genATSParamData.getParameter());
			calAtsGen246.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen246.setTotal_points(genATSParamData.getTotal_points());

			calAts246.setAtsGeneralId(atsGeneralId);
			calAts246.setAtsGeneralParamDto(calAtsGen246);
			calAts246.setAtsParamData(ats246ParamData);
			calAts246.setAtsParamId(atsParamId);
			calAts246.setAtsParamType(paramType);
			calAts246.setAtsScore(ats246_points);

			return calAts246;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts246;
		}
	}

	public AtsListDto calculateATS247(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts247 = new AtsListDto();
		AtsGenParamDto calAtsGen247 = new AtsGenParamDto();
		long ats247_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats247ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats247ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats247ParamData.get("logic_description");

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
					System.out.println("ATS-247 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats247_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats247_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats247_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats247_points == max) {
				paramType = "positive";
			} else if (ats247_points == partial) {
				paramType = "partial";
			} else if (ats247_points == 0) {
				paramType = "negative";
			}

			calAtsGen247.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen247.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen247.setCategory(genATSParamData.getCategory());
			calAtsGen247.setDescription(genATSParamData.getDescription());
			calAtsGen247.setMax_points(genATSParamData.getMax_points());
			calAtsGen247.setParameter(genATSParamData.getParameter());
			calAtsGen247.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen247.setTotal_points(genATSParamData.getTotal_points());

			calAts247.setAtsGeneralId(atsGeneralId);
			calAts247.setAtsGeneralParamDto(calAtsGen247);
			calAts247.setAtsParamData(ats247ParamData);
			calAts247.setAtsParamId(atsParamId);
			calAts247.setAtsParamType(paramType);
			calAts247.setAtsScore(ats247_points);

			return calAts247;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts247;
		}
	}

	public AtsListDto calculateATS248(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts248 = new AtsListDto();
		AtsGenParamDto calAtsGen248 = new AtsGenParamDto();
		long ats248_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats248ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats248ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats248ParamData.get("logic_description");

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
						System.out.println("ATS-248 :: " + key);
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
				ats248_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats248_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats248_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats248_points == max) {
				paramType = "positive";
			} else if (ats248_points == partial) {
				paramType = "partial";
			} else if (ats248_points == 0) {
				paramType = "negative";
			}

			calAtsGen248.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen248.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen248.setCategory(genATSParamData.getCategory());
			calAtsGen248.setDescription(genATSParamData.getDescription());
			calAtsGen248.setMax_points(genATSParamData.getMax_points());
			calAtsGen248.setParameter(genATSParamData.getParameter());
			calAtsGen248.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen248.setTotal_points(genATSParamData.getTotal_points());

			calAts248.setAtsGeneralId(atsGeneralId);
			calAts248.setAtsGeneralParamDto(calAtsGen248);
			calAts248.setAtsParamData(ats248ParamData);
			calAts248.setAtsParamId(atsParamId);
			calAts248.setAtsParamType(paramType);
			calAts248.setAtsScore(ats248_points);

			return calAts248;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts248;
		}
	}

	public AtsListDto calculateATS249(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts249 = new AtsListDto();
		AtsGenParamDto calAtsGen249 = new AtsGenParamDto();
		long ats249_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats249ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats249ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats249ParamData.get("logic_description");

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
						System.out.println("ATS-249 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats249_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats249_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats249_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats249_points == max) {
				paramType = "positive";
			} else if (ats249_points == partial) {
				paramType = "partial";
			} else if (ats249_points == 0) {
				paramType = "negative";
			}

			calAtsGen249.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen249.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen249.setCategory(genATSParamData.getCategory());
			calAtsGen249.setDescription(genATSParamData.getDescription());
			calAtsGen249.setMax_points(genATSParamData.getMax_points());
			calAtsGen249.setParameter(genATSParamData.getParameter());
			calAtsGen249.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen249.setTotal_points(genATSParamData.getTotal_points());

			calAts249.setAtsGeneralId(atsGeneralId);
			calAts249.setAtsGeneralParamDto(calAtsGen249);
			calAts249.setAtsParamData(ats249ParamData);
			calAts249.setAtsParamId(atsParamId);
			calAts249.setAtsParamType(paramType);
			calAts249.setAtsScore(ats249_points);

			return calAts249;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts249;
		}
	}

	public AtsListDto calculateATS250(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts250 = new AtsListDto();
		AtsGenParamDto calAtsGen250 = new AtsGenParamDto();
		long ats250_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats250ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats250ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats250ParamData.get("logic_description");

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
					System.out.println("ATS-250 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats250_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats250_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats250_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats250_points == max) {
				paramType = "positive";
			} else if (ats250_points == partial) {
				paramType = "partial";
			} else if (ats250_points == 0) {
				paramType = "negative";
			}

			calAtsGen250.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen250.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen250.setCategory(genATSParamData.getCategory());
			calAtsGen250.setDescription(genATSParamData.getDescription());
			calAtsGen250.setMax_points(genATSParamData.getMax_points());
			calAtsGen250.setParameter(genATSParamData.getParameter());
			calAtsGen250.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen250.setTotal_points(genATSParamData.getTotal_points());

			calAts250.setAtsGeneralId(atsGeneralId);
			calAts250.setAtsGeneralParamDto(calAtsGen250);
			calAts250.setAtsParamData(ats250ParamData);
			calAts250.setAtsParamId(atsParamId);
			calAts250.setAtsParamType(paramType);
			calAts250.setAtsScore(ats250_points);

			return calAts250;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts250;
		}
	}

	public AtsListDto calculateATS251(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts251 = new AtsListDto();
		AtsGenParamDto calAtsGen251 = new AtsGenParamDto();
		long ats251_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats251ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats251ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats251ParamData.get("logic_description");

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
					System.out.println("ATS-251 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats251_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats251_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats251_points == max) {
				paramType = "positive";
			} else if (ats251_points == partial) {
				paramType = "partial";
			} else if (ats251_points == 0) {
				paramType = "negative";
			}

			calAtsGen251.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen251.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen251.setCategory(genATSParamData.getCategory());
			calAtsGen251.setDescription(genATSParamData.getDescription());
			calAtsGen251.setMax_points(genATSParamData.getMax_points());
			calAtsGen251.setParameter(genATSParamData.getParameter());
			calAtsGen251.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen251.setTotal_points(genATSParamData.getTotal_points());

			calAts251.setAtsGeneralId(atsGeneralId);
			calAts251.setAtsGeneralParamDto(calAtsGen251);
			calAts251.setAtsParamData(ats251ParamData);
			calAts251.setAtsParamId(atsParamId);
			calAts251.setAtsParamType(paramType);
			calAts251.setAtsScore(ats251_points);

			return calAts251;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts251;
		}
	}

	public AtsListDto calculateATS252(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts252 = new AtsListDto();
		AtsGenParamDto calAtsGen252 = new AtsGenParamDto();
		long ats252_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats252ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats252ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats252ParamData.get("logic_description");

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
						System.out.println("ATS-252 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 524ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats252_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats252_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats252_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats252_points == max) {
				paramType = "positive";
			} else if (ats252_points == partial) {
				paramType = "partial";
			} else if (ats252_points == 0) {
				paramType = "negative";
			}

			calAtsGen252.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen252.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen252.setCategory(genATSParamData.getCategory());
			calAtsGen252.setDescription(genATSParamData.getDescription());
			calAtsGen252.setMax_points(genATSParamData.getMax_points());
			calAtsGen252.setParameter(genATSParamData.getParameter());
			calAtsGen252.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen252.setTotal_points(genATSParamData.getTotal_points());

			calAts252.setAtsGeneralId(atsGeneralId);
			calAts252.setAtsGeneralParamDto(calAtsGen252);
			calAts252.setAtsParamData(ats252ParamData);
			calAts252.setAtsParamId(atsParamId);
			calAts252.setAtsParamType(paramType);
			calAts252.setAtsScore(ats252_points);

			return calAts252;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts252;
		}
	}

	public AtsListDto calculateATS253(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts253 = new AtsListDto();
		AtsGenParamDto calAtsGen253 = new AtsGenParamDto();
		long ats253_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats253ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats253ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats253ParamData.get("logic_description");

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
						System.out.println("ATS-253 :: " + key);
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
				ats253_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats253_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats253_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats253_points == max) {
				paramType = "positive";
			} else if (ats253_points == partial) {
				paramType = "partial";
			} else if (ats253_points == 0) {
				paramType = "negative";
			}

			calAtsGen253.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen253.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen253.setCategory(genATSParamData.getCategory());
			calAtsGen253.setDescription(genATSParamData.getDescription());
			calAtsGen253.setMax_points(genATSParamData.getMax_points());
			calAtsGen253.setParameter(genATSParamData.getParameter());
			calAtsGen253.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen253.setTotal_points(genATSParamData.getTotal_points());

			calAts253.setAtsGeneralId(atsGeneralId);
			calAts253.setAtsGeneralParamDto(calAtsGen253);
			calAts253.setAtsParamData(ats253ParamData);
			calAts253.setAtsParamId(atsParamId);
			calAts253.setAtsParamType(paramType);
			calAts253.setAtsScore(ats253_points);

			return calAts253;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts253;
		}
	}

	public AtsListDto calculateATS254(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts254 = new AtsListDto();
		AtsGenParamDto calAtsGen254 = new AtsGenParamDto();
		long ats254_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats254ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats254ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats254ParamData.get("logic_description");

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
						System.out.println("ATS-254 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats254_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats254_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats254_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats254_points == max) {
				paramType = "positive";
			} else if (ats254_points == partial) {
				paramType = "partial";
			} else if (ats254_points == 0) {
				paramType = "negative";
			}

			calAtsGen254.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen254.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen254.setCategory(genATSParamData.getCategory());
			calAtsGen254.setDescription(genATSParamData.getDescription());
			calAtsGen254.setMax_points(genATSParamData.getMax_points());
			calAtsGen254.setParameter(genATSParamData.getParameter());
			calAtsGen254.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen254.setTotal_points(genATSParamData.getTotal_points());

			calAts254.setAtsGeneralId(atsGeneralId);
			calAts254.setAtsGeneralParamDto(calAtsGen254);
			calAts254.setAtsParamData(ats254ParamData);
			calAts254.setAtsParamId(atsParamId);
			calAts254.setAtsParamType(paramType);
			calAts254.setAtsScore(ats254_points);

			return calAts254;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts254;
		}
	}

	public AtsListDto calculateATS255(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts255 = new AtsListDto();
		AtsGenParamDto calAtsGen255 = new AtsGenParamDto();
		long ats255_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats255ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats255ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats255ParamData.get("logic_description");

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
						System.out.println("ATS-255 :: " + key);
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
				ats255_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats255_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats255_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats255_points == max) {
				paramType = "positive";
			} else if (ats255_points == partial) {
				paramType = "partial";
			} else if (ats255_points == 0) {
				paramType = "negative";
			}

			calAtsGen255.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen255.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen255.setCategory(genATSParamData.getCategory());
			calAtsGen255.setDescription(genATSParamData.getDescription());
			calAtsGen255.setMax_points(genATSParamData.getMax_points());
			calAtsGen255.setParameter(genATSParamData.getParameter());
			calAtsGen255.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen255.setTotal_points(genATSParamData.getTotal_points());

			calAts255.setAtsGeneralId(atsGeneralId);
			calAts255.setAtsGeneralParamDto(calAtsGen255);
			calAts255.setAtsParamData(ats255ParamData);
			calAts255.setAtsParamId(atsParamId);
			calAts255.setAtsParamType(paramType);
			calAts255.setAtsScore(ats255_points);

			return calAts255;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts255;
		}
	}

	public AtsListDto calculateATS256(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts256 = new AtsListDto();
		AtsGenParamDto calAtsGen256 = new AtsGenParamDto();
		long ats256_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats256ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats256ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats256ParamData.get("logic_description");

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
						System.out.println("ATS-256 :: " + key);
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
				ats256_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats256_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats256_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats256_points == max) {
				paramType = "positive";
			} else if (ats256_points == partial) {
				paramType = "partial";
			} else if (ats256_points == 0) {
				paramType = "negative";
			}

			calAtsGen256.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen256.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen256.setCategory(genATSParamData.getCategory());
			calAtsGen256.setDescription(genATSParamData.getDescription());
			calAtsGen256.setMax_points(genATSParamData.getMax_points());
			calAtsGen256.setParameter(genATSParamData.getParameter());
			calAtsGen256.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen256.setTotal_points(genATSParamData.getTotal_points());

			calAts256.setAtsGeneralId(atsGeneralId);
			calAts256.setAtsGeneralParamDto(calAtsGen256);
			calAts256.setAtsParamData(ats256ParamData);
			calAts256.setAtsParamId(atsParamId);
			calAts256.setAtsParamType(paramType);
			calAts256.setAtsScore(ats256_points);

			return calAts256;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts256;
		}
	}

	public AtsListDto calculateATS257(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts257 = new AtsListDto();
		AtsGenParamDto calAtsGen257 = new AtsGenParamDto();
		long ats257_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats257ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats257ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats257ParamData.get("logic_description");

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
						System.out.println("ATS-257 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats257_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats257_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats257_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats257_points == max) {
				paramType = "positive";
			} else if (ats257_points == partial) {
				paramType = "partial";
			} else if (ats257_points == 0) {
				paramType = "negative";
			}

			calAtsGen257.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen257.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen257.setCategory(genATSParamData.getCategory());
			calAtsGen257.setDescription(genATSParamData.getDescription());
			calAtsGen257.setMax_points(genATSParamData.getMax_points());
			calAtsGen257.setParameter(genATSParamData.getParameter());
			calAtsGen257.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen257.setTotal_points(genATSParamData.getTotal_points());

			calAts257.setAtsGeneralId(atsGeneralId);
			calAts257.setAtsGeneralParamDto(calAtsGen257);
			calAts257.setAtsParamData(ats257ParamData);
			calAts257.setAtsParamId(atsParamId);
			calAts257.setAtsParamType(paramType);
			calAts257.setAtsScore(ats257_points);

			return calAts257;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts257;
		}
	}

	public AtsListDto calculateATS258(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts258 = new AtsListDto();
		AtsGenParamDto calAtsGen258 = new AtsGenParamDto();
		long ats258_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats258ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats258ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats258ParamData.get("logic_description");
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
						System.out.println("ATS-258 :: " + errorPattern.toLowerCase());
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
				ats258_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats258_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats258_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats258_points == max) {
				paramType = "positive";
			} else if (ats258_points == partial) {
				paramType = "partial";
			} else if (ats258_points == 0) {
				paramType = "negative";
			}

			calAtsGen258.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen258.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen258.setCategory(genATSParamData.getCategory());
			calAtsGen258.setDescription(genATSParamData.getDescription());
			calAtsGen258.setMax_points(genATSParamData.getMax_points());
			calAtsGen258.setParameter(genATSParamData.getParameter());
			calAtsGen258.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen258.setTotal_points(genATSParamData.getTotal_points());

			calAts258.setAtsGeneralId(atsGeneralId);
			calAts258.setAtsGeneralParamDto(calAtsGen258);
			calAts258.setAtsParamData(ats258ParamData);
			calAts258.setAtsParamId(atsParamId);
			calAts258.setAtsParamType(paramType);
			calAts258.setAtsScore(ats258_points);

			return calAts258;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts258;
		}
	}

	public AtsListDto calculateATS259(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts259 = new AtsListDto();
		AtsGenParamDto calAtsGen259 = new AtsGenParamDto();
		long ats259_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats259ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats259ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats259ParamData.get("logic_description");

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
						System.out.println("ATS-259 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats259_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats259_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats259_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats259_points == max) {
				paramType = "positive";
			} else if (ats259_points == partial) {
				paramType = "partial";
			} else if (ats259_points == 0) {
				paramType = "negative";
			}

			calAtsGen259.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen259.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen259.setCategory(genATSParamData.getCategory());
			calAtsGen259.setDescription(genATSParamData.getDescription());
			calAtsGen259.setMax_points(genATSParamData.getMax_points());
			calAtsGen259.setParameter(genATSParamData.getParameter());
			calAtsGen259.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen259.setTotal_points(genATSParamData.getTotal_points());

			calAts259.setAtsGeneralId(atsGeneralId);
			calAts259.setAtsGeneralParamDto(calAtsGen259);
			calAts259.setAtsParamData(ats259ParamData);
			calAts259.setAtsParamId(atsParamId);
			calAts259.setAtsParamType(paramType);
			calAts259.setAtsScore(ats259_points);

			return calAts259;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts259;
		}
	}

	public AtsListDto calculateATS260(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts260 = new AtsListDto();
		AtsGenParamDto calAtsGen260 = new AtsGenParamDto();

		long ats260_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats260ParamData = getGeneralLegalATSParam(atsGeneralId, atsParamId);

			if (ats260ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats260ParamData.get("logic_description");

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
								System.out.println("ATS-260 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats260_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats260_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats260_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats260_points == max) {
				paramType = "positive";
			} else if (ats260_points == partial) {
				paramType = "partial";
			} else if (ats260_points == 0) {
				paramType = "negative";
			}

			calAtsGen260.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen260.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen260.setCategory(genATSParamData.getCategory());
			calAtsGen260.setDescription(genATSParamData.getDescription());
			calAtsGen260.setMax_points(genATSParamData.getMax_points());
			calAtsGen260.setParameter(genATSParamData.getParameter());
			calAtsGen260.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen260.setTotal_points(genATSParamData.getTotal_points());

			calAts260.setAtsGeneralId(atsGeneralId);
			calAts260.setAtsGeneralParamDto(calAtsGen260);
			calAts260.setAtsParamData(ats260ParamData);
			calAts260.setAtsParamId(atsParamId);
			calAts260.setAtsParamType(paramType);
			calAts260.setAtsScore(ats260_points);

			return calAts260;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts260;
		}
	}

}
