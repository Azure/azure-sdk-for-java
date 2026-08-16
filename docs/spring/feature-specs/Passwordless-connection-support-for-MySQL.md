## Design for Managed Identity OAuth support of Azure Database for MySQL
Managed Identity OAuth support of Azure Database for MySQL is aimed to enable credential-free JDBC connection to Azure Database for MySQL with Managed Identities.

### Expected feature usage
With the Managed Identity OAuth functionality, taking user-assigned managed identities as example, users can follow below steps to establish JDBC connection with Azure Database for MySQL without configuring credentials in applications.

1. Create the Managed Identity and configure it to the hosting service that an application runs on.
2. Configure the Azure Database for MySQL with [Azure AD integration](https://docs.microsoft.com/azure/mysql/howto-configure-sign-in-azure-ad-authentication)
3. Create a MySQL user for the created Managed Identity and grant necessary privileges.
4. Configure a JDBC url with the MySQL username to establish connection with Azure Database for MySQL.

### JDBC username schema design
When configuring a JDBC url to connect to Azure Database for MySQL, the schemas of the username can be various according to different conventions:
|Username schema|Description|
|--|--|
|{username} |For the raw JDBC convention, the username is purely the name of a MySQL database user.|
|{username}@{mysql-server-name} |For [Azure MySQL convention](https://docs.microsoft.com/azure/mysql/connect-java#prepare-a-configuration-file-to-connect-to-azure-database-for-mysql), the username need to have a suffix of MySQL server.|
|{managed-identity-name}@{tenant-name}@{mysql-server-name}|For [Azure MySQL convention of AAD integration](https://docs.microsoft.com/azure/mysql/howto-configure-sign-in-azure-ad-authentication#step-3-use-token-as-password-for-logging-in-with-mysql), the username should contain both AAD and MySQL server information.|
|{managed-identity-client-id}@{tenant-name}@{mysql-server-name}|For the existing usage of [azure_mysql_msi extension](https://github.com/Azure/azure-jdbc-msi-extension/tree/main/jdbc), the username should contain client Id of the managed identity.|

For the advantages of each schema, the former two start with MySQL Java developers' perspective that they leverage the database username for connection. 
This can be more familiar for Java developers and not break their prior knowledge. For the officially promoted schema of `{username}@{mysql-server-name}`, it can further reduce the migration efforts of removing credentials from an Azure Database for MySQL application.
However, those schemas may bring extra developing efforts for the Managed Identity OAuth support since the username doesn't contain direct Managed Identity information. Therefore, to support it, we have to consider how to acquire the client Id of Managed Identities internally.

While the latter two schemas combines the Azure AD information and MySQL server, which can reduce our developing effort as analysed above. However, those schemas may not be friendly for users since they require developers to learn about more knowledge for the username.
In addition, to gather those Managed Identities information requires extra operations.

In summary, we prefer to use the officially promoted schema `{username}@{mysql-server-name}` to help users develop their credential-free MySQL application in a familiar and simple way.

### User scenario

We should make sure the above proposal can work in the below scenarios:
- user-assigned/system-assigned managed identities in VM
- user-assigned managed identities in Azure Spring Cloud
- user-assigned/system-assigned managed identities in App Service
- user-assigned/system-assigned managed identities in AKS
- user-assigned/system-assigned managed identities in Azure Functions

### Promotion

#### Blog
To promote our support of Managed Identity OAuth for Azure Database for MySQL, a blog to introduce how to use this function to remove credentials from an MySQL application can be published.
In this blog, we can show how to quickly configure the MySQL server and modify an existing application to achieve credential-free connection with minimum changes. 

For the Azure Database for MySQL server side, developers need to perform below operations:
1. Create the Managed Identity and configure it to the hosting service that an application runs on.
2. Configure the Azure Database for MySQL with [Azure AD integration](https://docs.microsoft.com/azure/mysql/howto-configure-sign-in-azure-ad-authentication)
3. Create a MySQL user for the created Managed Identity and grant necessary privileges.

For applications, developers can only do below modifications:
1. Import our library into their `pom.xml`.
2. Delete the password configuration.
3. Change the username value as the one created above. (Let's see whether we can manage to omit this step as well.)

#### Reference documentation
We can also publish the reference documentation to introduce our library of Managed Identities OAuth support for MySQL.
In the doc, the feature description, working principle and full configuration options should be introduced. Compared with the blog, this doc focuses on give a whole picture of how our library works,
how to use it and all the configuration it supports which may provide more enhanced functions.