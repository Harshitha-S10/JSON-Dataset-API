package com.example.dataset.exception;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(String datasetName) {
        super("Dataset '" + datasetName + "' not found or has no records");
    }
}
