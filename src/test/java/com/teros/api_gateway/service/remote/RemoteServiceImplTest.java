package com.teros.api_gateway.service.remote;

import com.google.gson.*;
import com.netflix.discovery.converters.Auto;
import com.teros.api_gateway.service.schedule.RouteUpdateSchedule;
import org.apache.commons.io.FileUtils;
import org.aspectj.weaver.patterns.IToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class RemoteServiceImplTest {

    String baseURL = "http://10.10.2.102:48082";
    RouteUpdateSchedule routeUpdateSchedule = new RouteUpdateSchedule();

    private final int ROUTER_CREATE = 1000;
    private final int ROUTER_UPDATE = 1001;
    private final int ROUTER_DELETE = 1002;

    @Test
    public void routeTest() {
        Map<String, String> currentRouteMap = new HashMap<String, String>();
        Map<String, String> requestRouteMap = new HashMap<String, String>();
        Map<String, Integer> refreshRouteMap = new HashMap<String, Integer>();

        // store current route
        List<String> currentRouteIdList = getCurrentRouteIdList();
        for (int i = 0; i < currentRouteIdList.size(); i++) {
            String currentRouteId = currentRouteIdList.get(i);
            List<String> parseList = splitRouterBaseCheck(currentRouteId);
            currentRouteMap.put(parseList.get(0), parseList.get(1));
        }

        String contents = getRequestRouteContens();

        // store request route
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonParser Parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) Parser.parse(contents);
        JsonArray routeArray = (JsonArray) jsonObj.get("routeList");

        for (int i = 0; i < routeArray.size(); i++) {

            JsonObject object = (JsonObject) routeArray.get(i);
            String requestRouteId = object.get("id").getAsString();
            List<String> parseList = splitRouterBaseCheck(requestRouteId);
            requestRouteMap.put(parseList.get(0), parseList.get(1));
        }

        Set iterMap = null;
        // current 에는 존재하지만 request 에 없으면, ROUTE DELETE
        iterMap = currentRouteMap.keySet();
        for (Iterator iterator = iterMap.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();

            if (requestRouteMap.containsKey(key) == false) {
                refreshRouteMap.put(key, ROUTER_DELETE);
            }
        }

        // request 에는 존재하지만 request 에 없으면, ROUTE DELETE
        iterMap = requestRouteMap.keySet();
        for (Iterator iterator = iterMap.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();

            if (currentRouteMap.containsKey(key) == false) {
                refreshRouteMap.put(key, ROUTER_CREATE);
            } else {
                String currentRouteCheck = (String) requestRouteMap.get(key);
                String requestRouteCheck = (String) currentRouteMap.get(key);

                if (currentRouteCheck.equals(requestRouteCheck) == false) {
                    refreshRouteMap.put(key, ROUTER_UPDATE);
                }
            }
        }

        // REFRESH CURRENT ROUTE LIST
        refreshRouteList(refreshRouteMap);

    }

    public void refreshRouteList(Map<String, Integer> refreshRouteMap) {

        Set iterMap = refreshRouteMap.keySet();
        for (Iterator iterator = iterMap.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            int value = refreshRouteMap.get(key);

            if (value == ROUTER_CREATE || value == ROUTER_UPDATE) {

                String contents = getRequestRouteContens();

                JsonParser Parser = new JsonParser();
                JsonObject jsonObj = (JsonObject) Parser.parse(contents);
                JsonArray routeArray = (JsonArray) jsonObj.get("routeList");

                for (int i = 0; i < routeArray.size(); i++) {
                    JsonObject object = (JsonObject) routeArray.get(i);
                    String requestRouteId = object.get("id").getAsString();

                    List<String> requestRouteInfo = splitRouterBaseCheck(requestRouteId);
                    String requestRouteBaseId = requestRouteInfo.get(0);

                    if (key.equals(requestRouteBaseId)) {
                        String routeString = object.getAsString();
                        int a = 5;
                    }
                }

                requestRefreshRoute(key, HttpMethod.POST, "");

            } else if (value == ROUTER_DELETE) {
                requestRefreshRoute(key, HttpMethod.DELETE, "");
            }
        }
    }

    public void requestRefreshRoute(String routerId, HttpMethod method, String payload) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        /*
        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headerMap);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                method, requestEntity, String.class);
*/

    }

    public String getRequestRouteContens() {
        String baseHome = "e:/teros_home";
        String configPath = baseHome + File.separator + "config"
                + File.separator + "data-service" + File.separator + "data-service-router.json";

        String contents = "";
        if (fileExist(configPath) == true) {
            contents = readFile(configPath);
        }

        return contents;
    }

    public List<String> splitRouterBaseCheck(String routeId) {
        StringTokenizer token = null;
        String tokenValue = "-";
        List<String> splitRoute = new ArrayList<>();

        token = new StringTokenizer(routeId, tokenValue);

        String parseRouteId = "";
        String parseRouteCheckValue = "";

        int tokenIdx = 0;
        int tokenCount = token.countTokens();
        while (token.hasMoreTokens()) {
            tokenIdx++;

            if (tokenIdx < tokenCount) {
                parseRouteId += token.nextToken();
            } else
                parseRouteCheckValue = token.nextToken();

            if (tokenIdx < tokenCount - 1) {
                parseRouteId += "-";
            }
        }

        splitRoute.add(parseRouteId);
        splitRoute.add(parseRouteCheckValue);

        return splitRoute;
    }


    public List<String> getCurrentRouteIdList() {

        List<String> list = new ArrayList<String>();

        String url = baseURL + "/actuator/gateway/routes";
        String contents = RequestRouter(url);

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonParser Parser = new JsonParser();
        JsonArray routeArray = (JsonArray) Parser.parse(contents);

        for (int i = 0; i < routeArray.size(); i++) {

            JsonObject object = (JsonObject) routeArray.get(i);
            String routeId = object.get("route_id").getAsString();
            list.add(routeId);
        }
        return list;
    }


    public String RequestRouter(String url) {
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> headers = new HttpEntity<>(headerMap);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                HttpMethod.GET, headers, String.class);

        return responseEntity.getBody();
    }

    public boolean fileExist(String filePath) {
        if (Files.exists(Paths.get(filePath)))
            return true;
        else
            return false;
    }

    public String readFile(String ConfigPath) {
        String contents = "";
        try {
            File file = new File(ConfigPath);
            contents = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

}