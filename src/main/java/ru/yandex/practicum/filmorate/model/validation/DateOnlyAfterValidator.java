package ru.yandex.practicum.filmorate.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateOnlyAfterValidator implements ConstraintValidator<DateOnlyAfter, Date> {
    private Date startDate;

    @Override
    public void initialize(DateOnlyAfter constraintAnnotation) {
        try {
            this.startDate = new SimpleDateFormat("yyyy-MM-dd").parse(constraintAnnotation.value());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Incorrect date format in " + DateOnlyAfter.class.getSimpleName() + " annotation");
        }

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(@NonNull Date date, ConstraintValidatorContext constraintValidatorContext) {
        return date.after(startDate);
    }
}
