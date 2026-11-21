package com.example.dataset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end test: real controller, service and H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class DatasetApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void insert(String dataset, String json) throws Exception {
        mockMvc.perform(post("/api/dataset/" + dataset + "/record")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void insertThenGroupAndSort() throws Exception {
        String ds = "it_employees";
        insert(ds, "{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}");
        insert(ds, "{\"id\":2,\"name\":\"Jane Smith\",\"age\":25,\"department\":\"Engineering\"}");
        insert(ds, "{\"id\":3,\"name\":\"Alice Brown\",\"age\":28,\"department\":\"Marketing\"}");

        mockMvc.perform(get("/api/dataset/" + ds + "/query").param("groupBy", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering.length()").value(2))
                .andExpect(jsonPath("$.groupedRecords.Marketing.length()").value(1));

        mockMvc.perform(get("/api/dataset/" + ds + "/query").param("sortBy", "age").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].name").value("Jane Smith"))
                .andExpect(jsonPath("$.sortedRecords[2].name").value("John Doe"));
    }

    @Test
    void unknownDatasetReturns404() throws Exception {
        mockMvc.perform(get("/api/dataset/does_not_exist/query"))
                .andExpect(status().isNotFound());
    }
}
