package ru.yandex.practicum.filmorate.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateOnlyAfterValidator.class)
public @interface DateOnlyAfter {
    String value();

    String message() default "Date should be greater than {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
