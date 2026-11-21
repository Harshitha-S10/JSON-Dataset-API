package com.example.dataset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/** Only one of the two fields is populated; the other is omitted from the JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QueryResponse(Map<String, List<JsonNode>> groupedRecords,
                            List<JsonNode> sortedRecords) {

    public static QueryResponse grouped(Map<String, List<JsonNode>> groups) {
        return new QueryResponse(groups, null);
    }

    public static QueryResponse sorted(List<JsonNode> records) {
        return new QueryResponse(null, records);
    }
}
