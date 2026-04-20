package kr.co.ta9.meetingroom;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(9)
@RequiredArgsConstructor
public class NoticeInitializer implements ApplicationRunner {

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
    }
}
