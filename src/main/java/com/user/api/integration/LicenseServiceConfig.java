package com.user.api.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class LicenseServiceConfig {

	@Value("${licenseservice.api.apiKey}")
	private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("apiKey", apiKey);
        };
    }
    
}
