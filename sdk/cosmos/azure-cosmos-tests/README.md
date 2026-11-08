# Azure Cosmos Tests client library for Java
Library containing tests for azure-cosmos and azure-cosmos-test libraries. Only used internally  as home for tests to allow using failure injection even in the unit- and integration tests of azure-cosmos.

## Getting started
n/a

## Key concepts
n/a

## Examples
n/a

### Gateway V2 live split regression (opt-in)

`ThinClientSessionTokenSplitE2ETest` creates an owned container at **400 RU/s manual
throughput**, then raises it to **8,000 RU/s**. It waits up to ten minutes for actual
parent-to-child partition metadata; a completed throughput update alone is not
accepted as proof of a split. If no split is observed, the test fails and cleans up.
It does not inject synthetic ranges or increase throughput beyond 8,000 RU/s.

Use a global `ACCOUNT_HOST` for a Gateway V2-enabled account and configure credentials
through the existing `cosmos-v4.properties` mechanism. A regional endpoint is used by
the observer so its metadata polling cannot refresh the stale reader's endpoint-shared
routing cache. Only the test's own database/container is changed or deleted.

From the repository root in PowerShell:

```powershell
mvn -f sdk\cosmos\pom.xml -pl azure-cosmos-tests -am test-compile failsafe:integration-test failsafe:verify `
  -Pthinclient '-Dtest.groups=thinclientSplit' '-Dit.test=ThinClientSessionTokenSplitE2ETest' `
  '-Dfailsafe.failIfNoSpecifiedTests=false' '-Dgpg.skip=true'
```

The test calls `readItem` with parent progress/current child metadata, child
progress/current child metadata, and child progress/stale parent metadata.
Observation-only HTTP wrappers record the actual session headers (including decoded
Gateway V2 bytes) and forward every request to the real service. An advanced-child
progress control must fail with 404/1002 instead of succeeding after progress is lost.
When the initial container has a single range, the parent compound token concatenates
two genuine responses from that parent; no partition ID or progress is fabricated.

This group is intentionally excluded from the ordinary Gateway V2 suites. Provisioned
throughput incurs charges; let the unconditional cleanup finish when debugging.

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
