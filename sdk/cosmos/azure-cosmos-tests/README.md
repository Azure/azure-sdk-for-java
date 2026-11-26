# Azure Cosmos Tests client library for Java
Library containing tests for azure-cosmos and azure-cosmos-test libraries. Only used internally  as home for tests to allow using failure injection even in the unit- and integration tests of azure-cosmos.

## Getting started

### Vector embedding end-to-end coverage

`VectorSearchQueryTest.testVectorSearchAcrossEmbeddingTypes` uses one fresh
container with four embedding paths: `/float16`, `/float32`, `/int8`, and `/uint8`.
Every document contains all four vectors. One fixed SQL query projects four
`VectorDistance` scores and orders by the `float16` distance. It uses Euclidean
distance and the same query vector for all four paths.

The test verifies the persisted policies, document ordering, and all four numeric
scores for every result. The fixture includes fractional floats, negative `int8`
values, and `uint8` values above 127. The `float16` index uses the `quantizedFlat`
full-scan fallback; the other paths use `flat` indexes.

Setup uses a separate Direct client. The first document operation from the client
under test is this query, exercising deserialization of metadata containing all
four types. Teardown deletes the database and its container.

Only this test bypasses the shared query-based container-readiness probe and its
feed-range warm-up. That probe would encounter the `float16` QueryPlan service bug
before the query under test and report a readiness timeout instead. Container
creation and policy read-back retain the control-plane retry helper; no fixed
sleep is added.

This test belongs only to the `service-regression` TestNG group and Maven profile.
Routine `query`, `thinclient`, and `thinclientEndpointProbe` suites do not select
it. Its class setup owns the database, so it does not need the shared-container
initialization used by the routine live suites. The dedicated suite retains the
resource janitor and leak-detection listeners.

The [dedicated pipeline](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/service-regression.yml)
defaults to stage and runs three jobs, not a type-by-mode matrix:

| Connection mode | HTTP/2 | ThinClient enabled |
| --- | --- | --- |
| Direct | `false` | `false` |
| Compute | `true` | `false` |
| ThinClient | `true` | `true` |

Live execution is manual/on-demand only (`TestEnvironment=live`), outside routine
live CI. Failures still fail that dedicated run; neither stage nor live suppresses
failures. Stage Signoff must queue the stage run and require its success before
promotion. Merely registering the YAML does not create that dependency. See the
[pipeline setup instructions](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/pipeline/README.md#service-regression-pipeline).

Diagnostics must show actual document-query requests on the expected path.
Compute queries must negotiate HTTP/2 and must not target the thin-client proxy;
ThinClient queries must use that proxy. Missing advertised proxy endpoints or
disabled proxy query-plan routing fail setup rather than allowing gateway fallback
to count as coverage. The pipeline also rejects missing, skipped, or empty results.
Do not switch the global thin-client flag between live clients: routing reads it
lazily on each request.

Configure `ACCOUNT_HOST` and `ACCOUNT_KEY` for an existing account supporting these
vector types. Compute runs require an HTTP/2-capable Compute Gateway endpoint;
thin-client runs require the thin-client endpoint.

After building and installing the Cosmos modules with the same JDK, run from the
repository root:

```shell
mvn -pl com.azure:azure-cosmos-tests verify -Pservice-regression \
  -DCOSMOS.HTTP2_ENABLED=false -DCOSMOS.THINCLIENT_ENABLED=false \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false

mvn -pl com.azure:azure-cosmos-tests verify -Pservice-regression \
  -DCOSMOS.HTTP2_ENABLED=true -DCOSMOS.THINCLIENT_ENABLED=false \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false

mvn -pl com.azure:azure-cosmos-tests verify -Pservice-regression \
  -DCOSMOS.HTTP2_ENABLED=true -DCOSMOS.THINCLIENT_ENABLED=true \
  -DCOSMOS.THINCLIENT_QUERY_PLAN_ENABLED=true \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false
```

## Key concepts
n/a

## Examples
n/a

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
