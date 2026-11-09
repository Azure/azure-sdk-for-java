# Azure Cosmos Tests client library for Java
Library containing tests for azure-cosmos and azure-cosmos-test libraries. Only used internally  as home for tests to allow using failure injection even in the unit- and integration tests of azure-cosmos.

## Getting started
n/a

## Key concepts
n/a

## Examples
n/a

### Gateway V2 live split regression (endpoint-probe CI)

`ThinClientSessionTokenSplitE2ETest` runs in the `thinclientEndpointProbe` TestNG
group, selected by the `thinclient-endpoint-probe` Maven profile and the existing
`Cosmos_Live_Test_ThinClient_EndpointProbe` CI stage. That stage supplies the account,
allows 210 minutes per job, and runs post-job resource cleanup. The profile enables
HTTP/2 but leaves `COSMOS.THINCLIENT_ENABLED` unset so the connectivity probe decides
whether Gateway V2 is available.

`ThinClientSessionTokenSplitE2ETest` creates an owned container at **400 RU/s manual
throughput**, then raises it to **10,100 RU/s**. It uses the same blocking split-wait
pattern as the vector-search split test: a **15-minute** deadline, **10 seconds**
between polls, and `SplitTimeoutException` if the partition count does not increase.
Polls run sequentially, so a slow metadata refresh cannot accumulate timer ticks.
Each poll logs the throughput replacement's pending status, current RU/s, and observed ranges.
The overall test timeout remains **20 minutes**. A completed throughput update alone
is not accepted as proof of a split. If no split is observed, the test fails and cleans up.
It does not inject synthetic ranges or increase throughput beyond 10,100 RU/s.

Use a global `ACCOUNT_HOST` for an account with **at least two Gateway V2 regions**
and configure credentials through the existing `cosmos-v4.properties` mechanism.
The global endpoint and two regional endpoints isolate the positive stale reader,
the split observer, and a separate stale reader for the negative control. This prevents
either metadata polling or the positive refreshed read from refreshing the negative
control's endpoint-shared routing cache. Only the test's own database/container is
changed or deleted.

To run just this test locally, from the repository root in PowerShell:

```powershell
mvn -f sdk\cosmos\pom.xml -pl azure-cosmos-tests -am test-compile failsafe:integration-test failsafe:verify `
  -Pthinclient-endpoint-probe '-Dit.test=ThinClientSessionTokenSplitE2ETest' `
  '-Dfailsafe.failIfNoSpecifiedTests=false' '-Dgpg.skip=true'
```

The test calls `readItem` with parent progress/current child metadata, child
progress/current child metadata, and child progress/stale parent metadata. The
stale reader refreshes its routing map before sending the scoped token through V2.
Observation-only HTTP wrappers record the actual session headers (including decoded
Gateway V2 bytes) and forward every request to the real service. A mixed lower-parent
and advanced-child progress control must refresh the stale map, stay on V2, and fail
with 404/1002 instead of succeeding after the child's progress is lost.
When the initial container has a single range, the parent compound token concatenates
two genuine responses from that parent; no partition ID or progress is fabricated.

No custom group override is needed; the endpoint-probe suite includes this test
automatically. It is not part of the force-enabled `thinclient` group. Provisioned
throughput incurs charges; let the unconditional cleanup finish when debugging.

Explicit compound tokens use the existing target/parent token selection. An unresolved
target, no matching progress, or an unknown token range triggers one routing-map refresh.
The unknown-range check also applies when a parent token matches: V2 can route by EPK to
a split child without rejecting the client's stale parent map.

If refreshed metadata still cannot establish the scope, the request stays on V2 but
omits the caller's token; ambient cached progress is not substituted. The WARN message
and `sessionTokenEvaluationResults` diagnostics explain that the supplied session progress
is **not enforced**, include a safe reason and request identifiers, and advise checking
the token/container association and capturing diagnostics for recurring cases. Full
session tokens are not included in this warning. Do not blindly retry non-idempotent
writes in response to it. Metadata and token-parsing failures remain errors rather than
being converted to this weaker-consistency outcome. A fixed physical range that is still
missing after refresh retains normal range-gone handling; omitting a token does not
make an invalid physical route usable.

## Troubleshooting
n/a

## Next steps
n/a

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[cosmos_introduction]: https://learn.microsoft.com/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.z19.web.core.windows.net/java/azure-cosmos/latest/index.html
[cosmos_docs]: https://learn.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[maven]: https://maven.apache.org/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[troubleshooting]: https://learn.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://learn.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[quickstart]: https://learn.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync
