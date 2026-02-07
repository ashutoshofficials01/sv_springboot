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
	private final BusinessAtsUtil businessAtsUtil;
	private final ConstructionAtsUtil constructionAtsUtil;
	private final DesignAtsUtil designAtsUtil;
	private final EducationAtsUtil educationAtsUtil;
	private final FinanceAtsUtil financeAtsUtil;
	private final GovernmentAtsUtil governmentAtsUtil;
	private final HealthcareAtsUtil healthcareAtsUtil;
	private final LegalAtsUtil legalAtsUtil;
	private final TourismAtsUtil tourismAtsUtil;

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

	GeneralATSUtil(AtsGenParamDto atsGenParamDto, ITAtsUtil iTAtsUtil, BusinessAtsUtil businessAtsUtil,
			ConstructionAtsUtil constructionAtsUtil, DesignAtsUtil designAtsUtil, EducationAtsUtil educationAtsUtil,
			FinanceAtsUtil financeAtsUtil, GovernmentAtsUtil governmentAtsUtil, HealthcareAtsUtil healthcareAtsUtil,
			LegalAtsUtil legalAtsUtil, TourismAtsUtil tourismAtsUtil) {
		this.atsGenParamDto = atsGenParamDto;
		this.iTAtsUtil = iTAtsUtil;
		this.businessAtsUtil = businessAtsUtil;
		this.constructionAtsUtil = constructionAtsUtil;
		this.designAtsUtil = designAtsUtil;
		this.educationAtsUtil = educationAtsUtil;
		this.financeAtsUtil = financeAtsUtil;
		this.governmentAtsUtil = governmentAtsUtil;
		this.healthcareAtsUtil = healthcareAtsUtil;
		this.legalAtsUtil = legalAtsUtil;
		this.tourismAtsUtil = tourismAtsUtil;
	}

	/* IT Resume => => */

	private List<Map<String, Object>> generalITATSParameterList;

	private Map<String, Map<String, Object>> generalITATSParameterMap;

	@PostConstruct
	public void loadITATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:IT_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalITATSParameterList = (List<Map<String, Object>>) jsonData.get("IT_ATSParameter");

			// Create fast-lookup map
			generalITATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalITATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalITATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalITATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load IT_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
		return generalITATSParameterList;
	}

	public Map<String, Object> getGeneralATSParam(long atsGeneralId, String atsParamId) {
		return generalITATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= IT Resume */

	/* Construction Resume => => */

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
			throw new RuntimeException("Failed to load Construction_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllConstructionGeneralATSParams() {
		return generalConstructionATSParameterList;
	}

	public Map<String, Object> getGeneralConstructionATSParam(long atsGeneralId, String atsParamId) {
		return generalConstructionATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Construction Resume */

	/* Business Resume => => */

	private List<Map<String, Object>> generalBusinessATSParameterList;

	private Map<String, Map<String, Object>> generalBusinessATSParameterMap;

	@PostConstruct
	public void loadBusinessATSParameters() {
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

	public List<Map<String, Object>> getAllBusinessGeneralATSParams() {
		return generalBusinessATSParameterList;
	}

	public Map<String, Object> getGeneralBusinessATSParam(long atsGeneralId, String atsParamId) {
		return generalBusinessATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Business Resume */
	
	/* Design Resume => => */

	private List<Map<String, Object>> generalDesignATSParameterList;

	private Map<String, Map<String, Object>> generalDesignATSParameterMap;

	@PostConstruct
	public void loadDesignATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Design_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalDesignATSParameterList = (List<Map<String, Object>>) jsonData.get("Design_ATSParameter");

			// Create fast-lookup map
			generalDesignATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalDesignATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalDesignATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalDesignATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Design_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllDesignGeneralATSParams() {
		return generalDesignATSParameterList;
	}

	public Map<String, Object> getGeneralDesignATSParam(long atsGeneralId, String atsParamId) {
		return generalDesignATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Design Resume */
	
	/* Education Resume => => */

	private List<Map<String, Object>> generalEducationATSParameterList;

	private Map<String, Map<String, Object>> generalEducationATSParameterMap;

	@PostConstruct
	public void loadEducationATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Education_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalEducationATSParameterList = (List<Map<String, Object>>) jsonData.get("Education_ATSParameter");

			// Create fast-lookup map
			generalEducationATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalEducationATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalEducationATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalEducationATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Education_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllEducationGeneralATSParams() {
		return generalEducationATSParameterList;
	}

	public Map<String, Object> getGeneralEducationATSParam(long atsGeneralId, String atsParamId) {
		return generalEducationATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Education Resume */
	
	/* Finance Resume => => */

	private List<Map<String, Object>> generalFinanceATSParameterList;

	private Map<String, Map<String, Object>> generalFinanceATSParameterMap;

	@PostConstruct
	public void loadFinanceATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Finance_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalFinanceATSParameterList = (List<Map<String, Object>>) jsonData.get("Finance_ATSParameter");

			// Create fast-lookup map
			generalFinanceATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalFinanceATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalFinanceATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalFinanceATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Finance_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllFinanceGeneralATSParams() {
		return generalFinanceATSParameterList;
	}

	public Map<String, Object> getGeneralFinanceATSParam(long atsGeneralId, String atsParamId) {
		return generalFinanceATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Finance Resume */
	
	/* Government Resume => => */

	private List<Map<String, Object>> generalGovernmentATSParameterList;

	private Map<String, Map<String, Object>> generalGovernmentATSParameterMap;

	@PostConstruct
	public void loadGovernmentATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Government_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalGovernmentATSParameterList = (List<Map<String, Object>>) jsonData.get("Government_ATSParameter");

			// Create fast-lookup map
			generalGovernmentATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalGovernmentATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalGovernmentATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalGovernmentATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Government_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGovernmentGeneralATSParams() {
		return generalGovernmentATSParameterList;
	}

	public Map<String, Object> getGeneralGovernmentATSParam(long atsGeneralId, String atsParamId) {
		return generalGovernmentATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Government Resume */
	
	/* Healthcare Resume => => */

	private List<Map<String, Object>> generalHealthcareATSParameterList;

	private Map<String, Map<String, Object>> generalHealthcareATSParameterMap;

	@PostConstruct
	public void loadHealthcareATSParameters() {
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

	public List<Map<String, Object>> getAllHealthcareGeneralATSParams() {
		return generalHealthcareATSParameterList;
	}

	public Map<String, Object> getGeneralHealthcareATSParam(long atsGeneralId, String atsParamId) {
		return generalHealthcareATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Healthcare Resume */
	
	/* Legal Resume => => */

	private List<Map<String, Object>> generalLegalATSParameterList;

	private Map<String, Map<String, Object>> generalLegalATSParameterMap;

	@PostConstruct
	public void loadLegalATSParameters() {
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

	public List<Map<String, Object>> getAllLegalGeneralATSParams() {
		return generalLegalATSParameterList;
	}

	public Map<String, Object> getGeneralLegalATSParam(long atsGeneralId, String atsParamId) {
		return generalLegalATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Legal Resume */
	
	/* Tourism Resume => => */

	private List<Map<String, Object>> generalTourismATSParameterList;

	private Map<String, Map<String, Object>> generalTourismATSParameterMap;

	@PostConstruct
	public void loadTourismATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Tourism_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalTourismATSParameterList = (List<Map<String, Object>>) jsonData.get("Tourism_ATSParameter");

			// Create fast-lookup map
			generalTourismATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalTourismATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalTourismATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalTourismATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Tourism_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllTourismGeneralATSParams() {
		return generalTourismATSParameterList;
	}

	public Map<String, Object> getGeneralTourismATSParam(long atsGeneralId, String atsParamId) {
		return generalTourismATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Tourism Resume */

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

	public List<AtsListDto> calculateConstruction_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateEducation_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateFinance_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateBusiness_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateTourism_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateHealthcare_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateLegal_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateGovernment_ATSScore(String fileName, MultipartFile file) {

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

	public List<AtsListDto> calculateDesign_ATSScore(String fileName, MultipartFile file) {

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
