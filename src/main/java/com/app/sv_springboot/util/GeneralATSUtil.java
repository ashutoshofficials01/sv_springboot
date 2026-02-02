package com.app.sv_springboot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

						logger.info("ATS-001 = = = >> " + ats_001_score);

					}
					break;

				case "ATS-002":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_002_score = calculateATS002(atsParamId, generalATSId, fileName, file);

						logger.info("ATS-002 = = = >> " + ats_002_score);

					}

					break;

				case "ATS-003":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_003_score = calculateATS003(atsParamId, generalATSId, fileName, file);

						logger.info("ATS-003 = = = >> " + ats_003_score);
					}

					break;

				case "ATS-004":
					if (atsParamId.equalsIgnoreCase(genATSParamId)) {

						long generalATSId = agp.getAtsGeneralId();

						ats_004_score = calculateATS004(atsParamId, generalATSId, fileName, file);

						logger.info("ATS-004 = = = >> " + ats_004_score);
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

			System.out.println("resumeText :: " + resumeText);

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
			System.out.println("ats004_points :: " + ats004_points);

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
			return ats006_points;
		} catch (Exception e) {
			return 0;
		}
	}

	public long calculateATS007(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats007_points = 0;
		try {
			return ats007_points;
		} catch (Exception e) {
			return ats007_points;
		}
	}

	public long calculateATS008(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats008_points = 0;
		try {
			return ats008_points;
		} catch (Exception e) {
			return ats008_points;
		}
	}

	public long calculateATS009(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats009_points = 0;
		try {
			return ats009_points;
		} catch (Exception e) {
			return ats009_points;
		}
	}

	public long calculateATS010(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats010_points = 0;
		try {
			return ats010_points;
		} catch (Exception e) {
			return ats010_points;
		}
	}

	public long calculateATS011(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats011_points = 0;
		try {
			return ats011_points;
		} catch (Exception e) {
			return ats011_points;
		}
	}

	public long calculateATS012(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats012_points = 0;
		try {
			return ats012_points;
		} catch (Exception e) {
			return ats012_points;
		}
	}

	public long calculateATS013(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats013_points = 0;
		try {
			return ats013_points;
		} catch (Exception e) {
			return ats013_points;
		}
	}

	public long calculateATS014(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats014_points = 0;
		try {
			return ats014_points;
		} catch (Exception e) {
			return ats014_points;
		}
	}

	public long calculateATS015(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats015_points = 0;
		try {
			return ats015_points;
		} catch (Exception e) {
			return ats015_points;
		}
	}

	public long calculateATS016(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats016_points = 0;
		try {
			return ats016_points;
		} catch (Exception e) {
			return ats016_points;
		}
	}

	public long calculateATS017(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats017_points = 0;
		try {
			return ats017_points;
		} catch (Exception e) {
			return ats017_points;
		}
	}

	public long calculateATS018(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats018_points = 0;
		try {
			return ats018_points;
		} catch (Exception e) {
			return ats018_points;
		}
	}

	public long calculateATS019(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats019_points = 0;
		try {
			return ats019_points;
		} catch (Exception e) {
			return ats019_points;
		}
	}

	public long calculateATS020(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		long ats020_points = 0;
		try {
			return ats020_points;
		} catch (Exception e) {
			return ats020_points;
		}
	}

}
