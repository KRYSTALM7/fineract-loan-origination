# ADR-003: Pluggable Credit Scoring Interface

## Context
Different institutions use different scoring methods rule-based,
credit bureau, ML models, third-party APIs. OpenCBS validates this
pattern by linking to external scoring systems rather than embedding
logic. The scoring engine must not be hardcoded to one approach.

## Decision
Define a CreditScoringStrategy interface. The default implementation
is rule-based using CGAP microfinance guidelines with configurable weights:
- Income-to-loan ratio: 30%
- Existing debt burden: 25%
- Employment stability: 20%
- Repayment history: 15%
- Loan purpose risk: 10%

Future contributors can provide alternate implementations without
changing the API layer, workflow engine, or database schema.

## Consequences
- Default scoring works offline with no external dependencies
- Credit bureaus, ML models, or third-party APIs pluggable via interface
- Scoring weights configurable via application.properties
- Trade-off: interface adds one layer of indirection