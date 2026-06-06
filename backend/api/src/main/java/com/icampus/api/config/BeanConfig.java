package com.icampus.api.config;

import com.icampus.app.qa.QnaService;
import com.icampus.app.qa.support.QuestionSegmenter;
import com.icampus.app.qa.support.QuestionValidator;
import com.icampus.app.service.AdminService;
import com.icampus.app.service.AuthService;
import com.icampus.app.service.ContributionService;
import com.icampus.domain.repository.AnswerFeedbackRepository;
import com.icampus.domain.repository.ContributionRepository;
import com.icampus.domain.repository.KnowledgeBaseRepository;
import com.icampus.domain.repository.QuestionLogRepository;
import com.icampus.domain.repository.UserRepository;
import com.icampus.domain.spi.LlmClient;
import com.icampus.domain.spi.TokenProvider;
import com.icampus.infra.llm.MockLlmClient;
import com.icampus.api.security.JwtAuthenticationFilter;
import com.icampus.infra.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    // ========== JWT ==========

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${jwt.secret:icampus-jwt-secret-key-2024-min-length-32}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        return new JwtTokenProvider(secret, expirationMs);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public CurrentUserIdArgumentResolver currentUserIdArgumentResolver(TokenProvider tokenProvider) {
        return new CurrentUserIdArgumentResolver(tokenProvider);
    }

    // ========== LLM Client ==========

    @Bean
    public LlmClient llmClient() {
        return new MockLlmClient();
    }

    // ========== Services ==========

    @Bean
    public QnaService qnaService(KnowledgeBaseRepository knowledgeBaseRepository,
                                 QuestionLogRepository questionLogRepository,
                                 AnswerFeedbackRepository answerFeedbackRepository,
                                 LlmClient llmClient,
                                 QuestionValidator questionValidator,
                                 QuestionSegmenter questionSegmenter) {
        return new QnaService(knowledgeBaseRepository, questionLogRepository,
                answerFeedbackRepository, llmClient, questionValidator, questionSegmenter);
    }

    @Bean
    public AuthService authService(UserRepository userRepository,
                                    TokenProvider tokenProvider) {
        return new AuthService(userRepository, tokenProvider);
    }

    @Bean
    public AdminService adminService(ContributionRepository contributionRepository,
                                      UserRepository userRepository) {
        return new AdminService(contributionRepository, userRepository);
    }

    @Bean
    public ContributionService contributionService(ContributionRepository contributionRepository) {
        return new ContributionService(contributionRepository);
    }
}
