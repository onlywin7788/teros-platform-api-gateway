package com.teros.api_gateway.service.remote;

import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Import(FeignClientsConfiguration.class)
public class RemoteServiceImpl {

    private final RemoteService remoteService;

    public RemoteServiceImpl(RemoteService remoteService) {
        this.remoteService = remoteService;
    }

    public String getRoute(){
        String route =  remoteService.getRoute();
        return route;
    }

}
