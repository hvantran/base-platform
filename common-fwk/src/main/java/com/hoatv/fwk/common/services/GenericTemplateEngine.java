package com.hoatv.fwk.common.services;

import freemarker.template.Configuration;

import java.util.Map;

public interface GenericTemplateEngine {

    String process(String templateName, String templateString, Map<String, Object> objectData);
    String process(String templateName, String templateString, Map<String, Object> objectData, Configuration configuration);
}
