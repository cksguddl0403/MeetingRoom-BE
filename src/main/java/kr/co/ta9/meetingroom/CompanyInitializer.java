package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Order(1)
@RequiredArgsConstructor
public class CompanyInitializer implements ApplicationRunner {
    private final CompanyRepository companyRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        companyRepository.save(Company.createCompany(
                "티에이나인",
                "IT",
                LocalDate.of(2020, 1, 1),
                "",
                100,
                "123-45-67890"
        ));
    }
}
