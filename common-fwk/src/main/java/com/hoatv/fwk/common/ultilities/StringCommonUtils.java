package com.hoatv.fwk.common.ultilities;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StringCommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringCommonUtils.class);

    public static final String END_BRACKETS = "}";

    private StringCommonUtils() {
    }


    /**
     * Parse string input into Map<String, String> object with provided properties template
     * Example:
     * Input string: Student(name=Nick, age=6, className=1/10, nickname=tit)
     * Properties template: Set.of("{<(secondName)name><, >}", "{<age><, >}", "{<className><, >}", "{<nickname><, >}")
     * Output: {secondName=Nick, age=6, className=1/10, nickname=tit}
     *
     * @param propertyTemplates : The set of template properties need to collect
     * @param input: The input string
     * @return the properties as a Map
     */
    public static Map<String, String> getPropertiesFromTemplate (Set<String> propertyTemplates, String input) {
        return propertyTemplates.stream()
                .map(propertyTemplate -> getPropertiesFromTemplate(propertyTemplate, input))
                .flatMap(p -> p.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     *
     * Parse string input into Map<String, String> object with provided properties template
     * Example:
     * Input string: Student(name=Nick, age=6, className=1/10, nickname=tit)
     * Template string: "{<(secondName)name><, >} {<age><, >} {<className><, >} {<nickname><, >}"
     * Output: {secondName=Nick, age=6, className=1/10, nickname=tit}
     *
     * @param template: The string of template properties need to collect
     * @param input: The input string
     * @return the properties as a Map
     */
    public static Map<String, String> getPropertiesFromTemplate (String template, String input) {
        String[] placeHolders = StringUtils.substringsBetween(template, "{", "}");
        Map<String, String> properties = new HashMap<>();
        try {
            for (int index = 0; index < placeHolders.length; index++) {
                String propertyNamePlaceholder = placeHolders[index];
                String[] propertyFormulaArr = StringUtils.substringsBetween(propertyNamePlaceholder, "<", ">");
                String startPropertyNameTemplate = propertyFormulaArr[0];
                String endPropertyNameTemplate = propertyFormulaArr[1];

                int endDelta = getDelta(endPropertyNameTemplate);
                String endPropertyName = getPropertyName(endDelta, endPropertyNameTemplate);
                int startDelta = getDelta(startPropertyNameTemplate);
                String startPropertyName = getPropertyName(startDelta, startPropertyNameTemplate);

                String propertyNameRaw =  startPropertyNameTemplate
                        .replace(String.format("(%s)", startPropertyName), "");

                if (startDelta != 0 ) {
                    propertyNameRaw = propertyNameRaw
                            .replace(String.format(":%s:", startDelta), "");
                }

                int propertyStartIndex = input.indexOf(propertyNameRaw);
                if (propertyStartIndex == -1) {
                    LOGGER.debug("Property {} doesn't exist in input string", propertyNameRaw);
                    continue;
                }

                int currentIndex = propertyStartIndex + propertyNameRaw.length() + startDelta + 1;
                int nextIndex = input.indexOf(endPropertyName, currentIndex) + endDelta;

                if (nextIndex == -1) {
                    LOGGER.debug("Property {} doesn't exist in input string", propertyNameRaw);
                    continue;
                }

                String propertyValue = input.substring(currentIndex, nextIndex);
                propertyValue = propertyValue.endsWith(END_BRACKETS) ?
                        propertyValue.substring(0, propertyValue.length() - 1) : propertyValue;
                properties.put(startPropertyName, propertyValue);
            }
            return properties;
        } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {
            LOGGER.warn("Error occurred while processing template {}", template, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Rename property
     * Example <(secondName)name>. The result is secondName
     * Example <ad> . The result ad
     * @param propertyNameFormula
     * @return property name
     */
    private static String getPropertyName (String propertyNameFormula) {
        String propertyName = StringUtils.substringBetween(propertyNameFormula, "(", ")");
        if (StringUtils.isEmpty(propertyName)) {
            return propertyNameFormula;
        }
        return propertyName;
    }

    private static String getPropertyName (int endDelta, String endPropertyNameRaw) {
        String secondPropertyNameTemplate = getPropertyName(endPropertyNameRaw);
        if ( endDelta != 0) {
            String deltaString = String.format(":%s:", endDelta);
            String secondPropertyName = secondPropertyNameTemplate.replace(deltaString, "");
            if (StringUtils.isEmpty(secondPropertyName)) {
                String propertyPlaceHolder = String.format("(:%s:)", endDelta);
                return endPropertyNameRaw.replace(propertyPlaceHolder, "");
            }
        }
        return secondPropertyNameTemplate;
    }

    /**
     * Example abc:1: => delta = 1
     *         abc:-1: => delta = -1
     *         abc => delta = 0
     * @param propertyNameRaw
     * @return
     */
    private static int getDelta(String propertyNameRaw) {
        String deltaString = StringUtils.substringBetween(propertyNameRaw, ":", ":");

        if (StringUtils.isNotEmpty(deltaString)) {
            return Integer.parseInt(deltaString);
        }
        return 0;
    }
}
