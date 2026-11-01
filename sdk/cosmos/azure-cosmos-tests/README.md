# Azure Cosmos Tests client library for Java
Library containing tests for azure-cosmos and azure-cosmos-test libraries. Only used internally  as home for tests to allow using failure injection even in the unit- and integration tests of azure-cosmos.

## Getting started

### Vector embedding end-to-end coverage

`VectorEmbeddingQueryTest` runs in the live `query` profile. Configure `ACCOUNT_HOST`
and `ACCOUNT_KEY` for an existing test account supporting vector search and the
`float16`, `float32`, `int8`, and `uint8` embedding types. It creates and deletes its
own database, verifies a four-path embedding policy, and checks deterministic
nearest-neighbor results in both query directions. Its small fixture exercises
the `quantizedFlat` full-scan fallback, not approximate-index recall.

After building and installing the Cosmos modules with the same JDK, run from the
repository root:

```shell
mvn -pl com.azure:azure-cosmos-tests verify -Pquery \
  -Dit.test=VectorEmbeddingQueryTest -Dfailsafe.failIfNoTests=true \
  -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false
```

The `allVectorDataTypesRoundTrip` test belongs to the `unit` group and does not
require an account.

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
