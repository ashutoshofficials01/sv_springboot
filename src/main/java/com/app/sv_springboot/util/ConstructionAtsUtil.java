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
public class ConstructionAtsUtil {

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

	private List<Map<String, Object>> generalConstructionATSParameterList;

	private Map<String, Map<String, Object>> generalConstructionATSParameterMap;

	@PostConstruct
	public void loadConstructionATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Construction_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalConstructionATSParameterList = (List<Map<String, Object>>) jsonData.get("Construction_ATSParameter");

			// Create fast-lookup map
			generalConstructionATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalConstructionATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalConstructionATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalConstructionATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load generalConstructionATSParameters.json", e);
		}
	}

	public List<Map<String, Object>> getAllConstructionGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalATSParameterList :: " + generalATSParameterList);
		return generalConstructionATSParameterList;
	}

	public Map<String, Object> getGeneralConstructionATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralConstructionATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalATSParameterMap :: " + generalATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalConstructionATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	public AtsListDto calculateATS061(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts061 = new AtsListDto();
		AtsGenParamDto calAtsGen061 = new AtsGenParamDto();
		long ats061_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats061ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);
			if (ats061ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats061ParamData.get("logic_description");

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
					System.out.println("ATS-061 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats061_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats061_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats061_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats061_points == max) {
				paramType = "positive";
			} else if (ats061_points == partial) {
				paramType = "partial";
			} else if (ats061_points == 0) {
				paramType = "negative";
			}

			calAtsGen061.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen061.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen061.setCategory(genATSParamData.getCategory());
			calAtsGen061.setDescription(genATSParamData.getDescription());
			calAtsGen061.setMax_points(genATSParamData.getMax_points());
			calAtsGen061.setParameter(genATSParamData.getParameter());
			calAtsGen061.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen061.setTotal_points(genATSParamData.getTotal_points());

			calAts061.setAtsGeneralId(atsGeneralId);
			calAts061.setAtsGeneralParamDto(calAtsGen061);
			calAts061.setAtsParamData(ats061ParamData);
			calAts061.setAtsParamId(atsParamId);
			calAts061.setAtsParamType(paramType);
			calAts061.setAtsScore(ats061_points);

			return calAts061;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts061;
		}
	}

	public AtsListDto calculateATS062(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts062 = new AtsListDto();
		AtsGenParamDto calAtsGen062 = new AtsGenParamDto();
		long ats062_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats062ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats062ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats062ParamData.get("logic_description");

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
						System.out.println("ATS-062 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats062_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats062_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats062_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats062_points == max) {
				paramType = "positive";
			} else if (ats062_points == partial) {
				paramType = "partial";
			} else if (ats062_points == 0) {
				paramType = "negative";
			}

			calAtsGen062.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen062.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen062.setCategory(genATSParamData.getCategory());
			calAtsGen062.setDescription(genATSParamData.getDescription());
			calAtsGen062.setMax_points(genATSParamData.getMax_points());
			calAtsGen062.setParameter(genATSParamData.getParameter());
			calAtsGen062.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen062.setTotal_points(genATSParamData.getTotal_points());

			calAts062.setAtsGeneralId(atsGeneralId);
			calAts062.setAtsGeneralParamDto(calAtsGen062);
			calAts062.setAtsParamData(ats062ParamData);
			calAts062.setAtsParamId(atsParamId);
			calAts062.setAtsParamType(paramType);
			calAts062.setAtsScore(ats062_points);

			return calAts062;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts062;
		}
	}

	public AtsListDto calculateATS063(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts063 = new AtsListDto();
		AtsGenParamDto calAtsGen063 = new AtsGenParamDto();
		long ats063_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats063ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats063ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats063ParamData.get("logic_description");

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
						System.out.println("ATS-063 :: " + alternate);
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
				ats063_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats063_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats063_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats063_points == max) {
				paramType = "positive";
			} else if (ats063_points == partial) {
				paramType = "partial";
			} else if (ats063_points == 0) {
				paramType = "negative";
			}

			calAtsGen063.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen063.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen063.setCategory(genATSParamData.getCategory());
			calAtsGen063.setDescription(genATSParamData.getDescription());
			calAtsGen063.setMax_points(genATSParamData.getMax_points());
			calAtsGen063.setParameter(genATSParamData.getParameter());
			calAtsGen063.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen063.setTotal_points(genATSParamData.getTotal_points());

			calAts063.setAtsGeneralId(atsGeneralId);
			calAts063.setAtsGeneralParamDto(calAtsGen063);
			calAts063.setAtsParamData(ats063ParamData);
			calAts063.setAtsParamId(atsParamId);
			calAts063.setAtsParamType(paramType);
			calAts063.setAtsScore(ats063_points);

			return calAts063;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts063;
		}
	}

	public AtsListDto calculateATS064(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts064 = new AtsListDto();
		AtsGenParamDto calAtsGen064 = new AtsGenParamDto();
		long ats064_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats064ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats064ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats064ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-064 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats064_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats064_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats064_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats064_points == max) {
				paramType = "positive";
			} else if (ats064_points == partial) {
				paramType = "partial";
			} else if (ats064_points == 0) {
				paramType = "negative";
			}

			calAtsGen064.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen064.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen064.setCategory(genATSParamData.getCategory());
			calAtsGen064.setDescription(genATSParamData.getDescription());
			calAtsGen064.setMax_points(genATSParamData.getMax_points());
			calAtsGen064.setParameter(genATSParamData.getParameter());
			calAtsGen064.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen064.setTotal_points(genATSParamData.getTotal_points());

			calAts064.setAtsGeneralId(atsGeneralId);
			calAts064.setAtsGeneralParamDto(calAtsGen064);
			calAts064.setAtsParamData(ats064ParamData);
			calAts064.setAtsParamId(atsParamId);
			calAts064.setAtsParamType(paramType);
			calAts064.setAtsScore(ats064_points);

			return calAts064;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts064;
		}
	}

	public AtsListDto calculateATS065(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts065 = new AtsListDto();
		AtsGenParamDto calAtsGen065 = new AtsGenParamDto();
		long ats065_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats065ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats065ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats065ParamData.get("logic_description");

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
						System.out.println("ATS-065 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats065_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats065_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats065_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats065_points == max) {
				paramType = "positive";
			} else if (ats065_points == partial) {
				paramType = "partial";
			} else if (ats065_points == 0) {
				paramType = "negative";
			}

			calAtsGen065.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen065.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen065.setCategory(genATSParamData.getCategory());
			calAtsGen065.setDescription(genATSParamData.getDescription());
			calAtsGen065.setMax_points(genATSParamData.getMax_points());
			calAtsGen065.setParameter(genATSParamData.getParameter());
			calAtsGen065.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen065.setTotal_points(genATSParamData.getTotal_points());

			calAts065.setAtsGeneralId(atsGeneralId);
			calAts065.setAtsGeneralParamDto(calAtsGen065);
			calAts065.setAtsParamData(ats065ParamData);
			calAts065.setAtsParamId(atsParamId);
			calAts065.setAtsParamType(paramType);
			calAts065.setAtsScore(ats065_points);

			return calAts065;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts065;
		}
	}

	public AtsListDto calculateATS066(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts066 = new AtsListDto();
		AtsGenParamDto calAtsGen066 = new AtsGenParamDto();
		long ats066_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats066ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats066ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats066ParamData.get("logic_description");

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

			System.out.println("ATS-066 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats066ParamData);
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
				ats066_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats066_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats066_points = fullScore;
			} else {
				ats066_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats066_points == max) {
				paramType = "positive";
			} else if (ats066_points == partial) {
				paramType = "partial";
			} else if (ats066_points == 0) {
				paramType = "negative";
			}

			calAtsGen066.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen066.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen066.setCategory(genATSParamData.getCategory());
			calAtsGen066.setDescription(genATSParamData.getDescription());
			calAtsGen066.setMax_points(genATSParamData.getMax_points());
			calAtsGen066.setParameter(genATSParamData.getParameter());
			calAtsGen066.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen066.setTotal_points(genATSParamData.getTotal_points());

			calAts066.setAtsGeneralId(atsGeneralId);
			calAts066.setAtsGeneralParamDto(calAtsGen066);
			calAts066.setAtsParamData(ats066ParamData);
			calAts066.setAtsParamId(atsParamId);
			calAts066.setAtsParamType(paramType);
			calAts066.setAtsScore(ats066_points);

			return calAts066;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts066;
		}
	}

	public AtsListDto calculateATS067(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts067 = new AtsListDto();
		AtsGenParamDto calAtsGen067 = new AtsGenParamDto();
		long ats067_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats067ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats067ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats067ParamData.get("logic_description");

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
					System.out.println("ATS-067 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats067_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats067_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats067_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats067_points == max) {
				paramType = "positive";
			} else if (ats067_points == partial) {
				paramType = "partial";
			} else if (ats067_points == 0) {
				paramType = "negative";
			}

			calAtsGen067.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen067.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen067.setCategory(genATSParamData.getCategory());
			calAtsGen067.setDescription(genATSParamData.getDescription());
			calAtsGen067.setMax_points(genATSParamData.getMax_points());
			calAtsGen067.setParameter(genATSParamData.getParameter());
			calAtsGen067.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen067.setTotal_points(genATSParamData.getTotal_points());

			calAts067.setAtsGeneralId(atsGeneralId);
			calAts067.setAtsGeneralParamDto(calAtsGen067);
			calAts067.setAtsParamData(ats067ParamData);
			calAts067.setAtsParamId(atsParamId);
			calAts067.setAtsParamType(paramType);
			calAts067.setAtsScore(ats067_points);

			return calAts067;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts067;
		}
	}

	public AtsListDto calculateATS068(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts068 = new AtsListDto();
		AtsGenParamDto calAtsGen068 = new AtsGenParamDto();
		long ats068_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats068ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats068ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats068ParamData.get("logic_description");

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
						System.out.println("ATS-068 :: " + key);
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
				ats068_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats068_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats068_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats068_points == max) {
				paramType = "positive";
			} else if (ats068_points == partial) {
				paramType = "partial";
			} else if (ats068_points == 0) {
				paramType = "negative";
			}

			calAtsGen068.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen068.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen068.setCategory(genATSParamData.getCategory());
			calAtsGen068.setDescription(genATSParamData.getDescription());
			calAtsGen068.setMax_points(genATSParamData.getMax_points());
			calAtsGen068.setParameter(genATSParamData.getParameter());
			calAtsGen068.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen068.setTotal_points(genATSParamData.getTotal_points());

			calAts068.setAtsGeneralId(atsGeneralId);
			calAts068.setAtsGeneralParamDto(calAtsGen068);
			calAts068.setAtsParamData(ats068ParamData);
			calAts068.setAtsParamId(atsParamId);
			calAts068.setAtsParamType(paramType);
			calAts068.setAtsScore(ats068_points);

			return calAts068;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts068;
		}
	}

	public AtsListDto calculateATS069(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts069 = new AtsListDto();
		AtsGenParamDto calAtsGen069 = new AtsGenParamDto();
		long ats069_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats069ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats069ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats069ParamData.get("logic_description");

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
						System.out.println("ATS-069 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats069_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats069_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats069_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats069_points == max) {
				paramType = "positive";
			} else if (ats069_points == partial) {
				paramType = "partial";
			} else if (ats069_points == 0) {
				paramType = "negative";
			}

			calAtsGen069.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen069.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen069.setCategory(genATSParamData.getCategory());
			calAtsGen069.setDescription(genATSParamData.getDescription());
			calAtsGen069.setMax_points(genATSParamData.getMax_points());
			calAtsGen069.setParameter(genATSParamData.getParameter());
			calAtsGen069.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen069.setTotal_points(genATSParamData.getTotal_points());

			calAts069.setAtsGeneralId(atsGeneralId);
			calAts069.setAtsGeneralParamDto(calAtsGen069);
			calAts069.setAtsParamData(ats069ParamData);
			calAts069.setAtsParamId(atsParamId);
			calAts069.setAtsParamType(paramType);
			calAts069.setAtsScore(ats069_points);

			return calAts069;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts069;
		}
	}

	public AtsListDto calculateATS070(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts070 = new AtsListDto();
		AtsGenParamDto calAtsGen070 = new AtsGenParamDto();
		long ats070_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats070ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats070ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats070ParamData.get("logic_description");

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
					System.out.println("ATS-070 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats070_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats070_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats070_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats070_points == max) {
				paramType = "positive";
			} else if (ats070_points == partial) {
				paramType = "partial";
			} else if (ats070_points == 0) {
				paramType = "negative";
			}

			calAtsGen070.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen070.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen070.setCategory(genATSParamData.getCategory());
			calAtsGen070.setDescription(genATSParamData.getDescription());
			calAtsGen070.setMax_points(genATSParamData.getMax_points());
			calAtsGen070.setParameter(genATSParamData.getParameter());
			calAtsGen070.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen070.setTotal_points(genATSParamData.getTotal_points());

			calAts070.setAtsGeneralId(atsGeneralId);
			calAts070.setAtsGeneralParamDto(calAtsGen070);
			calAts070.setAtsParamData(ats070ParamData);
			calAts070.setAtsParamId(atsParamId);
			calAts070.setAtsParamType(paramType);
			calAts070.setAtsScore(ats070_points);

			return calAts070;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts070;
		}
	}

	public AtsListDto calculateATS071(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts071 = new AtsListDto();
		AtsGenParamDto calAtsGen071 = new AtsGenParamDto();
		long ats071_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats071ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats071ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats071ParamData.get("logic_description");

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
					System.out.println("ATS-071 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats071_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats071_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats071_points == max) {
				paramType = "positive";
			} else if (ats071_points == partial) {
				paramType = "partial";
			} else if (ats071_points == 0) {
				paramType = "negative";
			}

			calAtsGen071.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen071.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen071.setCategory(genATSParamData.getCategory());
			calAtsGen071.setDescription(genATSParamData.getDescription());
			calAtsGen071.setMax_points(genATSParamData.getMax_points());
			calAtsGen071.setParameter(genATSParamData.getParameter());
			calAtsGen071.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen071.setTotal_points(genATSParamData.getTotal_points());

			calAts071.setAtsGeneralId(atsGeneralId);
			calAts071.setAtsGeneralParamDto(calAtsGen071);
			calAts071.setAtsParamData(ats071ParamData);
			calAts071.setAtsParamId(atsParamId);
			calAts071.setAtsParamType(paramType);
			calAts071.setAtsScore(ats071_points);

			return calAts071;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts071;
		}
	}

	public AtsListDto calculateATS072(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts072 = new AtsListDto();
		AtsGenParamDto calAtsGen072 = new AtsGenParamDto();
		long ats072_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats072ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats072ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats072ParamData.get("logic_description");

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
						System.out.println("ATS-072 :: " + key);
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
				ats072_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats072_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats072_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats072_points == max) {
				paramType = "positive";
			} else if (ats072_points == partial) {
				paramType = "partial";
			} else if (ats072_points == 0) {
				paramType = "negative";
			}

			calAtsGen072.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen072.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen072.setCategory(genATSParamData.getCategory());
			calAtsGen072.setDescription(genATSParamData.getDescription());
			calAtsGen072.setMax_points(genATSParamData.getMax_points());
			calAtsGen072.setParameter(genATSParamData.getParameter());
			calAtsGen072.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen072.setTotal_points(genATSParamData.getTotal_points());

			calAts072.setAtsGeneralId(atsGeneralId);
			calAts072.setAtsGeneralParamDto(calAtsGen072);
			calAts072.setAtsParamData(ats072ParamData);
			calAts072.setAtsParamId(atsParamId);
			calAts072.setAtsParamType(paramType);
			calAts072.setAtsScore(ats072_points);

			return calAts072;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts072;
		}
	}

	public AtsListDto calculateATS073(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts073 = new AtsListDto();
		AtsGenParamDto calAtsGen073 = new AtsGenParamDto();
		long ats073_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats073ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats073ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats073ParamData.get("logic_description");

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
						System.out.println("ATS-073 :: " + key);
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
				ats073_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats073_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats073_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats073_points == max) {
				paramType = "positive";
			} else if (ats073_points == partial) {
				paramType = "partial";
			} else if (ats073_points == 0) {
				paramType = "negative";
			}

			calAtsGen073.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen073.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen073.setCategory(genATSParamData.getCategory());
			calAtsGen073.setDescription(genATSParamData.getDescription());
			calAtsGen073.setMax_points(genATSParamData.getMax_points());
			calAtsGen073.setParameter(genATSParamData.getParameter());
			calAtsGen073.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen073.setTotal_points(genATSParamData.getTotal_points());

			calAts073.setAtsGeneralId(atsGeneralId);
			calAts073.setAtsGeneralParamDto(calAtsGen073);
			calAts073.setAtsParamData(ats073ParamData);
			calAts073.setAtsParamId(atsParamId);
			calAts073.setAtsParamType(paramType);
			calAts073.setAtsScore(ats073_points);

			return calAts073;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts073;
		}
	}

	public AtsListDto calculateATS074(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts074 = new AtsListDto();
		AtsGenParamDto calAtsGen074 = new AtsGenParamDto();
		long ats074_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats074ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats074ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats074ParamData.get("logic_description");

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
						System.out.println("ATS-074 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats074_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats074_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats074_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats074_points == max) {
				paramType = "positive";
			} else if (ats074_points == partial) {
				paramType = "partial";
			} else if (ats074_points == 0) {
				paramType = "negative";
			}

			calAtsGen074.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen074.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen074.setCategory(genATSParamData.getCategory());
			calAtsGen074.setDescription(genATSParamData.getDescription());
			calAtsGen074.setMax_points(genATSParamData.getMax_points());
			calAtsGen074.setParameter(genATSParamData.getParameter());
			calAtsGen074.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen074.setTotal_points(genATSParamData.getTotal_points());

			calAts074.setAtsGeneralId(atsGeneralId);
			calAts074.setAtsGeneralParamDto(calAtsGen074);
			calAts074.setAtsParamData(ats074ParamData);
			calAts074.setAtsParamId(atsParamId);
			calAts074.setAtsParamType(paramType);
			calAts074.setAtsScore(ats074_points);

			return calAts074;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts074;
		}
	}

	public AtsListDto calculateATS075(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts075 = new AtsListDto();
		AtsGenParamDto calAtsGen075 = new AtsGenParamDto();
		long ats075_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats075ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats075ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats075ParamData.get("logic_description");

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
						System.out.println("ATS-075 :: " + key);
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
				ats075_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats075_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats075_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats075_points == max) {
				paramType = "positive";
			} else if (ats075_points == partial) {
				paramType = "partial";
			} else if (ats075_points == 0) {
				paramType = "negative";
			}

			calAtsGen075.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen075.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen075.setCategory(genATSParamData.getCategory());
			calAtsGen075.setDescription(genATSParamData.getDescription());
			calAtsGen075.setMax_points(genATSParamData.getMax_points());
			calAtsGen075.setParameter(genATSParamData.getParameter());
			calAtsGen075.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen075.setTotal_points(genATSParamData.getTotal_points());

			calAts075.setAtsGeneralId(atsGeneralId);
			calAts075.setAtsGeneralParamDto(calAtsGen075);
			calAts075.setAtsParamData(ats075ParamData);
			calAts075.setAtsParamId(atsParamId);
			calAts075.setAtsParamType(paramType);
			calAts075.setAtsScore(ats075_points);

			return calAts075;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts075;
		}
	}

	public AtsListDto calculateATS076(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts076 = new AtsListDto();
		AtsGenParamDto calAtsGen076 = new AtsGenParamDto();
		long ats076_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats076ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats076ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats076ParamData.get("logic_description");

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
						System.out.println("ATS-076 :: " + key);
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
				ats076_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats076_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats076_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats076_points == max) {
				paramType = "positive";
			} else if (ats076_points == partial) {
				paramType = "partial";
			} else if (ats076_points == 0) {
				paramType = "negative";
			}

			calAtsGen076.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen076.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen076.setCategory(genATSParamData.getCategory());
			calAtsGen076.setDescription(genATSParamData.getDescription());
			calAtsGen076.setMax_points(genATSParamData.getMax_points());
			calAtsGen076.setParameter(genATSParamData.getParameter());
			calAtsGen076.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen076.setTotal_points(genATSParamData.getTotal_points());

			calAts076.setAtsGeneralId(atsGeneralId);
			calAts076.setAtsGeneralParamDto(calAtsGen076);
			calAts076.setAtsParamData(ats076ParamData);
			calAts076.setAtsParamId(atsParamId);
			calAts076.setAtsParamType(paramType);
			calAts076.setAtsScore(ats076_points);

			return calAts076;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts076;
		}
	}

	public AtsListDto calculateATS077(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts077 = new AtsListDto();
		AtsGenParamDto calAtsGen077 = new AtsGenParamDto();
		long ats077_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats077ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats077ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats077ParamData.get("logic_description");

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
						System.out.println("ATS-077 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats077_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats077_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats077_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats077_points == max) {
				paramType = "positive";
			} else if (ats077_points == partial) {
				paramType = "partial";
			} else if (ats077_points == 0) {
				paramType = "negative";
			}

			calAtsGen077.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen077.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen077.setCategory(genATSParamData.getCategory());
			calAtsGen077.setDescription(genATSParamData.getDescription());
			calAtsGen077.setMax_points(genATSParamData.getMax_points());
			calAtsGen077.setParameter(genATSParamData.getParameter());
			calAtsGen077.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen077.setTotal_points(genATSParamData.getTotal_points());

			calAts077.setAtsGeneralId(atsGeneralId);
			calAts077.setAtsGeneralParamDto(calAtsGen077);
			calAts077.setAtsParamData(ats077ParamData);
			calAts077.setAtsParamId(atsParamId);
			calAts077.setAtsParamType(paramType);
			calAts077.setAtsScore(ats077_points);

			return calAts077;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts077;
		}
	}

	public AtsListDto calculateATS078(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts078 = new AtsListDto();
		AtsGenParamDto calAtsGen078 = new AtsGenParamDto();
		long ats078_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats078ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats078ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats078ParamData.get("logic_description");
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
						System.out.println("ATS-078 :: " + errorPattern.toLowerCase());
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
				ats078_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats078_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats078_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats078_points == max) {
				paramType = "positive";
			} else if (ats078_points == partial) {
				paramType = "partial";
			} else if (ats078_points == 0) {
				paramType = "negative";
			}

			calAtsGen078.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen078.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen078.setCategory(genATSParamData.getCategory());
			calAtsGen078.setDescription(genATSParamData.getDescription());
			calAtsGen078.setMax_points(genATSParamData.getMax_points());
			calAtsGen078.setParameter(genATSParamData.getParameter());
			calAtsGen078.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen078.setTotal_points(genATSParamData.getTotal_points());

			calAts078.setAtsGeneralId(atsGeneralId);
			calAts078.setAtsGeneralParamDto(calAtsGen078);
			calAts078.setAtsParamData(ats078ParamData);
			calAts078.setAtsParamId(atsParamId);
			calAts078.setAtsParamType(paramType);
			calAts078.setAtsScore(ats078_points);

			return calAts078;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts078;
		}
	}

	public AtsListDto calculateATS079(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts079 = new AtsListDto();
		AtsGenParamDto calAtsGen079 = new AtsGenParamDto();
		long ats079_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats079ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats079ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats079ParamData.get("logic_description");

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
						System.out.println("ATS-079 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats079_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats079_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats079_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats079_points == max) {
				paramType = "positive";
			} else if (ats079_points == partial) {
				paramType = "partial";
			} else if (ats079_points == 0) {
				paramType = "negative";
			}

			calAtsGen079.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen079.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen079.setCategory(genATSParamData.getCategory());
			calAtsGen079.setDescription(genATSParamData.getDescription());
			calAtsGen079.setMax_points(genATSParamData.getMax_points());
			calAtsGen079.setParameter(genATSParamData.getParameter());
			calAtsGen079.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen079.setTotal_points(genATSParamData.getTotal_points());

			calAts079.setAtsGeneralId(atsGeneralId);
			calAts079.setAtsGeneralParamDto(calAtsGen079);
			calAts079.setAtsParamData(ats079ParamData);
			calAts079.setAtsParamId(atsParamId);
			calAts079.setAtsParamType(paramType);
			calAts079.setAtsScore(ats079_points);

			return calAts079;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts079;
		}
	}

	public AtsListDto calculateATS080(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts080 = new AtsListDto();
		AtsGenParamDto calAtsGen080 = new AtsGenParamDto();

		long ats080_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats080ParamData = getGeneralConstructionATSParam(atsGeneralId, atsParamId);

			if (ats080ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats080ParamData.get("logic_description");

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
								System.out.println("ATS-080 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats080_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats080_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats080_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats080_points == max) {
				paramType = "positive";
			} else if (ats080_points == partial) {
				paramType = "partial";
			} else if (ats080_points == 0) {
				paramType = "negative";
			}

			calAtsGen080.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen080.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen080.setCategory(genATSParamData.getCategory());
			calAtsGen080.setDescription(genATSParamData.getDescription());
			calAtsGen080.setMax_points(genATSParamData.getMax_points());
			calAtsGen080.setParameter(genATSParamData.getParameter());
			calAtsGen080.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen080.setTotal_points(genATSParamData.getTotal_points());

			calAts080.setAtsGeneralId(atsGeneralId);
			calAts080.setAtsGeneralParamDto(calAtsGen080);
			calAts080.setAtsParamData(ats080ParamData);
			calAts080.setAtsParamId(atsParamId);
			calAts080.setAtsParamType(paramType);
			calAts080.setAtsScore(ats080_points);

			return calAts080;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts080;
		}
	}

}
