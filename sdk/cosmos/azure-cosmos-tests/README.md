# Azure Cosmos Tests client library for Java
Library containing tests for azure-cosmos and azure-cosmos-test libraries. Only used internally  as home for tests to allow using failure injection even in the unit- and integration tests of azure-cosmos.

## Getting started
n/a

## Key concepts

### Gateway V2 compound session tokens

`ThinClientStoreModel` prepares compound session tokens after the normal Gateway
session eligibility checks and before serialization. It resolves the partition from
the collection routing map, reusing an existing range when it belongs to that map.
The dedicated `SessionTokenHelper.scopeCompoundSessionToken` method merges progress
for the target partition and its parents.

The prepared token is stored in the request's existing native session-token context.
Serialization uses a request copy carrying only that scoped token, or no token for an
intentional omission. The caller's compound header is not mutated: no separate original
token field or header restoration is needed, and a retry or clone can select against its
current topology. An omitted wire token cannot activate ambient session resolution.
Transport selection, document-client preparation, and partition failover helpers remain unchanged.

If the target cannot be resolved, no token segment matches, or the topology does not account
for every supplied range ID, the SDK refreshes once using the rejected routing map. It then
resolves the target again and scopes from the original token. Unknown child IDs trigger this
refresh even if a lower parent token matches the stale topology.

If refreshed metadata still cannot establish a safe scope, the request proceeds without a
session token and caller progress is not enforced. The existing
`SessionTokenHelper` diagnostics and logging report the unmatched progress; warning logging
follows the helper's existing configuration. Metadata, parsing, and merge errors propagate;
there is no refresh loop or token-driven transport fallback. Missing pinned physical ranges
retain range-gone handling. Malformed tokens still fail.
The regular and real-split live regression classes run together under the
`thinclient-endpoint-probe` Maven profile. The split regression also verifies that unattainable
child progress still fails with session-not-available after refreshing a genuinely stale map.

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
