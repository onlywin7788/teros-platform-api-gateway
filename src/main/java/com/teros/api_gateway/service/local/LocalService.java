package com.teros.api_gateway.service.local;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(name ="teros-api-gateway")
public interface LocalService {

    @PostMapping(path = "/actuator/gateway/routes/{id}", consumes = "application/json", produces = "application/json")
    void createRouter(@PathVariable("id") String id, @RequestBody String body);

    @PostMapping("/actuator/gateway/refresh")
    void refreshRouter();
}