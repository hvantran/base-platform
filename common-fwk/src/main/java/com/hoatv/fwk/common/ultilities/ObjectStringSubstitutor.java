package com.hoatv.fwk.common.ultilities;

import com.hoatv.fwk.common.services.CheckedFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectStringSubstitutor<V> extends StringSubstitutor {

    private Map<String, V> valueMap;
    public ObjectStringSubstitutor(Map<String, V> valueMap) {
        super(valueMap);
        this.valueMap = valueMap;
    }

    public  ObjectStringSubstitutor(Map<String, V> valueMap, String prefix, String suffix) {
        super(valueMap, prefix, suffix);
        this.valueMap = valueMap;
    }

    public ObjectStringSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape) {
        super(valueMap, prefix, suffix, escape);
        this.valueMap = valueMap;
    }

    public ObjectStringSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape, String valueDelimiter) {
        super(valueMap, prefix, suffix, escape, valueDelimiter);
        this.valueMap = valueMap;
    }

    @Override
    protected String resolveVariable(String variableName, TextStringBuilder buf, int startPos, int endPos) {
        CheckedFunction<String, String> stringSubstitutorFromObject = input -> {
            Pattern pattern = Pattern.compile("(\\w+)(\\.)(\\w+)(\\(\\))");
            Matcher matcher = pattern.matcher(variableName);
            if (matcher.matches()) {
                String argumentName = matcher.group(1);
                String methodName = matcher.group(3);
                Object argument = valueMap.get(argumentName);
                Method actualMethod = argument.getClass().getMethod(methodName);
                Object getterMethodValue = actualMethod.invoke(argument);
                return String.valueOf(getterMethodValue);
            }
            return null;
        };
        String propertyValue = stringSubstitutorFromObject.apply(variableName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            return propertyValue;
        }
        return super.resolveVariable(variableName, buf, startPos, endPos);
    }
}
