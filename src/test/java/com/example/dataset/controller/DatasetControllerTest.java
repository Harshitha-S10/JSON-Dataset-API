package com.example.dataset.controller;

import com.example.dataset.dto.InsertResponse;
import com.example.dataset.dto.QueryResponse;
import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.InvalidRequestException;
import com.example.dataset.service.DatasetService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatasetController.class)
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private DatasetService service;

    @Test
    void insertReturns201() throws Exception {
        when(service.insert(eq("emp"), any(JsonNode.class)))
                .thenReturn(new InsertResponse("Record added successfully", "emp", 1L));

        mockMvc.perform(post("/api/dataset/emp/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"John\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record added successfully"))
                .andExpect(jsonPath("$.dataset").value("emp"))
                .andExpect(jsonPath("$.recordId").value(1));
    }

    @Test
    void insertWithMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/dataset/emp/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void insertWithInvalidBodyReturns400() throws Exception {
        when(service.insert(eq("emp"), any(JsonNode.class)))
                .thenThrow(new InvalidRequestException("Request body must be a non-empty JSON object"));

        mockMvc.perform(post("/api/dataset/emp/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body must be a non-empty JSON object"));
    }

    @Test
    void queryWithGroupByReturns200() throws Exception {
        JsonNode rec = mapper.readTree("{\"id\":1,\"department\":\"Eng\"}");
        when(service.query("emp", "department", null, null))
                .thenReturn(QueryResponse.grouped(Map.of("Eng", List.of(rec))));

        mockMvc.perform(get("/api/dataset/emp/query").param("groupBy", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Eng[0].id").value(1))
                .andExpect(jsonPath("$.sortedRecords").doesNotExist());
    }

    @Test
    void queryWithSortByReturns200() throws Exception {
        JsonNode rec = mapper.readTree("{\"id\":2,\"age\":25}");
        when(service.query("emp", null, "age", "asc"))
                .thenReturn(QueryResponse.sorted(List.of(rec)));

        mockMvc.perform(get("/api/dataset/emp/query").param("sortBy", "age").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].age").value(25))
                .andExpect(jsonPath("$.groupedRecords").doesNotExist());
    }

    @Test
    void queryUnknownDatasetReturns404() throws Exception {
        when(service.query(eq("nope"), any(), any(), any()))
                .thenThrow(new DatasetNotFoundException("nope"));

        mockMvc.perform(get("/api/dataset/nope/query"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void queryWithInvalidOrderReturns400() throws Exception {
        when(service.query(eq("emp"), any(), any(), eq("xyz")))
                .thenThrow(new InvalidRequestException("order must be 'asc' or 'desc'"));

        mockMvc.perform(get("/api/dataset/emp/query").param("sortBy", "age").param("order", "xyz"))
                .andExpect(status().isBadRequest());
    }
}
