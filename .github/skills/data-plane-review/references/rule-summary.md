# Java Data-Plane Review Rule Summary

Canonical definitions are grouped by review area:

- [`client-api.md`](client-api.md)
- [`operations.md`](operations.md)
- [`models-security.md`](models-security.md)
- [`versioning-build-generation.md`](versioning-build-generation.md)
- [`documentation-testing.md`](documentation-testing.md)

| Rule ID | Severity | Review target | Primary source |
| --- | --- | --- | --- |
| `DP-CLIENT-01` | Warning | Sync and async service clients | Java client guidelines |
| `DP-CLIENT-02` | Warning | Fluent client builder and valid construction | Java builder guidelines |
| `DP-API-01` | Warning | Implementation leakage | Java implementation and module guidelines |
| `DP-METHOD-01` | Suggestion | Service method names, overloads, options, and context | Java method guidelines |
| `DP-PAGING-01` | Warning | Collection return types | Java paging guidelines |
| `DP-LRO-01` | Warning | Poller types and `begin` methods | Java LRO guidelines |
| `DP-ASYNC-01` | Warning | Blocking work in async APIs | Java implementation guidelines |
| `DP-MODEL-01` | Warning | Public model usability and contract alignment | Java model guidelines |
| `DP-ENUM-01` | Warning | Extensible service values | Java enum guidelines |
| `DP-VALIDATION-01` | Warning | Client/service validation boundary | Java and general implementation guidelines |
| `DP-ERROR-01` | Warning | Exception shape and actionable errors | Java and general error guidelines |
| `DP-SECURITY-01` | Blocking | Credential handling and disclosure | Java auth/logging guidelines |
| `DP-CORE-01` | Warning | Azure Core pipeline, policies, tracing, and context | Java and general implementation guidelines |
| `DP-VERSION-01` | Blocking | GA public API compatibility | Java versioning guidelines |
| `DP-VERSION-02` | Blocking | Package stability vs. service API stability | General service-version guidelines |
| `DP-VERSION-03` | Warning | Repository version metadata | `CONTRIBUTING.md` and contributor docs |
| `DP-DOC-01` | Suggestion | Package README completeness | General documentation guidelines |
| `DP-DOC-02` | Suggestion | Runnable source-backed snippets | General documentation and repository build docs |
| `DP-DOC-03` | Suggestion | Public JavaDoc behavior and failures | Java and general documentation guidelines |
| `DP-BUILD-01` | Warning | Dependency scope, minimum set, and markers | Java module and repository version guidance |
| `DP-BUILD-02` | Warning | Standard POM and module registration | Java Maven and repository module guidance |
| `DP-SPEC-01` | Warning | Generated-code source ownership | Repository TypeSpec workflow and repeated review evidence |
| `DP-TEST-01` | Suggestion | HTTP-client and service-version coverage | Java testing guidelines |

The full trigger, exception, source comment, and correct form for each rule are
defined in the reference files above.
