<!--
// cSpell:ignore JDBC 
-->

# Release History

## 1.0.0 (2022-11-11)

This is the 1.0.0 GA version of Azure identity JDBC MySQL plugin library for Java.

### Breaking Changes

Upgrade mysql from mysql:mysql-connector-java:8.0.30 to com.mysql:mysql-connector-j:8.0.31 [#32094](https://github.com/Azure/azure-sdk-for-java/pull/32094).

### Bugs Fixed

Fix passwordless feature doesn't work in AZURE_GERMANY,AZURE_CHINA,AZURE_GOVERNMENT cloud [#31600](https://github.com/Azure/azure-sdk-for-java/pull/31600).


## 1.0.0-beta.1 (2022-09-23)

### Features Added
- Azure identity JDBC MySQL plugin library for Java. This package contains authentication plugin for Azure hosted MySQL services. Use this plugin to do authentication with Azure AD. For documentation on how to use this package, please refer to [Quickstart:Use Java and JDBC with Azure Database for MySQL](https://aka.ms/quickstart-jdbc-mysql).
