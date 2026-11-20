# Azure App Configuration integration tests

This test module exercises the current App Configuration starter through
`spring.config.import=azureAppConfiguration`. Client customizers are registered
before ConfigData loading using a `BootstrapRegistryInitializer`, not the legacy
Spring Cloud bootstrap context.

## Credential-free checks

From this directory, run:

```powershell
mvn -B -ntp test
```

Surefire runs the `*Test` classes by default. These verify bootstrap registration
and start a real Spring application using the ConfigData resolver, Azure SDK
clients, configuration binding, and feature evaluation. Only the HTTP transport
is substituted with synthetic responses; no Azure credentials or resources are
needed. They also verify that an invalid startup timeout fails before connecting.

Pagination coverage returns 100 settings on the first page and 1 on the second,
using the service's `Link` header and `@nextLink` body field. It checks that the
library follows the continuation, makes exactly two requests, and binds every
expected key/value pair.

Dependencies must be built from the current source or otherwise available in the
local Maven repository. From this directory, run against source dependencies:

```powershell
mvn -B -ntp -f ..\pom.xml -pl spring-cloud-azure-integration-test-appconfiguration-config -am test "-Dtest=ConfigDataIntegrationTest,TestBootstrapRegistryInitializerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

## Live Azure tests

Failsafe runs `*IT` classes only when `skipSpringITs=false`. Its `verify` goal
fails the build on integration-test failures; enabled runs with no discovered
tests also fail.

Prerequisites:

- A JDK supported by the current Spring Boot baseline and Maven.
- An existing App Configuration store with the unlabeled fixtures below and all
  key/label/value fixtures from `variables.selectionSettings` in
  [test-resources.json](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/spring-cloud-azure-integration-test-appconfiguration-config/test-resources.json).
- An existing Key Vault secret readable by the test identity.
- App Configuration Data Reader access (or Data Owner) and Key Vault secret-read
  access for that identity.
- `DefaultAzureCredential` authentication. Locally, sign in with `az login`;
  the credential chain can use `AzureCliCredential`. CI can use its configured
  environment or workload credentials. Set `AZURE_AUTHORITY_HOST` when targeting
  a non-public Azure cloud.

| Key | Value | Content type |
|---|---|---|
| `/application/config.message` | `Test` | Plain text |
| `/application/config.secret` | `{"uri":"https://<vault>/secrets/<secret>"}` | `application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8` |
| `.appconfig.featureflag/Alpha` | `{"id":"Alpha","enabled":true,"conditions":{"client_filters":[]}}` | `application/vnd.microsoft.appconfig.ff+json;charset=utf-8` |
| `/application/pagination.entries.key000` through `key100` (101 keys) | `value000` through `value100`, respectively | `text/plain` |

The secret can have any value: the live test compares the bound value with a
direct SDK read without including either value in assertion failures.
The [test resource template](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/spring-cloud-azure-integration-test-appconfiguration-config/test-resources.json)
provisions these fixtures for CI. The test code itself only reads Azure data; it
does not provision resources or modify configuration, flags, or secrets.

The template's `paginationSettings` resource copy loop creates the 101 pagination
fixtures. `PaginationIT` selects only that prefix, records the actual SDK pipeline
responses during ConfigData loading, and asserts page sizes of **100 and 1**, a
continuation on the first page only, and the complete bound map. The page size of
100 is treated as an App Configuration service contract. It does not infer
pagination from a separate SDK query.

From this directory:

```powershell
$env:STORE_NAME = 'https://<store>.azconfig.io'
mvn -B -ntp verify "-DskipSpringITs=false"
```

To run live tests against current source dependencies without packaging upstream
modules:

```powershell
mvn -B -ntp -f ..\pom.xml -pl spring-cloud-azure-integration-test-appconfiguration-config -am test-compile failsafe:integration-test failsafe:verify "-Dit.test=LoadConfigsIT,PaginationIT" "-Dfailsafe.failIfNoSpecifiedTests=false" "-DskipSpringITs=false"
```

Reports are written to `target/surefire-reports` and `target/failsafe-reports`.
Skipped live tests are not evidence of live compatibility. The Spring integration
pipeline explicitly enables live tests and supplies provisioned store settings.

## Troubleshooting

- Missing `STORE_NAME` or fixtures: configure the endpoint and seed only a store
  intended for integration testing with the keys above.
- Authentication or authorization failures: check the selected identity, cloud,
  and data-plane permissions for both App Configuration and Key Vault.
- Do not enable HTTP body logging for live runs; it may expose secret values.
