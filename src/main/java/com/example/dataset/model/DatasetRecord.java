package com.example.dataset.model;

import jakarta.persistence.*;

/** One JSON record belonging to a named dataset. The JSON is stored as text. */
@Entity
@Table(name = "dataset_record", indexes = @Index(name = "idx_dataset_name", columnList = "datasetName"))
public class DatasetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String datasetName;

    @Lob
    @Column(nullable = false)
    private String data;

    protected DatasetRecord() {
        // required by JPA
    }

    public DatasetRecord(String datasetName, String data) {
        this.datasetName = datasetName;
        this.data = data;
    }

    public Long getId() { return id; }
    public String getDatasetName() { return datasetName; }
    public String getData() { return data; }
}
