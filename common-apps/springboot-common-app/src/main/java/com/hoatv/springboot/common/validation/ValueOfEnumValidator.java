package com.hoatv.springboot.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private List<String> acceptedValues;
    private String message;

    @Override
    public void initialize(ValueOfEnum annotation) {
        message = annotation.message();
        acceptedValues = Stream.of(annotation.value().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (StringUtils.isEmpty(message)) {
            context.disableDefaultConstraintViolation();
            String messageTemplate = "Must be any of values " + acceptedValues;
            context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
        }
        return acceptedValues.contains(value.toString());
    }
}
