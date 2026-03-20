# DEVELOPMENT_NOTES.md

## Overall Approach

This solution is designed with focus on building a clean and
production-like structure using standard Spring Boot practices.

**High-level flow:** - Accept loan application request via REST API -
Validate incoming data early (fail fast) - Apply eligibility rules
step-by-step - Classify applicant risk - Calculate final interest rate -
Compute EMI and validate thresholds - Return either APPROVED or REJECTED
response - Persist the decision for audit purposes

------------------------------------------------------------------------

## Key Design Decisions

# 1. Layered Architecture (Controller → Service → Domain → Repository)

-   Keeps responsibilities clearly separated
-   Makes testing easier (mocking service/repository layers)
-   Improves maintainability and readability

------------------------------------------------------------------------

# 2. Breaking Business Logic into Small Components

Instead of putting everything in one service: - EMI calculation →
separate utility/service - Eligibility checks → dedicated logic - Risk
classification → isolated logic - Interest calculation → independent
component

This makes: - Code easier to read - Easier to test - Easier to extend
later

------------------------------------------------------------------------

# 3. Use of Enums

Used enums for: - EmploymentType - RiskBand - LoanPurpose -
RejectionReasons

Benefits: - Avoids invalid values - Improves code readability - Makes
switch-case logic cleaner

------------------------------------------------------------------------

# 4. Fail-Fast Validation Strategy

-   Invalid inputs are rejected immediately with HTTP 400
-   Prevents unnecessary processing
-   Keeps service efficient

------------------------------------------------------------------------

## Trade-offs Considered

# 1. Simplicity vs Flexibility

-   Implemented rules directly in code
-   Did NOT use rule engines (like Drools)

# 2. Synchronous Processing

-   Entire flow runs synchronously

# 3. Minimal Persistence Layer

-   Basic repository used for storing decisions

------------------------------------------------------------------------

## Assumptions Made

-   Interest rate is annual and converted to monthly for EMI calculation
-   Credit score boundaries are inclusive
-   No handling of:
    -   Prepayments
    -   Foreclosure
    -   Late payments
-   No external integrations (credit bureau, KYC, fraud checks)

------------------------------------------------------------------------

## Improvements with More Time

# 1. Introduce Configurable Rule Engine

-   Move business rules to external config or rule engine (e.g., Drools)
-   Allows business team to change rules without code changes

------------------------------------------------------------------------

# 2. Add Proper Database Layer

-   Use PostgreSQL or MongoDB
-   Store:
    -   Application history
    -   Decision logs
    -   Audit trails

------------------------------------------------------------------------

# 3. Expand API Capabilities

-   Add GET APIs:
    -   Fetch application by ID
    -   List applications with filters
-   Add pagination and sorting

------------------------------------------------------------------------

# 4. Better Validation Framework

-   Custom validators (annotations)
-   Centralized exception handling
-   Structured error responses

------------------------------------------------------------------------

# 5. Performance & Scalability

-   Introduce async processing (Kafka / message queues)
-   Parallelize eligibility checks if needed

------------------------------------------------------------------------

# 6. Security Enhancements

-   Add authentication & authorization
-   JWT / OAuth2 integration

------------------------------------------------------------------------

# 7. Testing Improvements

-   Add integration tests
-   Add edge case coverage
-   Add contract testing

------------------------------------------------------------------------

# 8. Observability

-   Logging (ELK stack)
-   Metrics (Prometheus + Grafana)
-   Distributed tracing

------------------------------------------------------------------------

# 9. DevOps & Deployment

-   Dockerize the application
-   Add CI/CD pipeline
-   Environment-based configurations


