package com.example.dataset.repository;

import com.example.dataset.model.DatasetRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetRecordRepository extends JpaRepository<DatasetRecord, Long> {
    List<DatasetRecord> findByDatasetNameOrderByIdAsc(String datasetName);
}
