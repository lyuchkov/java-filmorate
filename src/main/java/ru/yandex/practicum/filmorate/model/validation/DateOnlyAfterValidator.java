package ru.yandex.practicum.filmorate.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DateOnlyAfterValidator implements ConstraintValidator<DateOnlyAfter, LocalDate> {
    private LocalDate startDate;

    @Override
    public void initialize(DateOnlyAfter constraintAnnotation) {
        this.startDate = LocalDate.parse(constraintAnnotation.value());

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(@NonNull LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date.isAfter(startDate);
    }
}
