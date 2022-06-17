package com.user.api.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.form.ContentType;

public class LicenseServiceConfig {

	@Value("${licenseservice.api.apiKey}")
	private String apiKey;


    @Bean
    public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
        requestTemplate.header("apiKey", apiKey);
        requestTemplate.header("Accept", ContentType.APPLICATION_JSON.getMimeType());
    };
    }
    
}
