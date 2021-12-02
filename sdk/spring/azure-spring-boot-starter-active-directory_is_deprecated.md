
- [1. azure-spring-boot-starter-active-directory is deprecated](#1-azure-spring-boot-starter-active-directory-is-deprecated)
    * [1.1. Why deprecate azure-spring-boot-starter-active-directory](#11-why-deprecate-azure-spring-boot-starter-active-directory)
    * [1.2. What should I do if I still want to use the features provided by azure-spring-boot-starter-active-directory](#12-what-should-i-do-if-i-still-want-to-use-the-features-provided-by-azure-spring-boot-starter-active-directory)
        + [1.3. Mapping between azure-spring-boot-starter-active-directory and Spring Security framework](#13-mapping-between-azure-spring-boot-starter-active-directory-and-spring-security-framework)
        + [1.3.1. Application types in azure-spring-boot-starter-active-directory](#131-application-types-in-azure-spring-boot-starter-active-directory)
            - [1.2.1.1 Application is a web-application](#1211-application-is-a-web-application)
            - [1.2.1.2. Application is a resource-server](#1212-application-is-a-resource-server)
            - [1.2.1.3. Resource-server need to access other resource-server](#1213-resource-server-need-to-access-other-resource-server)
            - [1.2.1.4. Web-application and resource-server in one application](#1214-web-application-and-resource-server-in-one-application)
        + [1.2.2. Properties in azure-spring-boot-starter-active-directory](#122-properties-in-azure-spring-boot-starter-active-directory)
        + [1.2.3 Group based access control](#123-group-based-access-control)
            - [1.2.3.1 Group name based access control](#1231-group-name-based-access-control)
            - [1.2.3.2. Group id based access control](#1232-group-id-based-access-control)
                * [1.2.3.2.1. Group id based access control in client application](#12321-group-id-based-access-control-in-client-application)
                * [1.2.3.2.2. Group id based access control in resource server](#12322-group-id-based-access-control-in-resource-server)

# 1. azure-spring-boot-starter-active-directory is deprecated

## 1.1. Why deprecate azure-spring-boot-starter-active-directory

1. All features provided by azure-spring-boot-starter-active-directory can be achieved by Spring Security framework.
2. If one application depends on azure-spring-boot-starter-active-directory, it can not use other OAuth2 providers, such as google.
3. To implement some features, azure-spring-boot-starter-active-directory introduced some new concepts. These concepts are not necessary, and may make user confused.

## 1.2. What should I do if I still want to use the features provided by azure-spring-boot-starter-active-directory

Please refer to [spring-boot-application-with-azure-active-directory repo] to learn how to use [Azure Active Directory] in [Spring Boot] application.

### 1.3. Mapping between azure-spring-boot-starter-active-directory and Spring Security framework

### 1.3.1. Application types in azure-spring-boot-starter-active-directory

#### 1.2.1.1 Application is a web-application
Refer to [01-basic-scenario/client].

#### 1.2.1.2. Application is a resource-server
Refer to [01-basic-scenario/resource-server].

#### 1.2.1.3. Resource-server need to access other resource-server
Refer to [04-on-behalf-of-flow].

#### 1.2.1.4. Web-application and resource-server in one application
Just add both `spring-boot-starter-oauth2-client` and `spring-boot-starter-oauth2-resource-server` dependency in your `pom.xml`, and configure [Multiple HttpSecurity].


### 1.2.2. Properties in azure-spring-boot-starter-active-directory
| Properties                                                                             | How to handle this property if use Spring Security framework                                                                                     |
| -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **azure.activedirectory**.app-id-uri                                                   | Refer to [audience check in 01-basic-scenario/resource-server]    |
| **azure.activedirectory**.authorization-clients                                        | Refer to [client-registration configuration in 01-basic-scenario/client] |
| **azure.activedirectory**.authorization-clients.{client-name}.scopes                   | Refer to [client-registration configuration in 01-basic-scenario/client]|
| **azure.activedirectory**.authorization-clients.{client-name}.on-demand                | Refer to [homework of 03-multiple-resource-server]|
| **azure.activedirectory**.authorization-clients.{client-name}.authorization-grant-type | Refer to [Spring Boot 2.x Property Mappings] |
| **azure.activedirectory**.application-type                                             | Refer to [1.3.1. Application types in azure-spring-boot-starter-active-directory](#131-application-types-in-azure-spring-boot-starter-active-directory).|
| **azure.activedirectory**.base-uri                                                     | Refer to [client-registration configuration in 01-basic-scenario/client]  |
| **azure.activedirectory**.client-id                                                    | Refer to [client-registration configuration in 01-basic-scenario/client]|
| **azure.activedirectory**.client-secret                                                | Refer to [client-registration configuration in 01-basic-scenario/client]|
| **azure.activedirectory**.graph-membership-uri                                         | Refer to [client-registration configuration in 01-basic-scenario/client]|
| **azure.activedirectory**.post-logout-redirect-uri                                     | Refer to [OpenID Connect 1.0 Logout]                           |
| **azure.activedirectory**.resource-server.principal-claim-name                         | Configure jwtAuthenticationConverter like [02-check-permissions-by-claims-in-access-token/resource-server]             |
| **azure.activedirectory**.resource-server.claim-to-authority-prefix-map                | Refer to [02-check-permissions-by-claims-in-access-token/resource-server]|
| **azure.activedirectory**.tenant-id                                                    | Refer to [client-registration configuration in 01-basic-scenario/client]     |
| **azure.activedirectory**.user-group.allowed-group-names                               | Refer to [1.2.3 Group based access control](#123-group-based-access-control) |
| **azure.activedirectory**.user-group.allowed-group-ids                                 | Refer to [1.2.3 Group based access control](#123-group-based-access-control)|
| **azure.activedirectory**.user-name-attribute                                          | Refer to [user-name-attribute in 01-basic-scenario/client] |


### 1.2.3 Group based access control

#### 1.2.3.1 Group name based access control
Group name can be changed, so we do not suggest to use group name to do access control.

#### 1.2.3.2. Group id based access control

##### 1.2.3.2.1. Group id based access control in client application

In OAuth 2, authorization only happens when client access resource-server by access token. Only resource-server will validate the access control by access token. So access control in client application is not necessary.

##### 1.2.3.2.2. Group id based access control in resource server

Here is the 2 options:

1. Option 1: Use **group** claim in access token, refer to [02-check-permissions-by-claims-in-access-token].
2. Option 2: Use **role** claim in access token, and assign the role to all group members. Refer to [02-check-permissions-by-claims-in-access-token].

And option 2 is preferred, because:
1. In option 1, group-id should hard-code in you application code. Is hard to read and hard to maintain.
2. In larger organizations the number of groups a user is a member of may exceed the limit that Azure Active Directory will add to a token. 150 groups for a SAML token, and 200 for a JWT. This can lead to unpredictable results. Refs: [Configure group claims for applications with Azure Active Directory].



[Azure Active Directory]: https://azure.microsoft.com/services/active-directory/
[Spring Boot]: https://spring.io/projects/spring-boot
[spring-boot-application-with-azure-active-directory repo]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory
[01-basic-scenario/client]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/tree/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/01-basic-scenario/client
[01-basic-scenario/resource-server]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/tree/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/01-basic-scenario/resource-server
[04-on-behalf-of-flow]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/tree/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/04-on-behalf-of-flow
[Multiple HttpSecurity]: https://docs.spring.io/spring-security/site/docs/current/reference/html5/#multiple-httpsecurity
[audience check in 01-basic-scenario/resource-server]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/blob/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/01-basic-scenario/resource-server/src/main/java/com/azure/spring/sample/active/directory/oauth2/servlet/sample01/resource/server/configuration/ApplicationConfiguration.java#L28
[client-registration configuration in 01-basic-scenario/client]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/blob/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/01-basic-scenario/client/src/main/resources/application.yml#L20
[homework of 03-multiple-resource-server]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/blob/008b43011dd60a98b2e66b3818466239e94d6226/docs/servlet/oauth2/03-multiple-resource-server.md#5-homework
[Spring Boot 2.x Property Mappings]: https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2login-boot-property-mappings
[OpenID Connect 1.0 Logout]: https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2login-advanced-oidc-logout
[02-check-permissions-by-claims-in-access-token/resource-server]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/blob/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/02-check-permissions-by-claims-in-access-token/resource-server/src/main/java/com/azure/spring/sample/active/directory/oauth2/servlet/sample02/resource/server/configuration/WebSecurityConfiguration.java#L28
[user-name-attribute in 01-basic-scenario/client]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/blob/008b43011dd60a98b2e66b3818466239e94d6226/servlet/oauth2/01-basic-scenario/client/src/main/resources/application.yml#L19
[02-check-permissions-by-claims-in-access-token]: https://github.com/Azure-Samples/spring-boot-application-with-azure-active-directory/blob/spring-boot-2.5.x/docs/servlet/oauth2/02-check-permissions-by-claims-in-access-token.md
[Configure group claims for applications with Azure Active Directory]: https://docs.microsoft.com/en-us/azure/active-directory/hybrid/how-to-connect-fed-group-claims