package com.example.dataset.service;

import com.example.dataset.dto.InsertResponse;
import com.example.dataset.dto.QueryResponse;
import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.model.DatasetRecord;
import com.example.dataset.repository.DatasetRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetRecordRepository repository;

    private final ObjectMapper mapper = new ObjectMapper();
    private DatasetService service;

    @BeforeEach
    void setUp() {
        service = new DatasetService(repository, mapper, new RecordSorter(), new RecordGrouper());
    }

    private DatasetRecord record(String json) {
        return new DatasetRecord("emp", json);
    }

    @Test
    void insertSavesRecordAndReturnsBodyId() throws Exception {
        JsonNode body = mapper.readTree("{\"id\":1,\"name\":\"John\"}");
        when(repository.save(any(DatasetRecord.class))).thenAnswer(i -> i.getArgument(0));

        InsertResponse response = service.insert("emp", body);

        verify(repository).save(any(DatasetRecord.class));
        assertEquals("Record added successfully", response.message());
        assertEquals("emp", response.dataset());
        assertEquals(1L, response.recordId());
    }

    @Test
    void insertRejectsEmptyObject() throws Exception {
        JsonNode body = mapper.readTree("{}");

        assertThrows(InvalidRequestException.class, () -> service.insert("emp", body));
        verifyNoInteractions(repository);
    }

    @Test
    void insertRejectsNonObjectBody() throws Exception {
        JsonNode body = mapper.readTree("[1,2,3]");

        assertThrows(InvalidRequestException.class, () -> service.insert("emp", body));
    }

    @Test
    void queryThrowsWhenDatasetHasNoRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("missing")).thenReturn(List.of());

        assertThrows(DatasetNotFoundException.class, () -> service.query("missing", null, null, null));
    }

    @Test
    void queryRejectsInvalidOrder() {
        assertThrows(InvalidRequestException.class, () -> service.query("emp", null, "age", "sideways"));
    }

    @Test
    void queryGroupsRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(List.of(
                record("{\"id\":1,\"department\":\"Eng\"}"),
                record("{\"id\":2,\"department\":\"Eng\"}"),
                record("{\"id\":3,\"department\":\"Mkt\"}")));

        QueryResponse response = service.query("emp", "department", null, null);

        assertNull(response.sortedRecords());
        assertEquals(2, response.groupedRecords().get("Eng").size());
        assertEquals(1, response.groupedRecords().get("Mkt").size());
    }

    @Test
    void querySortsRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(List.of(
                record("{\"id\":1,\"age\":30}"),
                record("{\"id\":2,\"age\":25}")));

        QueryResponse response = service.query("emp", null, "age", "asc");

        assertNull(response.groupedRecords());
        assertEquals(25, response.sortedRecords().get(0).get("age").asInt());
    }

    @Test
    void queryWithoutParamsReturnsAllRecords() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(List.of(record("{\"id\":1}")));

        QueryResponse response = service.query("emp", null, null, null);

        assertEquals(1, response.sortedRecords().size());
    }
}
