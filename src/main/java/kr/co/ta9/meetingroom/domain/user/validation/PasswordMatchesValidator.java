package kr.co.ta9.meetingroom.domain.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kr.co.ta9.meetingroom.domain.auth.dto.FindPasswordResetRequestDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserCreateRequestDto;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String password;
        String passwordConfirm;
        String confirmProperty;

        if (value instanceof UserCreateRequestDto userCreateRequestDto) {
            password = userCreateRequestDto.getPassword();
            passwordConfirm = userCreateRequestDto.getPasswordConfirm();
            confirmProperty = "passwordConfirm";
        } else if (value instanceof FindPasswordResetRequestDto findPasswordResetRequestDto) {
            password = findPasswordResetRequestDto.getNewPassword();
            passwordConfirm = findPasswordResetRequestDto.getNewPasswordConfirm();
            confirmProperty = "newPasswordConfirm";
        } else {
            return true;
        }

        if (password == null || passwordConfirm == null) {
            return true;
        }

        boolean matched = password.equals(passwordConfirm);
        if (!matched) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                    .addPropertyNode(confirmProperty)
                    .addConstraintViolation();
        }
        return matched;
    }
}
