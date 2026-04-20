package kr.co.ta9.meetingroom.domain.inquiry.repository;

import kr.co.ta9.meetingroom.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long>, InquiryRepositoryCustom {
}

