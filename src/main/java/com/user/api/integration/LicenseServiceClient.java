package com.user.api.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "${licenseservice.name}", url = "${licenseservice.api.url}")
public interface LicenseServiceClient {
    @GetMapping(value="/ping")
    public String ping();

    @PostMapping(value = "/")
    public String addLicense(LicenseRequest request);
    
}
