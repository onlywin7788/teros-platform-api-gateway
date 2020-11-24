package com.teros.api_gateway.service.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(name ="teros-api-control-manager")
public interface RemoteService {

    @RequestMapping("/v1/api-gw/refresh")
    String getRoute();
}