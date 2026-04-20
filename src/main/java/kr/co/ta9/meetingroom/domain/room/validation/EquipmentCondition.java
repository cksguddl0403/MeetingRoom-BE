package kr.co.ta9.meetingroom.domain.room.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = EquipmentConditionValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface EquipmentCondition {

    String message() default "비품 조건에 비품 ID가 없거나 최소 수량이 1 미만입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
