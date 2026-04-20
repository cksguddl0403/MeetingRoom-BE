package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
@RequiredArgsConstructor
public class CompanyMemberInitializer implements ApplicationRunner {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        User user1 = userRepository.findByLoginId("testuser01")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        User user2 = userRepository.findByLoginId("testuser02")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        companyMemberRepository.save(CompanyMember.createCompanyMember(Role.ADMIN, user1, company));
        companyMemberRepository.save(CompanyMember.createCompanyMember(Role.EMPLOYEE, user2, company));
    }
}
