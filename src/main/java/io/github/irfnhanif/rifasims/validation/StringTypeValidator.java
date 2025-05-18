package io.github.irfnhanif.rifasims.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StringTypeValidator implements ConstraintValidator<MustBeString, String> {
    @Override
    public void initialize(MustBeString constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return ThreadLocalTypeInfo.isString(value);
    }
}
