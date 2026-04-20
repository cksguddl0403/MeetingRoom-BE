package kr.co.ta9.meetingroom.global.security.jwt.filter;

import tools.jackson.core.type.TypeReference;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StreamUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JwtLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final static String DEFAULT_FILTER_PROCESSES_URL = "/api/auth/sign-in";

    private final ObjectMapper objectMapper;

    private final static String CONTENT_TYPE = "application/json";

    private final UserRepository userRepository;

    public JwtLoginAuthenticationFilter(ObjectMapper objectMapper, UserRepository userRepository) {
        super(DEFAULT_FILTER_PROCESSES_URL);
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if(request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)  ) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }

        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, new TypeReference<>() {
        });

        String loginId = usernamePasswordMap.get("loginId");
        String password = usernamePasswordMap.get("password");

        userRepository.findByLoginId(loginId).orElseThrow(() -> new AuthenticationServiceException("loginId does not exist: " + loginId));

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginId, password);

        return getAuthenticationManager().authenticate(authRequest);
    }
}