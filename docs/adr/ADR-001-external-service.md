# ADR-001: Build LOS as External Standalone Service


## Context
The Fineract community needs a Loan Origination System. Two options exist:
build inside Fineract core, or build as a standalone external service.
Building inside core risks collisions with ongoing development and makes
the POC hard to demo independently.

## Decision
Build as a fully external standalone Spring Boot service that communicates
with Fineract exclusively through its published REST APIs. No changes are
made to Fineract core modules, no Liquibase migrations are added to the
Fineract repository, and no Fineract internal classes are extended.

## Consequences
- Zero risk of collision with ongoing Fineract core development
- Fully demonstrable without depending on unreleased Fineract features
- Clear integration path once FINERACT-2418 is resolved
- Independently deployable and testable
- Any version of Fineract can run alongside this service unchanged