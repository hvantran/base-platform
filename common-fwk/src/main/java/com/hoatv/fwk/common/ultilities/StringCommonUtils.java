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

    public static final String END_BRACES = "}$";

    public static final String OPEN_BRACES = "^{";

    public static final String OPEN_ANGLE_BRACKETS = "<";

    public static final String CLOSE_ANGLE_BRACKETS = ">";

    public static final String OPEN_PARENTHESIS = "(";

    public static final String CLOSE_PARENTHESIS = ")";

    public static final String EMPTY_STRING = "";

    public static final String ADDITIONAL_NUMBER_OF_CHARACTER_FORMULA_FORMAT = ":%s:";

    private StringCommonUtils() {
    }


    /**
     * Parse string input into Map<String, String> object with provided properties template
     * Example:
     * Input string: Student(name=Nick, age=6, className=1/10, nickname=tit)
     * Properties template: Set.of("^{<(:1:secondName)name><, >}$", "{(:1:)<age><, >}$", "{(:1:)<className><, >}$", "{(:1:)<nickname><, >}$")
     * Output: {secondName=Nick, age=6, className=1/10, nickname=tit}
     * Notes: :1: above is number of characters will take after the property, like above example it will skip = characters
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
     * Template string: "^{<(secondName)name><, >}$ ^{<age><, >}$ ^{<className><, >}$ ^{<nickname><, >}$"
     * Output: {secondName=Nick, age=6, className=1/10, nickname=tit}
     *
     * @param template: The string of template properties need to collect
     * @param input: The input string
     * @return the properties as a Map
     */

    private static Map<String, String> getPropertiesFromTemplate (String template, String input) {
        String[] placeHolders = StringUtils.substringsBetween(template, OPEN_BRACES, END_BRACES);
        Map<String, String> properties = new HashMap<>();
        try {
            for (int index = 0; index < placeHolders.length; index++) {
                String propertyNamePlaceholder = placeHolders[index];
                String[] propertyFormulaArr = StringUtils.substringsBetween(propertyNamePlaceholder, OPEN_ANGLE_BRACKETS, CLOSE_ANGLE_BRACKETS);
                String startPropertyNameTemplate = propertyFormulaArr[0];
                String endPropertyNameTemplate = propertyFormulaArr[1];

                int endDelta = getDelta(endPropertyNameTemplate);
                String endPropertyName = getPropertyName(endDelta, endPropertyNameTemplate);
                int startDelta = getDelta(startPropertyNameTemplate);
                String startPropertyName = getPropertyName(startDelta, startPropertyNameTemplate);
                String formatPropertyNameFormula = StringUtils.substringBetween(startPropertyNameTemplate, OPEN_PARENTHESIS, CLOSE_PARENTHESIS);

                String propertyNameRaw = startPropertyNameTemplate
                        .replace(String.format("(%s)", formatPropertyNameFormula), EMPTY_STRING);

                if (startDelta != 0) {
                    String deltaFormat = String.format(ADDITIONAL_NUMBER_OF_CHARACTER_FORMULA_FORMAT, startDelta);
                    propertyNameRaw = propertyNameRaw.replace(deltaFormat, EMPTY_STRING);
                }

                int propertyStartIndex = input.indexOf(propertyNameRaw);
                if (propertyStartIndex == -1) {
                    LOGGER.debug("Property {} doesn't exist in input string", propertyNameRaw);
                    continue;
                }

                int currentIndex = propertyStartIndex + propertyNameRaw.length() + startDelta;
                int nextIndex = input.indexOf(endPropertyName, currentIndex) + endDelta;

                if (nextIndex == -1) {
                    LOGGER.debug("Property {} doesn't exist in input string", propertyNameRaw);
                    continue;
                }

                String propertyValue = input.substring(currentIndex, nextIndex);
                propertyValue = propertyValue.endsWith("}") ?
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


    private static String getPropertyName (int delta, String endPropertyNameRaw) {
        String secondPropertyNameTemplate = getPropertyName(endPropertyNameRaw);
        if ( delta != 0) {
            String deltaString = String.format(ADDITIONAL_NUMBER_OF_CHARACTER_FORMULA_FORMAT, delta);
            String secondPropertyName = secondPropertyNameTemplate.replace(deltaString, EMPTY_STRING);
            if (StringUtils.isEmpty(secondPropertyName)) {
                String propertyPlaceHolder = String.format("(:%s:)", delta);
                return endPropertyNameRaw.replace(propertyPlaceHolder, EMPTY_STRING);
            }
            return secondPropertyName;
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
