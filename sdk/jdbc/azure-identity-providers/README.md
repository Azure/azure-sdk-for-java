- [Azure identity authentication providers plugin library for Java](#azure-identity-authentication-providers-plugin-library-for-java)
  - [Getting started](#getting-started)
    - [Prerequisites](#prerequisites)
  - [Key concepts](#key-concepts)
  - [Examples](#examples)
  - [Troubleshooting](#troubleshooting)
  - [Next steps](#next-steps)
  - [Contributing](#contributing)
    
# Azure identity authentication providers plugin library for Java

This package contains authentication providers to get a token from Azure Active Directory (Azure AD) for Azure services, like Azure Database for MySQL.

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/).
- [Java Development Kit (JDK)][jdk] with version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).

## Key concepts

Azure Identity Providers contains a common template framework for users to get a token from Azure AD and
use the token as a password. For example, to connect Azure hosted MySQL, get a token from Azure AD and use the
token as a password to connect with MySQL.

## Examples
For documentation on how to use this package, please refer to [Quickstart:Use Java and JDBC with Azure Database for MySQL](https://aka.ms/passwordless/quickstart/mysql) and [Quickstart:Use Java and JDBC with Azure Database for PostgreSQL](https://aka.ms/passwordless/quickstart/postgresql).


## Troubleshooting
If you encounter any bugs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues).

## Next steps
Other useful docs are:
* [With Azure Database for MySQL][azure-identity-providers-jdbc-mysql]:  Azure identity providers for Azure Database for MySQL.
* [With Azure Database for PostgreSQL][azure-identity-providers-jdbc-postgresql]:  Azure identity providers for Azure Database for PostgreSQL.


## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

<!-- LINKS -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure-identity-providers-jdbc-mysql]: https://github.com/Azure/azure-sdk-for-java/blob/9ad980f27a4ff29b9c5e694054e545560918ea0b/sdk/jdbc/azure-identity-providers/Azure-Database-for-MySQL-README.md
[azure-identity-providers-jdbc-postgresql]: https://github.com/Azure/azure-sdk-for-java/blob/9ad980f27a4ff29b9c5e694054e545560918ea0b/sdk/jdbc/azure-identity-providers/Azure-Database-for-PostgreSQL-README.md