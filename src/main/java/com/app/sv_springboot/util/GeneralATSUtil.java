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
import com.app.sv_springboot.entities.ATS_General_Param_Entity;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Component
public class GeneralATSUtil {

	private static Logger logger = LoggerFactory.getLogger(GeneralATSUtil.class);

	@Autowired
	ATSGeneralParamRepo atsGeneralParamRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	FileConversionUtil fileConUtil;

	ExtraATSUtil extraATSUtil;

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
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
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
//		System.out.println("getAllGeneralATSParams() --> generalATSParameterList :: " + generalATSParameterList);
		return generalATSParameterList;
	}

	public Map<String, Object> getGeneralATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalATSParameterMap :: " + generalATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	public long calculateATSScore(String fileName, MultipartFile file) {

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

						ats_001_score = calculateATS001(atsParamId, generalATSId, fileName, file);

					}
					break;

				case "ATS-002":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_002_score = calculateATS002(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-003":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_003_score = calculateATS003(atsParamId, generalATSId, fileName, file);
					}

					break;

				case "ATS-004":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_004_score = calculateATS004(atsParamId, generalATSId, fileName, file);
					}
					break;

				case "ATS-005":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_005_score = calculateATS005(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-006":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_006_score = calculateATS006(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-007":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_007_score = calculateATS007(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-008":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_008_score = calculateATS008(atsParamId, generalATSId, fileName, file);

					}
					break;

				case "ATS-009":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_009_score = calculateATS009(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-010":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_010_score = calculateATS010(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-011":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_011_score = calculateATS011(atsParamId, generalATSId, fileName, file);

					}
					break;

				case "ATS-012":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_012_score = calculateATS012(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-013":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_013_score = calculateATS013(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-014":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_014_score = calculateATS014(atsParamId, generalATSId, fileName, file);

					}
					break;

				case "ATS-015":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_015_score = calculateATS015(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-016":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_016_score = calculateATS016(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-017":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_017_score = calculateATS017(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-018":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_018_score = calculateATS018(atsParamId, generalATSId, fileName, file);

					}
					break;

				case "ATS-019":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_019_score = calculateATS019(atsParamId, generalATSId, fileName, file);

					}

					break;

				case "ATS-020":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_020_score = calculateATS020(atsParamId, generalATSId, fileName, file);

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

		return ats_Combined_Score;
	}

	public long calculateATS001(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats001_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats001ParamData = getGeneralATSParam(atsGeneralId, atsParamId);
			if (ats001ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return 0;
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

			return ats001_points;

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public long calculateATS002(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats002_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats002ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats002ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats002_points;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public long calculateATS003(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats003_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats003ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats003ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats003_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS004(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats004_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats004ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats004ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats004ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return 0;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("Extension :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats004_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats004_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats004_points = 0;
			}

			return ats004_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS005(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats005_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats005ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats005ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats005_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS006(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats006_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats006ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats006ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			// 6 Fonts cannot be detected
			if (detectedFonts == null || detectedFonts.isEmpty()) {
				return zeroScore;
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

			return ats006_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS007(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats007_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats007ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats007ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
				return 0;
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

			return ats007_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS008(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats008_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats008ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats008ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
				return 0;
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
				return 0;
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

			return ats008_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS009(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats009_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats009ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats009ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats009_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS010(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats010_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats010ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats010ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats010_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS011(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats011_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats011ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats011ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats011_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats011_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			return ats011_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS012(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats012_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats012ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats012ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
				return 0;
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
				ats012_points = genATSParamData.getAtsGeneralId();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats012_points = genATSParamData.getAtsGeneralId() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats012_points = 0;
			}

			return ats012_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS013(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats013_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats013ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats013ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
				return 0;
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

			return ats013_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS014(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats014_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats014ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats014ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
				return 0;
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

			return ats014_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS015(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats015_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats015ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats015ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats015_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS016(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats016_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats016ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats016ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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

			return ats016_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS017(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats017_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats017ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats017ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
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
				return 0;
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

			return ats017_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS018(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats018_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats018ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats018ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats018ParamData.get("logic_description");
			Map<String, Object> errorRules = (Map<String, Object>) logicDesc.get("error_detection_rules");
			Map<String, Object> scoringRules = (Map<String, Object>) logicDesc.get("scoring_rules");

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (resumeText == null || resumeText.isEmpty()) {
				return 0;
			}

			resumeText = resumeText.toLowerCase();

			// 5 Count all error patterns
			long totalErrors = 0;

			for (Object ruleListObj : errorRules.values()) {
				List<String> ruleList = (List<String>) ruleListObj;
				for (String errorPattern : ruleList) {
					if (resumeText.contains(errorPattern.toLowerCase())) {
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

			return ats018_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS019(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats019_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats019ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats019ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
			}

			// 3 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return 0;
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

			return ats019_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS020(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats020_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats020ParamData = getGeneralATSParam(atsGeneralId, atsParamId);

			if (ats020ParamData == null) {
				return 0;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return 0;
			}

			// 3️ Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return 0;
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

			return ats020_points;
		} catch (Exception e) {
			return 0;
		}
	}

}
