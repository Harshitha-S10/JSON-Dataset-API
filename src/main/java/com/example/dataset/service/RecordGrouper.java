package com.example.dataset.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Groups JSON records by the value of any field. Records without the field go under "UNKNOWN". */
@Component
public class RecordGrouper {

    static final String UNKNOWN_KEY = "UNKNOWN";

    public Map<String, List<JsonNode>> group(List<JsonNode> records, String field) {
        Map<String, List<JsonNode>> groups = new LinkedHashMap<>();
        for (JsonNode record : records) {
            JsonNode value = record.get(field);
            String key = (value == null || value.isNull()) ? UNKNOWN_KEY : value.asText();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        return groups;
    }
}
