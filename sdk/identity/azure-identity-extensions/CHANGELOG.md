# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2023-02-06)

### Other Changes

#### Dependency Updates

- Upgraded `mysql-connector-j` from version `8.0.31` to `8.0.32`.

## 1.1.0 (2023-01-11)

This is the 1.1.0 GA version of Azure identity extensions library for Java.

### Dependency Updates

- Upgrade Azure Identity to 1.7.3.

## 1.0.0 (2022-12-05)

This is the 1.0.0 GA version of Azure identity extensions library for Java.

### Breaking Changes

- Move `azure-identity-providers-core`, `azure-identity-providers-jdbc-mysql`, `azure-identity-providers-jdbc-postgresql` into one module: `azure-identity-extensions` [#32282](https://github.com/Azure/azure-sdk-for-java/pull/32282), [#32370](https://github.com/Azure/azure-sdk-for-java/pull/32370).
  - Please note that the MySQL or PostgreSQL driver dependency needs to be explicitly added to your project now. 
- Rename `com.azure.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin` to `com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin` [#32401](https://github.com/Azure/azure-sdk-for-java/pull/32401).
- Rename `com.azure.identity.providers.postgresql.AzureIdentityPostgresqlAuthenticationPlugin` to `com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin` [#32401](https://github.com/Azure/azure-sdk-for-java/pull/32401).

### Bugs Fixed

- Fix passwordless feature doesn't work in AZURE_GERMANY, AZURE_CHINA, and AZURE_GOVERNMENT clouds [#31600](https://github.com/Azure/azure-sdk-for-java/pull/31600).

## 1.0.0-beta.1 (2022-09-23)

### Features Added
- Azure identity JDBC plugin library for Java. This package contains a template framework to get a token as password.
