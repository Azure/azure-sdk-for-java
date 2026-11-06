# Azure Cosmos Tests client library for Java
Library containing tests for azure-cosmos and azure-cosmos-test libraries. Only used internally  as home for tests to allow using failure injection even in the unit- and integration tests of azure-cosmos.

## Getting started
n/a

## Key concepts
n/a

## Examples

### Direct-mode cancellation recovery

`RntbdRequestCancellationTests` covers pending-request cancellation bookkeeping,
the three-cancellation/ten-second-no-read health gates, intervening read resets,
per-channel isolation, and the distinction between transit expiration and cancellation.
It uses an embedded channel without an account or sleeps.

After building the Cosmos modules, run the focused unit tests from the repository root:

```shell
mvn -f sdk/cosmos/azure-cosmos-tests/pom.xml verify -Punit \
  -Dit.test=RntbdRequestCancellationTests,RntbdClientChannelHealthCheckerTests \
  -Dfailsafe.failIfNoTests=true -Dgpg.skip=true
```

The `FaultInjectionServerErrorRuleOnDirectTests#cancellationProneReplicaRecoversAfterEndToEndTimeouts`
live test uses `LATEST_COMMITTED` reads and targets one secondary replica with native
`RESPONSE_DELAY` fault injection and `suppressServiceRequests(true)`. The five-second
end-to-end timeout precedes the ten-second network timeout so the test observes
cancellation rather than ordinary transit expiration. One connection per endpoint
keeps the cancellation count on one channel; detector thresholds are unchanged.

The test correlates full CosmosDiagnostics with the cancellation-prone health WARN,
requiring three cancellations on the same channel since the same last read, zero
retries, a responding companion replica, and successful reads on other replicas
while the fault remains active. Requests are bounded; the rule, appender, item, and
client are cleaned up. It logs each operation's diagnostics for failure investigation.

Run against a non-production account using the usual `ACCOUNT_HOST`, `ACCOUNT_KEY`,
and `ACCOUNT_CONSISTENCY` configuration:

```shell
mvn -f sdk/cosmos/azure-cosmos-tests/pom.xml verify -Pmulti-region \
  -Dit.test=FaultInjectionServerErrorRuleOnDirectTests#cancellationProneReplicaRecoversAfterEndToEndTimeouts \
  -Dfailsafe.failIfNoTests=true -Dgpg.skip=true
```

The live suite provisions its normal shared test fixtures and consumes RUs. Do not
run it against customer resources. A successful simulation proves the recovery
mechanism, not the cause of an unrelated incident or server-side replica removal.

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
