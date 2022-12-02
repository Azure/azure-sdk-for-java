<!--
// cSpell:ignore JDBC 
-->

# Release History

## 1.0.0 (2022-12-07)

This is the 1.0.0 GA version of Azure identity extensions library for Java.

### Breaking Changes

- Move `azure-identity-providers-core, `azure-identity-providers-jdbc-mysql`, `azure-identity-providers-jdbc-postgresql` into one module [#32282](https://github.com/Azure/azure-sdk-for-java/pull/32282).
- Rename `com.azure.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin` to `com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin`.
- Rename `com.azure.identity.providers.postgresql.AzureIdentityPostgresqlAuthenticationPlugin` to `com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin`.

### Bugs Fixed

- Fix passwordless feature doesn't work in AZURE_GERMANY, AZURE_CHINA, and AZURE_GOVERNMENT clouds [#31600](https://github.com/Azure/azure-sdk-for-java/pull/31600).

## 1.0.0-beta.1 (2022-09-23)

### Features Added
- Azure identity JDBC plugin library for Java. This package contains a template framework to get a token as password.
