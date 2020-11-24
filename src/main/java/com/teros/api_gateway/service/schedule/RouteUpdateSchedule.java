package com.teros.api_gateway.service.schedule;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teros.api_gateway.common.file.CommonFile;
import com.teros.api_gateway.common.remote.CommonRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

@Service
@Slf4j
public class RouteUpdateSchedule {

    private final CommonFile commonFile;
    private final CommonRemote commonRemote;

    @Value("${extra.env.teros_home}")
    private String baseHome;

    @Value("${server.port}")
    private String port;

    private String saveRequestRouteUid = "";
    private String baseURL = "";
    String configPath = "";
    String routeToken = "-";

    private final int ROUTER_CREATE = 1000;
    private final int ROUTER_UPDATE = 1001;
    private final int ROUTER_DELETE = 1002;

    public RouteUpdateSchedule(CommonFile commonFile, CommonRemote commonRemote) {
        this.commonFile = commonFile;
        this.commonRemote = commonRemote;
    }

    @Scheduled(fixedDelay = 3000)
    public void createRoute() throws Exception {

        baseURL = "http://localhost:" + port;
        configPath = baseHome + File.separator + "config"
                + File.separator + "api-gateway" + File.separator + "api-gateway-router.json";

        if (commonFile.fileExist(configPath) == false) {
            log.info("config file not exist :: [" + configPath + "]");
            return;
        }

        // route id check
        String requestRouteUid = getRequestRouteUidByFile();
        if (requestRouteUid.equals(saveRequestRouteUid) == true) {
            return;
        } else {
            saveRequestRouteUid = requestRouteUid;
            log.info(String.format("request route UID detected. UID:[%s], CONFIG_PATH:[%s]",  saveRequestRouteUid, configPath));
        }

        Map<String, String> currentRouteMap = new HashMap<String, String>();
        Map<String, String> requestRouteMap = new HashMap<String, String>();
        Map<String, Integer> refreshRouteMap = new HashMap<String, Integer>();

        currentRouteMap = getCurrentRouteList();
        requestRouteMap = getRequestRouteList();
        refreshRouteMap = getRefreshRouteList(currentRouteMap, requestRouteMap);

        if (refreshRouteMap.size() > 0) {
            updateRouteList(currentRouteMap, requestRouteMap, refreshRouteMap);
            commonRemote.requestRestEntity(baseURL + "/actuator/gateway/refresh" , HttpMethod.POST, "");
        }
    }


    public Map<String, String> getCurrentRouteList() {

        String url = baseURL + "/actuator/gateway/routes";
        String objectName = "route_id";

        Map<String, String> resultMap = new HashMap<String, String>();
        List<String> list = new ArrayList<String>();

        ResponseEntity<String> entity = commonRemote.requestRestEntity(url, HttpMethod.GET, "");
        String contents = entity.getBody();

        JsonParser Parser = new JsonParser();
        JsonArray routeArray = (JsonArray) Parser.parse(contents);

        for (int i = 0; i < routeArray.size(); i++) {
            JsonObject object = (JsonObject) routeArray.get(i);
            String routeId = object.get(objectName).getAsString();
            list.add(routeId);
        }

        for (int i = 0; i < list.size(); i++) {
            String currentRouteId = list.get(i);
            List<String> parseList = splitRouterInfo(currentRouteId);
            resultMap.put(parseList.get(0), parseList.get(1));
        }

        return resultMap;
    }

    public Map<String, String> getRequestRouteList() {
        Map<String, String> resultMap = new HashMap<String, String>();
        String contents = getRequestRouteListByFile();

        // store request route
        JsonParser Parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) Parser.parse(contents);
        JsonArray routeArray = (JsonArray) jsonObj.get("routeList");

        for (int i = 0; i < routeArray.size(); i++) {

            JsonObject object = (JsonObject) routeArray.get(i);
            String requestRouteId = object.get("id").getAsString();
            List<String> parseList = splitRouterInfo(requestRouteId);
            resultMap.put(parseList.get(0), parseList.get(1));
        }

        return resultMap;
    }

    public Map<String, Integer> getRefreshRouteList(Map<String, String> currentRouteMap
            , Map<String, String> requestRouteMap) {

        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        Set iterMap = null;

        // current 에는 존재하지만 request 에 없으면, ROUTE DELETE
        iterMap = currentRouteMap.keySet();
        for (Iterator iterator = iterMap.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();

            if (requestRouteMap.containsKey(key) == false) {
                resultMap.put(key, ROUTER_DELETE);
            }
        }


        iterMap = requestRouteMap.keySet();
        for (Iterator iterator = iterMap.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();

            // request 존재, current 미존재 = CREATE
            if (currentRouteMap.containsKey(key) == false) {
                resultMap.put(key, ROUTER_CREATE);
            } else {
                // request, current 모두 존재, datetime 이 변경되면 UPDATE (DELETE -> CREATE)
                String currentRouteCheck = (String) requestRouteMap.get(key);
                String requestRouteCheck = (String) currentRouteMap.get(key);

                if (currentRouteCheck.equals(requestRouteCheck) == false) {
                    resultMap.put(key, ROUTER_UPDATE);
                }
            }
        }
        return resultMap;
    }

    public void updateRouteList(Map<String, String> currentRouteMap,
                                Map<String, String> requestRouteMap,
                                Map<String, Integer> refreshRouteMap) {

        String url = baseURL + "/actuator/gateway/routes";
        String updateUrl = "";
        Set iterMap = null;

        iterMap = refreshRouteMap.keySet();
        for (Iterator iterator = iterMap.iterator(); iterator.hasNext(); ) {
            String refreshRotuteKey = (String) iterator.next();
            int refreshRotuteValue = refreshRouteMap.get(refreshRotuteKey);

            if (refreshRotuteValue == ROUTER_CREATE || refreshRotuteValue == ROUTER_UPDATE) {
                String requestRouteValue = (String) requestRouteMap.get(refreshRotuteKey);

                String routeContents = getRequestRouteByFile(refreshRotuteKey + routeToken + requestRouteValue);

                if (refreshRotuteValue == ROUTER_UPDATE) {
                    String currentRouteValue = (String) currentRouteMap.get(refreshRotuteKey);

                    // delete
                    updateUrl = url + "/" + refreshRotuteKey + routeToken + currentRouteValue;
                    commonRemote.requestRestEntity(updateUrl, HttpMethod.DELETE, "");
                }

                // create
                updateUrl = url + "/" + refreshRotuteKey + routeToken + requestRouteValue;
                commonRemote.requestRestEntity(updateUrl, HttpMethod.POST, routeContents);

            } else if (refreshRotuteValue == ROUTER_DELETE) {
                String currentRouteValue = (String) currentRouteMap.get(refreshRotuteKey);
                updateUrl = url + "/" + refreshRotuteKey + routeToken + currentRouteValue;
                commonRemote.requestRestEntity(updateUrl, HttpMethod.DELETE, "");
            }
        }
    }

    public String getRequestRouteListByFile() {
        String baseHome = "e:/teros_home";
        String contents = "";

        if (commonFile.fileExist(configPath) == true) {
            contents = commonFile.readFile(configPath);
        }
        return contents;
    }

    public String getRequestRouteByFile(String routeId) {

        String returnString = "";

        String contents = getRequestRouteListByFile();
        JsonParser Parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) Parser.parse(contents);
        JsonArray routeArray = (JsonArray) jsonObj.get("routeList");

        for (int i = 0; i < routeArray.size(); i++) {
            JsonObject object = (JsonObject) routeArray.get(i);
            String contentsRouteId = object.get("id").getAsString();

            if (routeId.equals(contentsRouteId) == true) {
                returnString = object.toString();
                break;
            }
        }
        return returnString;
    }

    public String getRequestRouteUidByFile() {

        String returnString = "";

        String contents = getRequestRouteListByFile();
        JsonParser Parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) Parser.parse(contents);
        return jsonObj.get("requestRouteUid").getAsString();
    }

    public List<String> splitRouterInfo(String routeId) {
        StringTokenizer token = null;
        List<String> splitRoute = new ArrayList<>();

        token = new StringTokenizer(routeId, routeToken);

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
                parseRouteId += routeToken;
            }
        }

        splitRoute.add(parseRouteId);
        splitRoute.add(parseRouteCheckValue);

        return splitRoute;
    }
}
