package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.domain.room.repository.RoomRepository;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(3)
@RequiredArgsConstructor
public class RoomInitializer implements ApplicationRunner {
    private final CompanyRepository companyRepository;
    private final RoomRepository roomRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        Room room1 = Room.createRoom("A 회의실", 6, company);
        Room room2 = Room.createRoom("B 회의실", 10, company);
        Room room3 = Room.createRoom("C 회의실", 20, company);

        roomRepository.saveAll(List.of(room1, room2, room3));
    }
}
