# Customer Workflow Coverage Map

This file tracks the customer release-validation workflows from `e:\benchmark-tests` against the runnable Cosmos SDK tests in this module. Keep it updated as customer-derived workflows are ported, disabled, or marked duplicate.

## Classification

| Classification | Meaning |
|---|---|
| `gap` | Customer workflow has no equivalent SDK scenario coverage. |
| `partial` | SDK covers the primitives, but not the customer-style operation chain or diagnostics assertion. |
| `duplicate` | Existing SDK tests already cover the behavior with enough release-signal fidelity. |
| `wrapper-specific` | Assertion belongs to the customer's wrapper defaults, not an SDK contract. |
| `deferred` | Candidate workflow, but not enabled until runtime/flakiness/account-shape trade-offs are reviewed. |

## Initial Coverage Triage

| Customer source area | Customer workflow signal | Existing SDK references | Classification | Initial action |
|---|---|---|---|---|
| `test/CosmosDaoTest.java` | Create/read/query/upsert/delete, readAll, bulk/batch, custom serializer through a DAO wrapper | `CosmosItemTest`, `DocumentCrudTest`, `CosmosBulkTest`, `CosmosBulkAsyncTest`, `TransactionalBatchTest`, `CosmosItemSerializerTest` | `partial` / `wrapper-specific` | Port only customer-style operation chains with diagnostics assertions; mark DAO cache/session-map/default policy checks wrapper-specific. |
| `test/CosmosMultiFeatureTests.java` | App-style create/read/query/upsert/delete, keyword identifiers, invalid session token, no preferred region default routing | `CosmosDiagnosticsTest`, `ExcludeRegionTests`, `SessionConsistencyWithRegionScopingTests` | `partial` | Start with keyword identifier and region-routing workflows in `fi-customer-workflows`. |
| `test/CosmosDriverDynamicRequestOptionTest.java` | Dynamic operation policy changes request options per operation and validates request options through diagnostics | `OperationPoliciesTest`, `GatewayReadConsistencyStrategyE2ETest`, `CosmosLatestCommittedItemTests` | `partial` | Add customer workflow tests that combine create/read/query/readMany/upsert chains and diagnostics request-option validation. |
| `test/Latest_Committed_Tests.java` | Latest-committed with excluded regions, consistency combinations, RU and contacted-region expectations | `CosmosLatestCommittedItemTests`, `GatewayReadConsistencyStrategyE2ETest`, `ClientRetryPolicyE2ETests` | `partial` | Add focused live multi-region workflow rows; keep primitive latest-committed behavior as duplicate references. |
| `regression/direct/*.java` | Latest-committed direct-mode regression matrix for change feed, read, query, readMany, session/eventual combinations | `CosmosLatestCommittedItemTests`, change feed processor tests | `partial` | Port only variants that add multi-region workflow signal beyond existing latest-committed tests. |
| `regression/gateway/*.java` | Gateway latest-committed regression matrix | `GatewayReadConsistencyStrategyE2ETest`, `GatewayReadConsistencyStrategySpyWireTest` | `duplicate` / `partial` | Keep as duplicate unless the coverage table identifies a workflow assertion not present in gateway tests. |
| `test/CosmosHighE2ETimeoutTest.java` | E2E timeout behavior under response delay and partition migrating faults for create/query/readMany/batch | `EndToEndTimeOutValidationTests`, `EndToEndTimeOutWithAvailabilityTest`, `FaultInjectionWithAvailabilityStrategyTestsBase` | `partial` | Add one customer-style chain after request-option workflows stabilize. |
| `test/CosmosStoredProcedureTest.java` | Stored procedure create/read/update diagnostics under response delay and read-session-not-available faults | `StoredProcedureCrudTest`, `StoredProcedureQueryTest`, `StoredProcedureUpsertReplaceTest`, `CosmosSyncStoredProcTest` | `gap` / `partial` | Add targeted stored-procedure fault workflow that deploys scripts in setup. |
| `test/ChangeFeedProcessorTest.java` | CFP start/stop, latest-version handler, current state, restart, and fault-injected read feed | `IncrementalChangeFeedProcessorTest`, `FullFidelityChangeFeedProcessorTest`, `CosmosContainerChangeFeedTest` | `partial` | Add a small CFP workflow and replace fixed sleeps with polling. |
| `test/PartitionLevelCircuitBreakerTests.java` | PCLB app chain and query-plan behavior under regional faults | `PerPartitionCircuitBreakerE2ETests`, `PerPartitionAutomaticFailoverE2ETests` | `partial` | Add one PCLB-enabled workflow row after first live suite run. |
| `test/CosmosConflictResolutionTest.java` | Multi-client conflict detection and conflict query | `CosmosConflictsTest`, `ConflictTests`, `MultiMasterConflictResolutionTest` | `duplicate` / `partial` | Document existing coverage first; port only if customer ordering/diagnostics differs. |
| `test/Cosmos429test.java` | 429 and connection delay behavior in app-shaped calls | `RetryThrottleTest`, `ResourceThrottleRetryPolicyTest`, `FaultInjectionServerErrorRuleOnDirectTests`, `FaultInjectionServerErrorRuleOnGatewayTests` | `duplicate` / `partial` | Prefer parameterized FI rows; do not create a standalone clone. |
| `singlemaster/direct/*.java` | Single-write account availability strategies in direct mode | `EndToEndTimeOutWithAvailabilityTest`, `ExcludeRegionTests`, `FITests_*` | `deferred` | Document rows first; add a single-write multi-region matrix only if unique customer coverage remains. |
| `singlemaster/gateway/*.java` | Single-write account availability strategies in gateway mode | Gateway retry/fault-injection tests | `deferred` | Same as singlemaster/direct. |
| `multimaster/direct/*.java` | Multi-write direct availability strategy matrix across fault/status/operation combinations | `FaultInjectionWithAvailabilityStrategyTestsBase`, `FITests_*`, `PerPartitionAutomaticFailoverE2ETests` | `partial` | Port representative workflow matrix with TestNG data providers instead of one class per customer file. |
| `multimaster/gateway/*.java` | Multi-write gateway availability strategy matrix | `FaultInjectionServerErrorRuleOnGatewayTests`, `FaultInjectionServerErrorRuleOnGatewayV2Tests`, `FITests_*` | `partial` | Port selected gateway workflow rows after direct-mode baseline. |

## Enabled Suite

The initial implementation adds TestNG group `fi-customer-workflows`, Maven profile `-Pfi-customer-workflows`, and live matrix display name `FaultInjectionCustomerWorkflows`. The suite is intended to run only through the existing on-demand Cosmos live test path.

Single-write multi-region customer workflows use TestNG group `fi-sm-customer-workflows`, Maven profile `-Pfi-sm-customer-workflows`, and live matrix display name `FaultInjectionSingleMasterCustomerWorkflows`.

## Implemented Workflow Classes

| Workflow class | Customer coverage areas represented |
|---|---|
| `CustomerWorkflowRequestOptionsTest` | Dynamic request options, keyword identifiers, excluded regions, create/read/query/readMany/upsert/delete diagnostics. |
| `CustomerWorkflowDaoStyleOperationsTest` | DAO-style CRUD chain, readAll, patch, transactional batch, bulk read/patch with max micro-batch sizing, and request-level serializer propagation. |
| `CustomerWorkflowLatestCommittedTest` | Latest-committed point read, query, readMany, change feed, excluded regions, diagnostics request-option propagation, regional lease-not-found fault coverage, and direct/gateway client variants. |
| `CustomerWorkflowSessionTokenTest` | ReadMany with valid and advanced user session tokens, validating read-session-not-available behavior. |
| `CustomerWorkflowStoredProcedureTest` | Stored procedure create/read/execute with script logging and metadata fault-rule coverage. |
| `CustomerWorkflowChangeFeedProcessorTest` | Latest-version CFP start, restart, current state/lag, and read-feed fault recovery. |
| `CustomerWorkflowAvailabilityFaultMatrixTest` | Expanded multi-master direct/gateway fault matrix for read, query, readMany, create, upsert, replace, delete, and patch operations across representative 404/408/410/429/449/500/503 families. |
| `CustomerWorkflowHighE2ETimeoutTest` | Response-delay workflow with E2E timeout and availability strategy for create, read, query, readMany, upsert, batch, patch, and partition-migrating read. |
| `CustomerWorkflowPartitionLevelCircuitBreakerTest` | PCLB-oriented point read, query-plan diagnostics/query, and patch app-chain workflow under the PCLB-enabled live matrix leg. |
| `CustomerWorkflowSingleMasterAvailabilityTest` | Single-write multi-region excluded-readable-region reads, local readable-region read faults, write faults constrained to the single writable region, and representative direct/gateway read/create fault matrices. |

## Remaining Gap Summary

| Remaining area | Current status after `fi-customer-workflows` | Importance of adding more |
|---|---|---|
| Exhaustive dynamic request-option matrix | Core app-style create/read/query/readMany/upsert/delete request-option propagation is covered; the exhaustive per-option matrix remains in existing SDK primitive tests. | `nice to have` / mostly duplicate. Add only if release owners want customer-style chaining for every option combination. |
| Latest-committed RU comparison variants | Point read, query, readMany, change feed, excluded regions, diagnostics propagation, and a regional lease-not-found fault are covered; strict RU comparison checks remain. | `nice to have`. RU comparisons are service-sensitive and less valuable than the diagnostics/routing checks now covered. |
| Gateway latest-committed regression variants | Direct and gateway latest-committed workflow variants are covered by `CustomerWorkflowLatestCommittedTest`; existing gateway read-consistency tests remain the primitive anchor. | `covered enough`. No further action unless strict one-class-per-customer-file parity is required. |
| Stored procedure exact fault parity | Stored procedure create/read/execute/script-log and metadata fault-rule coverage are added; exact response-delay/read-session-not-available stored-procedure fault parity is not fully represented because fault injection has no stored-procedure-specific operation type. | `addressing significant partial gap`, but may require deeper test-infra support or a carefully scoped metadata/data-plane proxy scenario. |
| CFP full customer matrix | Latest-version CFP start, restart, current state/lag, and read-feed fault recovery are covered; full-fidelity/all-versions, side-cart, and deeper lease recovery variants remain. | `nice to have`. Current workflow covers the highest-signal CFP behavior without copying the large CFP matrix. |
| Full multi-write availability matrix | Expanded direct/gateway multi-write fault rows now cover read/query/readMany/create/upsert/replace/delete/patch across representative 404/408/410/429/449/500/503 families. The only unported portion is exact one-class-per-customer-file parity and every operation/error permutation. | `runtime-heavy duplicate`. Stop here unless parity is required over runtime. |
| Single-write direct/gateway availability matrix | Dedicated single-write multi-region live leg and representative direct/gateway read/create fault matrices are added through `fi-sm-customer-workflows`; exact one-class-per-error-file parity remains. | `runtime-heavy duplicate`. Stop here unless strict customer-suite parity is required. |
| High E2E timeout extended fault variants | Response-delay E2E timeout with availability strategy now covers create/read/query/readMany/upsert/batch/patch plus partition-migrating read; deeper customer-specific timing/RU assertions remain. | `nice to have`. The main workflow gap is covered; remaining work is runtime-sensitive strict parity. |
| PCLB exact regional circuit-breaker assertions | PCLB-oriented read/query-plan diagnostics/query/patch app-chain workflow is added; exact circuit-breaker state transitions remain in existing PCLB tests. | `nice to have` for customer parity; existing SDK PCLB tests already cover the lower-level behavior. |
| 429 and connection-delay app-shaped calls | 429-style rows are now represented in multi-write and single-write matrices; connection-delay/connect-reset style network transport variants remain in existing transport/FI tests and selected timeout workflows. | `runtime-heavy duplicate`. Add only if network-fault parity is explicitly required. |
| Conflict resolution and conflict query | Not added to the new workflow suite; existing conflict tests cover core SDK behavior. | `nice to have` / duplicate. Add only if customer multi-client ordering or diagnostics are materially different. |
| Basic multi-write behavior and feature-validation classes | Covered indirectly by CRUD/request-options/latest-committed/session-token workflows and existing multi-master tests. | `completely duplicate` for this suite unless a specific uncovered assertion is identified. |
| Custom serializer standalone tests | Request-level serializer propagation is represented in the DAO-style workflow; existing serializer tests cover normal and exception behavior. | `completely duplicate`. Keep deeper standalone serializer tests out of this workflow suite. |
| Customer wrapper defaults, caches, DAO session maps, and configuration defaults | Not ported by design because these are not SDK contracts. | `completely useless for SDK coverage` / wrapper-specific. Keep documented only. |

## Porting Rules

- Use SDK-native tests in `azure-cosmos-tests`; do not copy customer-specific package dependencies.
- Do not copy hardcoded customer endpoints, account keys, database names, or container names.
- Prefer dynamic account-region discovery over hardcoded region order.
- Replace fixed sleeps with polling or retry loops.
- Preserve customer workflow shape where it adds release signal: operation chains, contacted-region diagnostics, effective consistency, effective read-consistency strategy, retry counts, and request-option propagation.
- Mark wrapper default assertions as `wrapper-specific` unless the SDK owns the behavior.
