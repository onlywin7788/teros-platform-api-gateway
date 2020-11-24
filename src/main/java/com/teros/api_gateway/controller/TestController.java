package com.teros.api_gateway.controller;

import com.teros.api_gateway.service.local.LocalServiceImpl;
import com.teros.api_gateway.service.remote.RemoteServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/v1")
public class TestController {

    private RemoteServiceImpl remoteServiceImpl;
    private LocalServiceImpl localServiceImpl;

    public TestController(RemoteServiceImpl remoteServiceImpl, LocalServiceImpl localServiceImpl) {
        this.remoteServiceImpl = remoteServiceImpl;
        this.localServiceImpl = localServiceImpl;
    }

    @GetMapping("/test")
    public void test() {
        String routerRule = getRouterRule();
        System.out.println(routerRule);

        createRouter("test_001", routerRule);
        refreshRouter();
    }

    public String getRouterRule() {
        System.out.println("GET ROUTER RULE");
        return remoteServiceImpl.getRoute();
    }

    public void createRouter(String id, String body) {
        System.out.println("CREATE ROUTER");
        localServiceImpl.createRouter(id, body);
    }

    public void refreshRouter() {
        System.out.println("REFRESH ROUTER");
        localServiceImpl.refreshRouter();
    }
}