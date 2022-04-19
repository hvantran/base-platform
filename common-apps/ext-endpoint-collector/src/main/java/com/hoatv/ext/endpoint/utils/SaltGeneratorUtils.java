package com.hoatv.ext.endpoint.utils;

import com.hoatv.ext.endpoint.dtos.DataGeneratorVO;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

public class SaltGeneratorUtils {
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static final String SALT_NUMS = "1234567890";
    private static final Random rnd = new Random();

    private SaltGeneratorUtils() {
    }

    public enum GeneratorType {
        RANDOM,
        SEQUENCE,
        RANDOM_WITH_CONDITION,
        NONE
    }

    public static String getSaltString(Integer length) {
        StringBuilder salt = new StringBuilder();
        return getSalt(length, salt, SALT_CHARS);
    }

    public static String getSaltString(Integer length, String startWith) {
        StringBuilder salt = new StringBuilder(startWith);
        return getSalt(length, salt, SALT_CHARS);
    }

    public static String getSaltNums(Integer length) {
        StringBuilder salt = new StringBuilder();
        return getSalt(length, salt, SALT_NUMS);
    }

    public static String getSaltNums(Integer length, String startWith) {
        StringBuilder salt = new StringBuilder(startWith);
        return getSalt(length, salt, SALT_NUMS);
    }

    public static String getSequenceNums(int length, int startWith, int increases) {
        return StringUtils.leftPad(String.valueOf(startWith+increases), length, "0");
    }

    private static String getSalt(int length, StringBuilder salt, String saltNums) {
        while (salt.length() < length) {
            int index = rnd.nextInt(saltNums.length());
            salt.append(saltNums.charAt(index));
        }
        return salt.toString();
    }

    public static String generateSaltValue(DataGeneratorVO dataGeneratorVO, int increases) throws
            IllegalAccessException, InvocationTargetException {

        GeneratorType generatorType = dataGeneratorVO.getGeneratorType();
        CheckedFunction<String, Method> generatorMethodFunc = dataGeneratorVO.getGeneratorMethodFunc();
        String generatorMethodName = dataGeneratorVO.getGeneratorMethodName();
        Integer generatorSaltLength = dataGeneratorVO.getGeneratorSaltLength();
        String generatorSaltStartWith = dataGeneratorVO.getGeneratorSaltStartWith();

        switch (generatorType) {
            case SEQUENCE:
                return getSequenceNums(generatorSaltLength, Integer.parseInt(generatorSaltStartWith), increases);
            case RANDOM:
                Method generatorMethod = generatorMethodFunc.apply(generatorMethodName);
                return (String) generatorMethod.invoke(SaltGeneratorUtils.class, generatorSaltLength, generatorSaltStartWith);
            case RANDOM_WITH_CONDITION:
                generatorMethod = generatorMethodFunc.apply(generatorMethodName);
                String random = (String) generatorMethod.invoke(SaltGeneratorUtils.class, generatorSaltLength, generatorSaltStartWith);
                Predicate<String> checkExistingFunc = dataGeneratorVO.getCheckExistingFunc();
                ObjectUtils.checkThenThrow(Objects::isNull, checkExistingFunc, "Random with pre check required checking function");
                while (checkExistingFunc.test(random)) {
                    random = (String) generatorMethod.invoke(SaltGeneratorUtils.class, generatorSaltLength, generatorSaltStartWith);
                }
                return random;
            case NONE:
            default:
                return null;
        }
    }
    public static CheckedFunction<String, Method> getGeneratorMethodFunc(String generatorSaltStartWith) {
        return methodName -> {
            if (StringUtils.isNotEmpty(generatorSaltStartWith)) {
                return SaltGeneratorUtils.class.getMethod(methodName, Integer.class, String.class);
            }
            return SaltGeneratorUtils.class.getMethod(methodName, Integer.class);
        };
    }
}
