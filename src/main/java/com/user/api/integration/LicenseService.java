package com.user.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LicenseService {

    @Autowired
    private LicenseServiceClient client;

	public String ping(){
        client.ping();
		return "pong";
	}

    public String addLicense(LicenseRequest request){
        return client.addLicense(request);
    }    
}
