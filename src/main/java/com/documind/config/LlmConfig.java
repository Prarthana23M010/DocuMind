package com.documind.config;

import com.documind.llm.LlmClient;
import com.documind.llm.OllamaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class LlmConfig {

    @Bean
    public LlmClient llmClient(@Value("${ollama.base-url}") String baseUrl, @Value("${ollama.model}") String model) {
        return new OllamaClient(baseUrl,model);
    }
}
