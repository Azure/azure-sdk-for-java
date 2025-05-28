# Azure Key Vault client libraries for Java

[![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/598?branchName=main)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=598) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html) [![Dependencies](https://img.shields.io/badge/dependencies-analyzed-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/dependencies.html) [![SpotBugs](https://img.shields.io/badge/SpotBugs-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/spotbugsXml.html) [![CheckStyle](https://img.shields.io/badge/CheckStyle-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/checkstyle-aggregate.html)

Azure Key Vault is a cloud service that provides secure storage of certificates, cryptographic keys and secrets used by
cloud applications and services.

Azure Key Vault Managed HSM is a fully-managed, highly-available, single-tenant, standards-compliant cloud service that
enables you to safeguard cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.

For more information refer to [About Azure Key Vault][azure_keyvault] and
[What is Azure Key Vault Managed HSM?][azure_keyvault_mhsm].

Documentation for this SDK can be found at [Azure Key Vault Java Documentation][azure_keyvault_java].

## Getting started

To get started with a specific library, see the **README.md** file located in the library's project folder. You can find
service libraries in the `/sdk/keyvault-v2/azure-security-keyvault-<subcomponent>` directory.

- [Azure Key Vault Keys][azure_keyvault_keys_library] is a cloud service that enables you to safeguard and manage
  cryptographic keys.
- [Azure Key Vault Certificates][azure_keyvault_certificates_library] is a cloud service that allows you to securely
  manage and tightly control your certificates.
- [Azure Key Vault Secrets][azure_keyvault_secrets_library] is a cloud service that provides management and secure
  storage for secrets, such as passwords and database connection strings.
- The [Azure Key Vault Administration][azure_keyvault_administration_library] library clients support administrative
  tasks such as full backup/restore and key-level role-based access control (RBAC) for Azure Key Vault Managed HSM.

<!-- LINKS -->
[azure_keyvault]: https://learn.microsoft.com/azure/key-vault/general/overview
[azure_keyvault_java]: https://learn.microsoft.com/java/api/overview/azure/keyvault
[azure_keyvault_mhsm]: https://learn.microsoft.com/azure/key-vault/managed-hsm/overview
[azure_keyvault_administration_library]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-administration/README.md
[azure_keyvault_certificates_library]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-certificates/README.md
[azure_keyvault_keys_library]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-keys/README.md
[azure_keyvault_secrets_library]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/README.md
