package com.example.dataset.service;

import com.example.dataset.dto.InsertResponse;
import com.example.dataset.dto.QueryResponse;
import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.model.DatasetRecord;
import com.example.dataset.model.SortOrder;
import com.example.dataset.repository.DatasetRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetService {

    private final DatasetRecordRepository repository;
    private final ObjectMapper mapper;
    private final RecordSorter sorter;
    private final RecordGrouper grouper;

    public DatasetService(DatasetRecordRepository repository, ObjectMapper mapper,
                          RecordSorter sorter, RecordGrouper grouper) {
        this.repository = repository;
        this.mapper = mapper;
        this.sorter = sorter;
        this.grouper = grouper;
    }

    public InsertResponse insert(String datasetName, JsonNode body) {
        if (body == null || !body.isObject() || body.isEmpty()) {
            throw new InvalidRequestException("Request body must be a non-empty JSON object");
        }
        DatasetRecord saved = repository.save(new DatasetRecord(datasetName, body.toString()));
        return new InsertResponse("Record added successfully", datasetName, resolveRecordId(body, saved));
    }

    public QueryResponse query(String datasetName, String groupBy, String sortBy, String order) {
        SortOrder sortOrder = SortOrder.from(order);

        List<JsonNode> records = repository.findByDatasetNameOrderByIdAsc(datasetName).stream()
                .map(this::parse)
                .toList();
        if (records.isEmpty()) {
            throw new DatasetNotFoundException(datasetName);
        }

        if (hasText(sortBy)) {
            records = sorter.sort(records, sortBy, sortOrder);
        }
        if (hasText(groupBy)) {
            return QueryResponse.grouped(grouper.group(records, groupBy));
        }
        return QueryResponse.sorted(records);
    }

    /** Uses the record's own "id" when it is an integer (as in the sample), else the database id. */
    private Long resolveRecordId(JsonNode body, DatasetRecord saved) {
        JsonNode id = body.get("id");
        if (id != null && id.isIntegralNumber()) {
            return id.asLong();
        }
        return saved.getId();
    }

    private JsonNode parse(DatasetRecord record) {
        try {
            return mapper.readTree(record.getData());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Corrupt JSON in record " + record.getId(), e);
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
