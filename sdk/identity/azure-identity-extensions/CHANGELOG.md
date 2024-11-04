# Release History

## 1.2.0-beta.2 (Unreleased)

### Other Changes

#### Dependency Updates

- Upgraded `mysql-connector-j` from `8.0.33` to version `9.0.0`.

## 1.1.17 (2024-06-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.12.1` to version `1.13.0`.


## 1.1.16 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.12.0` to version `1.12.1`.


## 1.1.15 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.11.4` to version `1.12.0`.


## 1.1.14 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.11.2` to version `1.11.4`.
- Upgraded `postgresql` from `42.3.8` to version `42.3.9`.


## 1.1.13 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.11.1` to version `1.11.2`.


## 1.1.12 (2024-02-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.11.0` to version `1.11.1`.

## 1.1.11 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.11.0` to version `1.11.1`.

## 1.1.10 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.10.4` to version `1.11.0`.

## 1.1.9 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.10.1` to version `1.10.4`.

## 1.1.8 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.10.0` to version `1.10.1`.

## 1.1.7 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.9.2` to version `1.10.0`.

## 1.1.6 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.9.1` to version `1.9.2`.

## 1.1.5 (2023-06-21)

### Other Changes

#### Dependency Updates

- Upgraded `mysql-connector-j` from `8.0.32` to version `8.0.33`.
- Upgraded `azure-identity` from `1.9.0` to version `1.9.1`.

## 1.1.4 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.8.2` to version `1.9.0`.

## 1.1.3 (2023-04-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.8.1` to version `1.8.2`.

## 1.1.2 (2023-03-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-identity` from `1.8.0` to version `1.8.1`.

## 1.1.1 (2023-02-13)

### Dependency Updates

- Upgrade Azure Identity to 1.8.0.

### Others

- Improve the performance of DefaultTokenCredentialProvider's `get()` method.

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
