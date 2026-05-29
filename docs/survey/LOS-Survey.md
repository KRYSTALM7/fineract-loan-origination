<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
-->
# Loan Origination System — Survey Findings (D1)

## 1. Purpose

This document presents the findings of the survey conducted
as part of FINERACT-2442 (GSoC 2026). The survey examines existing
open-source loan origination systems to inform the design of the
Fineract LOS POC. All API designs, entity structures, and workflow
stages in this project are derived from these findings.

---

## 2. Systems Surveyed

### 2.1 Mifos Workflow (GSoC 2025)

**Repository:** https://github.com/openMF/mifos-workflow

Mifos Workflow is a GSoC 2025 output a standalone Spring Boot service
that wraps Apache Fineract operations inside a Flowable BPMN 2.0 workflow
engine. It is the closest existing open-source attempt at workflow-driven
loan origination on top of Fineract.

**Loan Origination Endpoints:**
- POST /workflow/loan-origination/start
- POST /workflow/loan-origination/approve
- POST /workflow/loan-origination/reject
- POST /workflow/loan-disbursement/start
- POST /workflows/loan-cancellation/start

**Fineract Integration Payload (POST /loans):**
```json
{
  "clientId": 123,
  "productId": 1,
  "principal": 10000.00,
  "loanTermFrequency": 12,
  "loanTermFrequencyType": 2,
  "loanType": "individual",
  "loanPurposeId": 1,
  "interestRatePerPeriod": 15.0,
  "amortizationType": 1,
  "numberOfRepayments": 12,
  "repaymentEvery": 1,
  "expectedDisbursementDate": "2024-01-15",
  "submittedOnDate": "2024-01-01"
}
```

**Key Findings:**
- No explicit application state machine lifecycle managed by Flowable internally
- Single approve/reject only no multi-stage approval hierarchy
- No applicant data model beyond clientId
- No credit scoring
- No mock adapter for Fineract
- Multi-tenancy relies entirely on Fineract auth headers

---

### 2.2 OpenCBS Loan Origination Solution

**Reference:** https://opencbs.com

OpenCBS is the most feature-complete open-source LOS reference for MFI
environments. It validates core design decisions in the Fineract LOS POC.

**Origination Flow:**
- Multi-channel intake — loan officer, web, tablet field app
- Configurable approval routing by product, amount, client type
- Explicit re-work state validates the REFERRED transition
- Digital credit committee with multiple approval levels
- Task management inbox pending, re-work, approved tracking
- AML blacklist check at intake
- Pluggable external scoring system

**Key Findings:**
- Configurable routing confirms the multi-stage approval design
- Re-work state directly validates REFERRED → UNDER_REVIEW transition
- Pluggable scoring confirms the CreditScoringStrategy interface pattern
- No Fineract integration, separate commercial system
- LOS module is proprietary, no public source code

---


## 3. Comparison Matrix

| Capability | Mifos Workflow | OpenCBS LOS | Fineract LOS POC |
|---|---|---|---|
| State machine | None — BPMN implicit | Task-based | Explicit 6-state machine |
| Multi-stage approval | Single stage | Configurable | Configurable multi-stage |
| Applicant data model | clientId only | Full profile | Full ApplicantProfile entity |
| Credit scoring | Absent | External system | Rule-based + pluggable |
| Document tracking | Absent | Present | RequiredDocument entity |
| Mock Fineract adapter | Absent | Not applicable | Present |
| Fineract integration | Direct REST | None | DisbursementBridgeService |
| Open source | Yes | Core only | Yes — Apache 2.0 |
| Multi-tenancy | Via Fineract | Multi-branch | Own tenant_id |

---

## 4. Workflow Blueprint — Minimum Viable Origination Stages

Based on survey findings, these stages are universal across all
institution types and must be supported by any Fineract-compatible LOS:

| Stage | Who Acts | Output State |
|---|---|---|
| Application Intake | Applicant / Agent | DRAFT |
| Document Submission | Applicant | SUBMITTED |
| Initial Review | Loan Officer | UNDER_REVIEW or REFERRED |
| Credit Assessment | System (auto) | Score recorded |
| Approval Decision | Branch Manager | APPROVED or REJECTED |
| Disbursement | System (auto) | DISBURSED — loan created in Fineract |

---

## 5. Key Design Decisions Validated by Survey

**1. Custom state machine over BPMN**
Mifos Workflow shows that BPMN makes state implicit and
infrastructure-heavy. Explicit states are more queryable and testable.
See ADR-002.

**2. Configurable multi-stage approval**
OpenCBS routes applications by loan amount, product, and client type.
Our configurable approval chain follows the same principle.
See ADR-001.

**3. Pluggable scoring interface**
OpenCBS links to external scoring rather than embedding it.
Our CreditScoringStrategy interface follows the same pattern.
See ADR-003.

**4. Mock adapter for Fineract bridge**
Neither surveyed system handles Fineract unavailability gracefully.
Our FineractIntegrationPort with mock fallback solves this directly.