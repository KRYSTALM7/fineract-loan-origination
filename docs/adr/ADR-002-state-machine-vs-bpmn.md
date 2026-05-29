# ADR-002: Custom State Machine over BPMN Engine

## Context
Mifos Workflow (GSoC 2025) used Flowable BPMN to manage loan lifecycle.
BPMN engines add significant infrastructure overhead 50+ internal tables,
own thread pool, own REST API. Application state is implicit inside the
engine and not directly queryable from the database.

## Decision
Implement a custom explicit state machine with named statuses:
DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED/REFERRED → DISBURSED.
Every invalid transition throws an explicit exception. Valid transitions
are defined in a transition map, making additions a one-line change.

## Consequences
- Application status directly queryable via SQL
- Every invalid transition explicitly blocked and testable
- No external engine dependency lighter infrastructure
- Unit testable without any running infrastructure
- Trade-off: complex conditional branching is harder than BPMN