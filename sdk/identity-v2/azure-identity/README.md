# Azure Identity client library for Java

The Azure Identity V2 library provides [Microsoft Entra ID](https://learn.microsoft.com/entra/fundamentals/whatis) token-based authentication support across the Azure SDK.

## Getting started

### Include the package

#### Include the BOM file

#### Include direct dependency

To take dependency on a particular version of the library that isn't present in the BOM, add the direct dependency to your project as follows:

[//]: # ({x-version-update-start;com.azure:azure-identity-v2;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity-v2</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

### Authenticate the client

## Key concepts

## Examples

### Authenticate GitHub Actions workflows

`com.azure.v2.identity.GitHubActionsCredential` authenticates through GitHub Actions OIDC federation.
Configure an Entra application's federated identity credential to trust your GitHub workflow's issuer, subject,
and audience, following [GitHub's Azure OIDC setup guide][github_oidc].

The workflow needs `permissions: id-token: write`. Set `AZURE_TENANT_ID` and `AZURE_CLIENT_ID` in the workflow
environment. GitHub supplies `ACTIONS_ID_TOKEN_REQUEST_URL` and `ACTIONS_ID_TOKEN_REQUEST_TOKEN`; do not
configure those values as repository secrets or print them. No client secret, certificate, or `azure/login`
step is required.

```java readme-sample-v2-github-actions-credential
TokenCredential gitHubCredential = new GitHubActionsCredentialBuilder().build();
```

The builder also accepts explicit IDs, which override the environment variables:

```java readme-sample-v2-github-actions-credential-explicit
TokenCredential explicitCredential
    = new GitHubActionsCredentialBuilder().tenantId("<tenant-id>").clientId("<client-id>").build();
```

The v2 credential uses `com.azure.v2.core.credentials.TokenCredential` and its synchronous `getToken` API.
It is not included in `DefaultAzureCredential`.

An explicit `authorityHost` takes precedence over `AZURE_AUTHORITY_HOST`, followed by Azure Public.
The assertion audience is derived from the effective authority; configure the same audience on the federated
identity credential:

| Authority host | Audience |
| --- | --- |
| `https://login.microsoftonline.com/` | `api://AzureADTokenExchange` |
| `https://login.chinacloudapi.cn/` | `api://AzureADTokenExchangeChina` |
| `https://login.microsoftonline.us/` | `api://AzureADTokenExchangeUSGov` |
| `https://login.sovcloud-identity.fr/` | `api://AzureADTokenExchangeFrance` |
| `https://login.sovcloud-identity.de/` | `api://AzureADTokenExchangeGermany` |
| `https://login.sovcloud-identity.sg/` | `api://AzureADTokenExchangeGovSG` |

### Run the opt-in end-to-end test

The [Identity GitHub Actions credential workflow][github_oidc_workflow] builds both Identity
packages from source and requests a public-cloud ARM access token from each, without calling Azure resources or
logging token values. It runs on pushes to `vcolin7-github-actions-credential` and also supports manual dispatch.

Configure a test application's federated identity credential for your repository's actual issuer and subject
(including immutable IDs when applicable), the ref to be tested, and audience `api://AzureADTokenExchange`.
Set repository variables `AZURE_IDENTITY_GITHUB_ACTIONS_TENANT_ID` and `AZURE_IDENTITY_GITHUB_ACTIONS_CLIENT_ID`,
then push the workflow and SDK changes to `vcolin7-github-actions-credential` in your fork.
For manual runs, the workflow must first be present on the repository's default branch; you can then dispatch
it on a trusted ref. The workflow does not create cloud resources or repository settings.

The dedicated live test is skipped unless `RUN_GITHUB_ACTIONS_CREDENTIAL_LIVE_TESTS=true`, `AZURE_TEST_MODE=LIVE`,
and `GITHUB_ACTIONS=true`. The workflow supplies those settings.

## Troubleshooting

`GitHubActionsCredentialBuilder.build()` raises `CredentialUnavailableException` when required configuration is
missing, the runner request URL is invalid, or the authority has no supported audience mapping. Supply the Azure IDs
through the builder or environment and ensure the job has `permissions: id-token: write`. The runner request URL
must be an absolute HTTPS URL.

GitHub OIDC HTTP or response-parsing failures raise `CredentialAuthenticationException`. For HTTP 401/403,
check the workflow permissions and run the SDK in the step where GitHub provides its OIDC variables.
For Entra federation errors, verify that the issuer, subject, and audience match the application's federated
identity credential. Error messages do not include the assertion or raw OIDC response body.

## Next steps


## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information, see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[github_oidc]: https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-azure
[github_oidc_workflow]: https://github.com/Azure/azure-sdk-for-java/blob/main/.github/workflows/identity-github-actions-credential.yml
