package com.hoatv.fwk.common.services;

import java.util.Map;

public interface GenericTemplateEngine {

    String process(String templateName, String template, Map<String, Object> data);
}
