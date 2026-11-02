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

There are three logical connection-mode cases, not a type-by-mode matrix. The
factory selects one client per existing CI lane, in separate JVMs:

| Suite / lane | Clients exercised |
| --- | --- |
| `query`, HTTP/2 disabled | Direct TCP |
| HTTP/2 `query`, `COSMOS.THINCLIENT_ENABLED=false` | Compute |
| `thinclient` | ThinClient |

The `thinclientEndpointProbe` suite additionally runs the same ThinClient case
using probe-selected routing rather than explicit opt-in.

Diagnostics must show actual document-query requests on the expected path.
Compute queries must negotiate HTTP/2 and must not target the thin-client proxy;
ThinClient queries must use that proxy, including in the endpoint-probe lane.
Do not switch the global thin-client flag between live clients: routing reads it
lazily on each request.

Configure `ACCOUNT_HOST` and `ACCOUNT_KEY` for an existing account supporting these
vector types. Compute runs require an HTTP/2-capable Compute Gateway endpoint;
thin-client runs require the thin-client endpoint.

After building and installing the Cosmos modules with the same JDK, run from the
repository root:

```shell
mvn -pl com.azure:azure-cosmos-tests verify -Pquery \
  -DCOSMOS.HTTP2_ENABLED=false -DCOSMOS.THINCLIENT_ENABLED=false \
  -Dit.test=VectorSearchQueryTest \
  -Dfailsafe.failIfNoTests=true \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false

mvn -pl com.azure:azure-cosmos-tests verify -Pquery \
  -DCOSMOS.HTTP2_ENABLED=true -DCOSMOS.THINCLIENT_ENABLED=false \
  -Dit.test=VectorSearchQueryTest \
  -Dfailsafe.failIfNoTests=true \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false

mvn -pl com.azure:azure-cosmos-tests verify -Pthinclient \
  -Dit.test=VectorSearchQueryTest \
  -Dfailsafe.failIfNoTests=true \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false
```

For probe-selected ThinClient coverage, use `-Pthinclient-endpoint-probe` instead
and leave `COSMOS.THINCLIENT_ENABLED` unset.

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
