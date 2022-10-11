# Azure identity JDBC plugin library for Java

This package contains a template framework to get a token from Azure AD.
For now, it is used by `azure-identity-providers-jdbc-mysql` and `azure-identity-providers-jdbc-postgresql` as the common framework
to get a token as the password for connection.

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/).
- [Java Development Kit (JDK)][jdk] with version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).

## Key concepts

Azure Identity Providers Core contains a common template framework for users to get a token from Azure AD and
use the token as a password. For example, to connect Azure hosted MySQL, get a token from Azure AD and use the
token as a password to connect with MySQL.

## Examples
For documentation on how to use this package, please refer to [Quickstart:Use Java and JDBC with Azure Database for MySQL](https://aka.ms/quickstart-jdbc-mysql) and .


## Troubleshooting
If you encounter any bugs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues).

## Next steps
Other useful packages are:
* [azure-identity-providers-jdbc-mysql][azure-identity-providers-jdbc-mysql]:  Azure identity JDBC MySQL plugin library for Java.
* [azure-identity-providers-jdbc-postgresql][azure-identity-providers-jdbc-postgresql]:  Azure identity JDBC PostgreSQL plugin library for Java.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

<!-- LINKS -->
[azure-identity-providers-jdbc-mysql]: https://github.com/Azure/azure-sdk-for-java/blob/31c42eac4fa6e8a3bb00c2e01f80e9a8bacd1d78/sdk/jdbc/azure-identity-providers-jdbc-mysql
[azure-identity-providers-jdbc-postgresql]: https://github.com/Azure/azure-sdk-for-java/blob/31c42eac4fa6e8a3bb00c2e01f80e9a8bacd1d78/sdk/jdbc/azure-identity-providers-jdbc-postgresql
