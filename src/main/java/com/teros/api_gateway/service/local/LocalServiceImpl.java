package com.teros.api_gateway.service.local;

import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Import(FeignClientsConfiguration.class)
public class LocalServiceImpl {

    private final LocalService localService;
    public LocalServiceImpl(LocalService localService) {
        this.localService = localService;
    }

    public void createRouter(String id, String body){
        localService.createRouter(id, body);
    }

    public void refreshRouter(){
        localService.refreshRouter();
    }

}
