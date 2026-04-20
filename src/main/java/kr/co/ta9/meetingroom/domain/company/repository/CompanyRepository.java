package kr.co.ta9.meetingroom.domain.company.repository;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
