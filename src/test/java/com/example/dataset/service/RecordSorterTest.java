package com.example.dataset.service;

import com.example.dataset.model.SortOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordSorterTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RecordSorter sorter = new RecordSorter();

    private JsonNode json(String s) throws Exception {
        return mapper.readTree(s);
    }

    @Test
    void sortsNumericFieldAscending() throws Exception {
        List<JsonNode> records = List.of(
                json("{\"name\":\"John\",\"age\":30}"),
                json("{\"name\":\"Jane\",\"age\":25}"),
                json("{\"name\":\"Alice\",\"age\":28}"));

        List<JsonNode> result = sorter.sort(records, "age", SortOrder.ASC);

        assertEquals(List.of(25, 28, 30), result.stream().map(r -> r.get("age").asInt()).toList());
    }

    @Test
    void sortsNumericFieldDescending() throws Exception {
        List<JsonNode> records = List.of(json("{\"age\":25}"), json("{\"age\":30}"), json("{\"age\":28}"));

        List<JsonNode> result = sorter.sort(records, "age", SortOrder.DESC);

        assertEquals(List.of(30, 28, 25), result.stream().map(r -> r.get("age").asInt()).toList());
    }

    @Test
    void numbersAreComparedNumericallyNotAsText() throws Exception {
        List<JsonNode> records = List.of(json("{\"n\":9}"), json("{\"n\":100}"));

        List<JsonNode> result = sorter.sort(records, "n", SortOrder.ASC);

        assertEquals(9, result.get(0).get("n").asInt());
    }

    @Test
    void sortsTextIgnoringCase() throws Exception {
        List<JsonNode> records = List.of(json("{\"name\":\"bob\"}"), json("{\"name\":\"Alice\"}"));

        List<JsonNode> result = sorter.sort(records, "name", SortOrder.ASC);

        assertEquals("Alice", result.get(0).get("name").asText());
    }

    @Test
    void recordsMissingTheFieldAreAlwaysLast() throws Exception {
        List<JsonNode> records = List.of(json("{\"id\":1}"), json("{\"age\":20}"), json("{\"age\":10}"));

        List<JsonNode> asc = sorter.sort(records, "age", SortOrder.ASC);
        List<JsonNode> desc = sorter.sort(records, "age", SortOrder.DESC);

        assertEquals(10, asc.get(0).get("age").asInt());
        assertEquals(1, asc.get(2).get("id").asInt());
        assertEquals(20, desc.get(0).get("age").asInt());
        assertEquals(1, desc.get(2).get("id").asInt());
    }
}
