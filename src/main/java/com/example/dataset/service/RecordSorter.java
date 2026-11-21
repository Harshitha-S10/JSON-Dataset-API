package com.example.dataset.service;

import com.example.dataset.model.SortOrder;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/** Sorts JSON records by any field. Numbers compare numerically, everything else as text. Missing values go last. */
@Component
public class RecordSorter {

    public List<JsonNode> sort(List<JsonNode> records, String field, SortOrder order) {
        Comparator<JsonNode> valueComparator = this::compareValues;
        if (order == SortOrder.DESC) {
            valueComparator = valueComparator.reversed();
        }
        Comparator<JsonNode> comparator =
                Comparator.comparing(r -> valueOf(r, field), Comparator.nullsLast(valueComparator));
        return records.stream().sorted(comparator).toList();
    }

    private JsonNode valueOf(JsonNode record, String field) {
        JsonNode value = record.get(field);
        return (value == null || value.isNull()) ? null : value;
    }

    private int compareValues(JsonNode a, JsonNode b) {
        if (a.isNumber() && b.isNumber()) {
            return a.decimalValue().compareTo(b.decimalValue());
        }
        return a.asText().compareToIgnoreCase(b.asText());
    }
}
