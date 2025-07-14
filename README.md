# Mission Awareness Service (MAS)

This project is a Spring Boot application that provides REST APIs to process satellite imaging activities, as per the Planet Assessment.

## Tech Stack

* **Java 17**
* **Spring Boot 3.1.5**
* **Gradle** (for dependency management)
* **Spock Framework** (for testing)
* **Docker & Docker Compose** (for containerization and deployment)

## Project Structure

```
src/main/java
├── controller        # REST endpoints (e.g., ImagingController)
├── dto              # Data Transfer Objects (e.g., ImagingActivity)
└── service          # Core business logic
src/test/groovy      # Spock tests
data/                # Sample input files (e.g., imaging_activities.json)
Dockerfile
docker-compose.yml
```

## Prerequisites

* Docker Desktop installed and running
* Java 17+ and Gradle (if running without Docker)

## How to Run

To run the application using Docker:

```bash
docker-compose up --build
```

Once started, the service will be available at:
**[http://localhost:8080](http://localhost:8080)**

---

## API Endpoints

### Task 1 – Get Chronological Imaging Window

Returns all activities sorted by their `start_time`.

* **Method:** `GET`
* **URL:** `/api/imaging/chronological-window`
* **Response:**
  `200 OK` with JSON array of imaging activities in chronological order.

#### Example:

```bash
curl -X GET http://localhost:8080/api/imaging/chronological-window
```

---

### Task 2 – Split Imaging Windows by Activity State (Streaming)

Returns imaging windows grouped by activity state and time streaming rules.

* **Method:** `POST`
* **URL:** `/api/imaging/split-windows`
* **Request Body:**

```json
{
  "activityState": "proposed"
}
```

* **Response:**
  `200 OK` with `List<List<ImagingActivity>>`, grouped into non-overlapping chronological windows with the same `activity_state`.

#### Example:

```bash
curl -X POST http://localhost:8080/api/imaging/split-windows \
  -H "Content-Type: application/json" \
  -d '{ "activityState": "scheduled" }'
```

---

## How to Run Tests

To manually run the test suite (via Gradle):

```bash
./gradlew clean test
```

If using Docker, tests are executed as part of the container build.

