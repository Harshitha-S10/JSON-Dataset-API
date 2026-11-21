Demo video (Loom):https://www.loom.com/share/22811873983649cdb88cfc94fc86d50f
Swagger UI:http://localhost:8080/swagger-ui/index.html#/dataset-controller/query
GitHub:https://github.com/Harshitha-S10


# Dataset API

A Spring Boot REST application for storing JSON records in named datasets and querying them with dynamic **group-by** and **sort-by**.

## Tech stack
Java 17 · Spring Boot 3.2 · Spring Data JPA · H2 (in-memory relational DB) · JUnit 5 · Mockito · MockMvc · Maven

## Run
Prerequisites: JDK 17+ and Maven 3.8+.

```bash
mvn spring-boot:run     # starts on http://localhost:8080
mvn test                # runs all unit + integration tests
```

H2 console (optional): http://localhost:8080/h2-console, JDBC URL `jdbc:h2:mem:datasetdb`, user `sa`, empty password.
To use PostgreSQL/MySQL, add the driver to `pom.xml` and change the `spring.datasource.*` properties.

A ready-made Postman collection is in `postman/dataset-api.postman_collection.json` (import it into Postman).

## Design
- **Layers:** Controller -> Service -> Repository, with DTOs (Java records) between layers.
- **Storage:** one table `dataset_record(id, dataset_name, data)`. The JSON is stored as text so any schema works without creating tables per dataset.
- **Group / sort:** done in the service layer with Jackson `JsonNode`, using two small single-responsibility components, `RecordGrouper` and `RecordSorter`, so they are unit-testable and swappable.
- **Errors:** `@RestControllerAdvice` (`GlobalExceptionHandler`) returns a consistent JSON error body.

## API

### 1. Insert record
`POST /api/dataset/{datasetName}/record`

Body: any non-empty JSON object.
```json
{ "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
```
Response `201 Created`:
```json
{ "message": "Record added successfully", "dataset": "employee_dataset", "recordId": 1 }
```
`recordId` is the record's own integer `id` field if present, otherwise the database-generated id.

### 2. Query records
`GET /api/dataset/{datasetName}/query`

| Param   | Required | Description                                   |
|---------|----------|-----------------------------------------------|
| groupBy | no       | JSON field to group by                        |
| sortBy  | no       | JSON field to sort by                         |
| order   | no       | `asc` (default) or `desc`, used with `sortBy` |

**Group by** `?groupBy=department` -> `200`
```json
{ "groupedRecords": { "Engineering": [ {"id":1,"name":"John Doe","age":30,"department":"Engineering"} ], "Marketing": [ ] } }
```
**Sort by** `?sortBy=age&order=asc` -> `200`
```json
{ "sortedRecords": [ {"id":2,"name":"Jane Smith","age":25,"department":"Engineering"} ] }
```
If both are given, records are sorted first and then grouped (each group stays sorted).
With neither, all records are returned under `sortedRecords` in insertion order.

Rules: numbers sort numerically, text sorts case-insensitively, records missing the field sort last / group under `UNKNOWN`.

### Errors
```json
{ "status": 404, "error": "Not Found", "message": "Dataset 'x' not found or has no records", "timestamp": "2026-11-20T10:00:00Z" }
```
| Status | When |
|--------|------|
| 400 | malformed/empty JSON body, body not a JSON object, invalid `order` |
| 404 | dataset has no records / unknown URL |
| 405 | wrong HTTP method |
| 500 | unexpected server error |

## Tests (TDD)
- `RecordSorterTest`, `RecordGrouperTest`: pure unit tests
- `DatasetServiceTest`: Mockito
- `DatasetControllerTest`: `@WebMvcTest` + MockMvc
- `DatasetApiIntegrationTest`: full stack with H2

## Possible improvements
Pagination, filtering, JSON-column queries in PostgreSQL (`jsonb`), OpenAPI/Swagger docs, authentication.
