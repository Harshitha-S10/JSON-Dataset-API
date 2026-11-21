package com.example.dataset.controller;

import com.example.dataset.dto.InsertResponse;
import com.example.dataset.dto.QueryResponse;
import com.example.dataset.service.DatasetService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dataset/{datasetName}")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @PostMapping("/record")
    public ResponseEntity<InsertResponse> insert(@PathVariable String datasetName,
                                                 @RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(datasetName, body));
    }

    @GetMapping("/query")
    public ResponseEntity<QueryResponse> query(@PathVariable String datasetName,
                                               @RequestParam(required = false) String groupBy,
                                               @RequestParam(required = false) String sortBy,
                                               @RequestParam(required = false) String order) {
        return ResponseEntity.ok(service.query(datasetName, groupBy, sortBy, order));
    }
}
