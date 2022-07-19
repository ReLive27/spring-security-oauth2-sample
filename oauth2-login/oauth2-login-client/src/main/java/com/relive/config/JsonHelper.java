package com.relive.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author: ReLive
 * @date: 2022/7/18 8:28 下午
 */
@Slf4j
public class JsonHelper {
    private static final JsonHelper.MapTypeReference MAP_TYPE = new JsonHelper.MapTypeReference();

    private static ObjectMapper mapper;

    private JsonHelper() {
    }

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static JsonNode getFirstNode(final JsonNode node, final String path) {
        JsonNode resultNode = null;
        if (path != null) {
            resultNode = getElement(node, path);
        }
        return resultNode;
    }

    public static JsonNode getElement(final JsonNode json, final String name) {
        if (json != null && name != null) {
            JsonNode node = json;
            for (String nodeName : name.split("\\.")) {
                if (node != null) {
                    if (nodeName.matches("\\d+")) {
                        node = node.get(Integer.parseInt(nodeName));
                    } else {
                        node = node.get(nodeName);
                    }
                }
            }
            if (node != null) {
                return node;
            }
        }
        return null;
    }


    public static Map<String, Object> parseMap(String json) {
        try {
            return mapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            log.error("Cannot convert json to map");
        }
        return null;
    }

    private static class MapTypeReference extends TypeReference<Map<String, Object>> {
        private MapTypeReference() {
        }
    }
}
