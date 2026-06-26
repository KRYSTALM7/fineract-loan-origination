# Contributing to Apache Fineract Loan Origination

Thank you for your interest in contributing to Apache Fineract Loan Origination!

This project provides a standalone Loan Origination Service designed to integrate with Apache Fineract. While it was initiated during **Google Summer of Code (GSoC) 2026** under **[FINERACT-2442](https://issues.apache.org/jira/browse/FINERACT-2442)**, it is intended to evolve as a community-driven Apache project. Contributions of all sizes are welcome, including new features, bug fixes, documentation improvements, tests, and design discussions.

---

## Getting Started

Before contributing, we recommend becoming familiar with the project and its architecture.

1. Read the [README](Readme.md) for an overview and setup instructions.
2. Review the [Project Survey](docs/survey/LOS-Survey.adoc) to understand the motivation and design goals.
3. Read the [Architecture Decision Records](docs/adr/) to understand the architectural decisions that guide the project.
4. Join the [Apache Fineract Developer Mailing List](mailto:dev@fineract.apache.org) to participate in discussions.
5. Review the related **[FINERACT-2442 Jira Issue](https://issues.apache.org/jira/browse/FINERACT-2442)**.
6. Fork the repository and create a feature branch from `main`.

---

## Development Guidelines

Please follow the same development practices used throughout Apache Fineract.

- Keep pull requests small and focused.
- Add unit tests where appropriate.
- Ensure CI passes before opening a PR.
- Include the Apache License header in all new source files.
- Follow the existing project structure and coding conventions.

Before opening a pull request, run:

```bash
./mvnw spotless:apply
./mvnw spotless:check
./mvnw apache-rat:check
./mvnw clean verify
```

---

## Coding Conventions

This module follows the conventions of the broader Apache Fineract project.

For general contribution guidelines, see the official
[Apache Fineract CONTRIBUTING Guide](https://github.com/apache/fineract/blob/develop/CONTRIBUTING.md).

Project-specific conventions include:

- Package root: `org.apache.fineract.los`
- REST controllers should follow the existing `ApiResource` naming convention.
- Favor interface-based designs for pluggable components (credit scoring, Fineract integrations, workflow engines, etc.).
- New implementations should extend existing abstractions rather than modify them whenever possible.

---

## Getting Help

If you have questions or would like to discuss an implementation:

- Apache Fineract Developer Mailing List: <dev@fineract.apache.org>
- Matrix: <https://matrix.to/#/#apache-fineract-gsoc:matrix.org>
- Jira: https://issues.apache.org/jira/projects/FINERACT

---

## Acknowledgements

This project was initiated during **Google Summer of Code 2026** under **[FINERACT-2442](https://issues.apache.org/jira/browse/FINERACT-2442)**.

Special thanks to everyone who has helped guide the project:

- **[James Dailey](https://github.com/jdailey)** — Primary Mentor
- **[Aman Mittal](https://github.com/Aman-Mittal)** — Mentor
- **[PaulTofunmi](https://github.com/paultofunmi)** — Reviewer
- **[Attila Budai](https://github.com/budaidev)** — Reviewer

Initial implementation by **[Sujan Kumar MV (KRYSTALM7)](https://github.com/KRYSTALM7)** as part of Google Summer of Code 2026.