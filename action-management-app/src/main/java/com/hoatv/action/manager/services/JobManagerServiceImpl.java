package com.hoatv.action.manager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.action.manager.dtos.JobCategory;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.repositories.JobDocumentRepository;
import com.hoatv.action.manager.repositories.JobExecutionResultDocumentRepository;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.TemplateEngineEnum;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import com.hoatv.monitor.mgmt.TimingMetricMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobManagerServiceImpl implements JobManagerService {

    private ScriptEngineService scriptEngineService;

    private ObjectMapper objectMapper;

    private JobDocumentRepository jobDocumentRepository;

    private JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository;

    private static Map<String, String> DEFAULT_CONFIGURATIONS = new HashMap<>();

    static {
        DEFAULT_CONFIGURATIONS.put("templateEngineName", "freemarker");
    }

    @Autowired
    public JobManagerServiceImpl(ScriptEngineService scriptEngineService, JobDocumentRepository jobDocumentRepository,
                                 JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository) {
        this.scriptEngineService = scriptEngineService;
        this.jobDocumentRepository = jobDocumentRepository;
        this.jobExecutionResultDocumentRepository = jobExecutionResultDocumentRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @LoggingMonitor
    public JobResult executeJob(JobDefinitionDTO jobDefinitionDTO) {

        String jobName = jobDefinitionDTO.getJobName();
        JobCategory jobCategory = jobDefinitionDTO.getJobCategory();
        String templateName = String.format("%s-%s", jobName, jobCategory);

        String configurations = jobDefinitionDTO.getConfigurations();
        CheckedSupplier<Map> configurationToMapSupplier = () -> objectMapper.readValue(configurations, Map.class);
        Map<String, Object> configurationMap = configurationToMapSupplier.get();

        String defaultTemplateEngine = DEFAULT_CONFIGURATIONS.get("templateEngineName");
        String templateEngineName = (String) configurationMap.getOrDefault("templateEngineName",
                defaultTemplateEngine);

        TemplateEngineEnum templateEngine = TemplateEngineEnum.getTemplateEngineFromName(templateEngineName);
        String jobContent = templateEngine.process(templateName, jobDefinitionDTO.getJobContent(), configurationMap);
        return scriptEngineService.executeJobLauncher(jobContent);
    }
}
