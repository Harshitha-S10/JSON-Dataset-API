package com.example.dataset.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordGrouperTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RecordGrouper grouper = new RecordGrouper();

    @Test
    void groupsByField() throws Exception {
        List<JsonNode> records = List.of(
                mapper.readTree("{\"id\":1,\"department\":\"Engineering\"}"),
                mapper.readTree("{\"id\":2,\"department\":\"Engineering\"}"),
                mapper.readTree("{\"id\":3,\"department\":\"Marketing\"}"));

        Map<String, List<JsonNode>> grouped = grouper.group(records, "department");

        assertEquals(2, grouped.get("Engineering").size());
        assertEquals(1, grouped.get("Marketing").size());
    }

    @Test
    void recordsWithoutFieldGoToUnknownGroup() throws Exception {
        List<JsonNode> records = List.of(mapper.readTree("{\"id\":1}"));

        Map<String, List<JsonNode>> grouped = grouper.group(records, "department");

        assertEquals(1, grouped.get("UNKNOWN").size());
    }

    @Test
    void emptyInputGivesEmptyResult() {
        assertTrue(grouper.group(List.of(), "department").isEmpty());
    }
}
