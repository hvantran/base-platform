package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.exceptions.AppException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;

@Getter
public enum TemplateEngineEnum implements GenericTemplateEngine {

    FREEMARKER_ENGINE("freemarker") {

        private static final FreemarkerConfigurationSingleton CONFIGURATION = new FreemarkerConfigurationSingleton();

        @Override
        public String process(String templateName, String templateString, Map<String, Object> objectData) {
            return this.process(templateName, templateString, objectData, CONFIGURATION.configuration);
        }

        @Override
        public String process(String templateName, String templateString, Map<String, Object> objectData,
                              Configuration configuration) {
            CheckedSupplier<String> processSupplier = () -> {
                super.process(templateName, templateString, objectData);
                Template template = new Template(templateName, templateString, CONFIGURATION.configuration);
                Writer out = new StringWriter();
                template.process(objectData, out);
                return out.toString();
            };
            return processSupplier.get();
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateEngineEnum.class);
    private final String templateEngineName;
    TemplateEngineEnum(String templateEngineName) {
        this.templateEngineName = templateEngineName;
    }

    @Override
    public String process(String templateName, String template, Map<String, Object> data) {
        LOGGER.info("Processing template {} by engine {}", templateName, templateEngineName);
        LOGGER.debug("Template name: {}, data: {}", templateName, data);
        return null;
    }

    public static TemplateEngineEnum getTemplateEngineFromName(String templateEngineName) {
        return Arrays.stream(TemplateEngineEnum.values())
                .filter(templateEngineEnum -> templateEngineName.equals(templateEngineEnum.templateEngineName))
                .findFirst().orElseThrow(() -> new AppException("Cannot find template engine: " + templateEngineName));
    }

    private static final class FreemarkerConfigurationSingleton {

        private final Configuration configuration;

        FreemarkerConfigurationSingleton() {
            Configuration defaultConfiguration = new Configuration(Configuration.VERSION_2_3_30);
            defaultConfiguration.setDefaultEncoding("UTF-8");
            defaultConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            defaultConfiguration.setLogTemplateExceptions(false);
            defaultConfiguration.setWrapUncheckedExceptions(true);
            defaultConfiguration.setFallbackOnNullLoopVariable(false);
            defaultConfiguration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_30).build());

            this.configuration = defaultConfiguration;
        }
    }
}
