package io.github.irfnhanif.rifasims.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StringTypeValidator.class)
public @interface MustBeString {
    String message() default "Field must be a string";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
