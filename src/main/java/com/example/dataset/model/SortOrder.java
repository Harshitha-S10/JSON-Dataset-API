package com.example.dataset.model;

import com.example.dataset.exception.InvalidRequestException;

public enum SortOrder {
    ASC, DESC;

    /** Parses "asc"/"desc" (case-insensitive). Null or blank defaults to ASC. */
    public static SortOrder from(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }
        try {
            return SortOrder.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("order must be 'asc' or 'desc'");
        }
    }
}
