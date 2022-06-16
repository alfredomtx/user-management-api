package com.user.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/LicenseService")
public class LicenseServiceController {

    @Autowired
    private LicenseService service;

    @GetMapping(value = "ping")
    public String ping(){
        return service.ping();
    }
}
