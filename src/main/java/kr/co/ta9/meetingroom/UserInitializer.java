package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        User user1 = User.createUser(
                "testuser01",
                passwordEncoder.encode("Test#pwd01"),
                "하린",
                "testnick01",
                "test.user01@example.org"
        );

        User user2 = User.createUser(
                "testuser02",
                passwordEncoder.encode("Test#pwd02"),
                "지우",
                "testnick02",
                "test.user02@example.org"
        );
        userRepository.saveAll(List.of(user1, user2));
    }
}
