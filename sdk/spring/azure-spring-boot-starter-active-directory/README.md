# Azure AD Spring Boot Starter client library for Java

When you are building a web application, identity and access management will always be foundational pieces.

Azure offers a great platform to democratize your application development journey, as it not only offers a cloud-base identity service, but also deep integration with the rest of the Azure ecosystem.

Spring Security has made it easy to secure your Spring based applications with powerful abstractions and extensible interfaces. However, as powerful as the Spring framework can be, it is not tailored to a specific identity provider.

The `azure-spring-boot-starter-active-directory` (`aad-starter` for short) provides the most optimal way to connect your `web application` to an Azure Active Directory(AAD for short) tenant and protect `resource server` with AAD. It uses the Oauth 2.0 protocol to protect `web applications` and `resource servers`.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]
- [Register an application in Azure Portal][register_an_application_in_portal]

## Key concepts

A `web application` is any web based application that allows user to login, whereas a `resource server` will either accept or deny access after validating access_token. We will cover 4 scenarios in this guide:

1. Accessing a web application.
1. Web application accessing resource servers.
1. Accessing a resource server.
1. Resource server accessing other resource servers.

### Accessing a web application

This scenario uses the [The OAuth 2.0 authorization code grant] flow to login in a user with a Microsoft account. 

**System diagram**:

![Standalone Web Application](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/web-application.png)


* Step 1: Make sure `redirect URI` has been set to `{application-base-uri}/login/oauth2/code/`, for 
example `http://localhost:8080/login/oauth2/code/`. Note the tailing `/` cannot be omitted.

    ![web-application-set-redirect-uri-1.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/web-application-set-redirect-uri-1.png)
    ![web-application-set-redirect-uri-2.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/web-application-set-redirect-uri-2.png)

* Step 2: Add the following dependencies in your pom.xml.

    [//]: # "{x-version-update-start;com.azure.spring:azure-spring-boot-starter-active-directory;current}"
    ```xml
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>azure-spring-boot-starter-active-directory</artifactId>
        <version>3.5.0-beta.1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    ```
    [//]: # "{x-version-update-end}"
   
* Step 3: Add properties in application.yml. These values should be got in [prerequisite].
    ```yaml
    azure:
      activedirectory:
        tenant-id: xxxxxx-your-tenant-id-xxxxxx
        client-id: xxxxxx-your-client-id-xxxxxx
        client-secret: xxxxxx-your-client-secret-xxxxxx
    ```

* Step 4: Write your Java code:

    `AADWebSecurityConfigurerAdapter` contains necessary web security configuration for **aad-starter**.

     (A). `DefaultAADWebSecurityConfigurerAdapter` is configured automatically if you not provide one.
    
     (B). You can provide one by extending `AADWebSecurityConfigurerAdapter` and call `super.configure(http)` explicitly 
    in the `configure(HttpSecurity http)` function. Here is an example:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/security/AADOAuth2LoginSecurityConfig.java#L11-L25 -->
    ```java
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class AADOAuth2LoginSecurityConfig extends AADWebSecurityConfigurerAdapter {
    
        /**
         * Add configuration logic as needed.
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                    .anyRequest().authenticated();
            // Do some custom configuration
        }
    }
    ```

### Web application accessing resource servers

**System diagram**:

![web-application-visiting-resource-servers.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/web-application-visiting-resource-servers.png)

* Step 1: Make sure `redirect URI` has been set, just like [Accessing a web application].

* Step 2: Add the following dependencies in you pom.xml.

    [//]: # "{x-version-update-start;com.azure.spring:azure-spring-boot-starter-active-directory;current}"
    ```xml
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>azure-spring-boot-starter-active-directory</artifactId>
        <version>3.5.0-beta.1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    ```
    [//]: # "{x-version-update-end}"

* Step 3: Add properties in application.yml:
    ```yaml
    azure:
      activedirectory:
        tenant-id: xxxxxx-your-tenant-id-xxxxxx
        client-id: xxxxxx-your-client-id-xxxxxx
        client-secret: xxxxxx-your-client-secret-xxxxxx
        authorization-clients:
          graph:
            scopes: https://graph.microsoft.com/Analytics.Read, email
    ```
    Here, `graph` is the name of `OAuth2AuthorizedClient`, `scopes` means the scopes need to consent when login.

* Step 4: Write your Java code:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/controller/ClientController.java#L40-L48 -->
    ```java
    @GetMapping("/graph")
    @ResponseBody
    public String graph(
        @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphClient
    ) {
        // toJsonString() is just a demo.
        // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
        return toJsonString(graphClient);
    }
    ```
    Here, `graph` is the client name configured in step 2. OAuth2AuthorizedClient contains access_token. 
access_token can be used to access resource server.

### Accessing a resource server
This scenario not support login. Just protect the server by validating the access_token, and if valid, serves the request.

**System diagram**:

![Standalone resource server usage](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/resource-server.png)

To use **aad-starter** in this scenario, we need these steps:

* Step 1: Add the following dependencies in you pom.xml.

    [//]: # "{x-version-update-start;com.azure.spring:azure-spring-boot-starter-active-directory;current}"
    ```xml
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>azure-spring-boot-starter-active-directory</artifactId>
        <version>3.5.0-beta.1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    ```
    [//]: # "{x-version-update-end}"

* Step 2: Add properties in application.yml:
    ```yaml
    azure:
      activedirectory:
        client-id: <client-id>
        app-id-uri: <app-id-uri>
    ```
    Both `client-id` and `app-id-uri` can be used to verify access token. `app-id-uri` can be get in Azure Portal:
    
    ![get-app-id-uri-1.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/get-app-id-uri-1.png)
    ![get-app-id-uri-2.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/get-app-id-uri-2.png)

* Step 3: Write Java code:
  
    `AADResourceServerWebSecurityConfigurerAdapter` contains necessary web security configuration for resource server.

    (A). `DefaultAADResourceServerWebSecurityConfigurerAdapter` is configured automatically if you not provide one.
    
    (B). You can provide one by extending `AADResourceServerWebSecurityConfigurerAdapter` and call `super.configure(http)` explicitly
    in the `configure(HttpSecurity http)` function. Here is an example:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server/src/main/java/com/azure/spring/sample/aad/security/AADOAuth2ResourceServerSecurityConfig.java#L12-L23 -->
    ```java
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class AADOAuth2ResourceServerSecurityConfig extends AADResourceServerWebSecurityConfigurerAdapter {
        /**
         * Add configuration logic as needed.
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests((requests) -> requests.anyRequest().authenticated());
        }
    }
    ```

### Resource server visiting other resource servers

This scenario support visit other resource servers in resource servers.

**System diagram**:

![resource-server-visiting-other-resource-servers.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/resource-server-visiting-other-resource-servers.png)

To use **aad-starter** in this scenario, we need these steps:

* Step 1: Add the following dependencies in you pom.xml.

    [//]: # "{x-version-update-start;com.azure.spring:azure-spring-boot-starter-active-directory;current}"
    ```xml
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>azure-spring-boot-starter-active-directory</artifactId>
        <version>3.5.0-beta.1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    ```
    [//]: # "{x-version-update-end}"

* Step 2: Add properties in application.yml:
    ```yaml
    azure:
       activedirectory:
          tenant-id: <Tenant-id-registered-by-application>
          client-id: <Web-API-A-client-id>
          client-secret: <Web-API-A-client-secret>
          app-id-uri: <Web-API-A-app-id-url>
          authorization-clients:
             graph:
                scopes:
                   - https://graph.microsoft.com/User.Read
    ```

* Step 3: Write Java code:

    Using `@RegisteredOAuth2AuthorizedClient` to access related resource server:

    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server-obo/src/main/java/com/azure/spring/sample/aad/controller/SampleController.java#L64-L68 --> 
    ```java
    @PreAuthorize("hasAuthority('SCOPE_Obo.Graph.Read')")
    @GetMapping("call-graph")
    public String callGraph(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph) {
        return callMicrosoftGraphMeEndpoint(graph);
    }
    ```

### Configurable properties

This starter provides following properties:

| Properties                                                              | Description                                                                                    |
| ----------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **azure.activedirectory**.app-id-uri                                    | It used in resource server, used to validate the audience in access_token. access_token is valid only when the audience in access_token equal to client-id or app-id-uri    |
| **azure.activedirectory**.authorization-clients                         | A map configure the resource APIs the application is going to visit. Each item corresponding to one resource API the application is going to visit. In Spring code, each item corresponding to one OAuth2AuthorizedClient object|
| **azure.activedirectory**.authorization-clients.{client-name}.scopes    | API permissions of a resource server that the application is going to acquire.                 |
| **azure.activedirectory**.authorization-clients.{client-name}.on-demand | This is used for incremental consent. The default value is false. If it's true, it's not consent when user login, when application needs the additional permission, incremental consent is performed with one OAuth2 authorization code flow.|
| **azure.activedirectory**.base-uri                                      | Base uri for authorization server, the default value is `https://login.microsoftonline.com/`.  |
| **azure.activedirectory**.client-id                                     | Registered application ID in Azure AD.                                                         |
| **azure.activedirectory**.client-secret                                 | client secret of the registered application.                                                   |
| **azure.activedirectory**.graph-membership-uri                          | It's used to load users' groups. The default value is `https://graph.microsoft.com/v1.0/me/memberOf`, this uri just get direct groups. To get all transitive membership, set it to `https://graph.microsoft.com/v1.0/me/transitiveMemberOf`. The 2 uris are both Azure Global, check `Property example 1` if you want to use Azure China.|
| **azure.activedirectory**.post-logout-redirect-uri                      | Redirect uri for posting log-out.                            |
| **azure.activedirectory**.tenant-id                                     | Azure Tenant ID.                                             |
| **azure.activedirectory**.user-group.allowed-groups                     | Expected user groups that an authority will be granted to if found in the response from the MemeberOf Graph API Call. |
| **azure.activedirectory**.user-name-attribute                           | Decide which claim to be principal's name. |

Here are some examples about how to use these properties:

#### Property example 1: Use [Azure China] instead of Azure Global.

* Step 1: Add property in application.yml
    ```yaml
    azure:
      activedirectory:
        base-uri: https://login.partner.microsoftonline.cn
        graph-base-uri: https://microsoftgraph.chinacloudapi.cn
    ```

#### Property example 2: Use `group name` to protect some method in web application.

* Step 1: Add property in application.yml
    ```yaml
    azure:
      activedirectory:
        user-group:
          allowed-groups: group1, group2
    ```

* Step 2: Add `@EnableGlobalMethodSecurity(prePostEnabled = true)` in web application:

    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/security/AADOAuth2LoginSecurityConfig.java#L11-L25 -->
    ```java
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class AADOAuth2LoginSecurityConfig extends AADWebSecurityConfigurerAdapter {
    
        /**
         * Add configuration logic as needed.
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                    .anyRequest().authenticated();
            // Do some custom configuration
        }
    }
    ```

    Then we can protect the method by `@PreAuthorize` annotation:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/controller/RoleController.java#L11-L40 -->
    ```java
    @Controller
    public class RoleController {
        @GetMapping("group1")
        @ResponseBody
        @PreAuthorize("hasRole('ROLE_group1')")
        public String group1() {
            return "group1 message";
        }
    
        @GetMapping("group2")
        @ResponseBody
        @PreAuthorize("hasRole('ROLE_group2')")
        public String group2() {
            return "group2 message";
        }
    
        @GetMapping("group1Id")
        @ResponseBody
        @PreAuthorize("hasRole('ROLE_<group1-id>')")
        public String group1Id() {
            return "group1Id message";
        }
    
        @GetMapping("group2Id")
        @ResponseBody
        @PreAuthorize("hasRole('ROLE_<group2-id>')")
        public String group2Id() {
            return "group2Id message";
        }
    }
    ```

#### Property example 3: [Incremental consent] in Web application visiting resource servers.

* Step 1: Add property in application.yml
    ```yaml
    azure:
      activedirectory:
        authorization-clients:
          graph:
            scopes: https://graph.microsoft.com/Analytics.Read, email
          arm: # client registration id
            on-demand: true  # means incremental consent
            scopes: https://management.core.windows.net/user_impersonation
    ```

* Step 2: Write Java code:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/controller/OnDemandClientController.java#L17-L25 -->
    ```java
    @GetMapping("/arm")
    @ResponseBody
    public String arm(
        @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient armClient
    ) {
        // toJsonString() is just a demo.
        // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
        return toJsonString(armClient);
    }
    ```

    After these steps. `arm`'s scopes (https://management.core.windows.net/user_impersonation) doesn't 
    need to be consented at login time. When user request `/arm` endpoint, user need to consent the 
    scope. That's `incremental consent` means.
    
    After the scopes have been consented, AAD server will remember that this user has already granted 
    the permission to the web application. So incremental consent will not happen anymore after user 
    consented.
  
#### Property example 4: [Client credential flow] in resource server visiting resource servers.

* Step 1: Add property in application.yml
    ```yaml
    azure:
      activedirectory:
        authorization-clients:
          webapiC:                          # When authorization-grant-type is null, on behalf of flow is used by default
            authorization-grant-type: client_credentials
            scopes:
                - <Web-API-C-app-id-url>/.default
    ```

* Step 2: Write Java code:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server-obo/src/main/java/com/azure/spring/sample/aad/controller/SampleController.java#L134-L146 -->
    ```java
    @PreAuthorize("hasAuthority('SCOPE_Obo.WebApiA.ExampleScope')")
    @GetMapping("webapiA/webapiC")
    public String callClientCredential() {
        String body = webClient
            .get()
            .uri(CUSTOM_LOCAL_READ_ENDPOINT)
            .attributes(clientRegistrationId("webapiC"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Response from Client Credential: {}", body);
        return "client Credential response " + (null != body ? "success." : "failed.");
    }
    ```

### Advanced features

#### Support access control by id token in web application 

This starter supports creating `GrantedAuthority` from id_token's `roles` claim to allow using `id_token` for authorization in web application. Developers can use the
`appRoles` feature of Azure Active Directory to create `roles` claim and implement access control. 

Note:
 - The `roles` claim generated from `appRoles` is decorated with prefix `APPROLE_`.
 - When using `appRoles` as `roles` claim, please avoid configuring group attribute as `roles` at the same time. The latter will override the claim to contain group information instead of `appRoles`. Below configuration in manifest should be avoided:
    ```
    "optionalClaims": {
        "idtoken": [{
            "name": "groups",
            "additionalProperties": ["emit_as_roles"]
        }]
    }
    ```

Follow the guide to [add app roles in your application and assign to users or groups](https://docs.microsoft.com/azure/active-directory/develop/howto-add-app-roles-in-azure-ad-apps).

* Step 1: Add below `appRoles` configuration in your application's manifest:
    ```
      "appRoles": [
        {
          "allowedMemberTypes": [
            "User"
          ],
          "displayName": "Admin",
          "id": "2fa848d0-8054-4e11-8c73-7af5f1171001",
          "isEnabled": true,
          "description": "Full admin access",
          "value": "Admin"
         }
      ]
    ```
* Step 2: Write Java code:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/controller/AuthorityController.java#L13-L18 -->
    ```java
    @GetMapping("Admin")
    @ResponseBody
    @PreAuthorize("hasAuthority('APPROLE_Admin')")
    public String Admin() {
        return "Admin message";
    }
    ```

#### Support Conditional Access in web application.
  
This starter supports [Conditional Access] policy. By using [Conditional Access] policies, you can apply the right **access controls** when needed to keep your organization secure. **Access controls** has many concepts, [Block Access] and [Grant Access] are important. In some scenarios, this stater will help you complete [Grant Access] controls. 
 
In [Resource server visiting other resource server] scenario(For better description, we think that resource server with OBO function as **webapiA** and the other resource servers as **webapiB**), When we configure the webapiB application with Conditional Access(such as [multi-factor authentication]), this stater will help us send the Conditional Access information of the webapiA to the web application and the web application will help us complete the Conditional Access Policy. As shown below:

  ![aad-conditional-access-flow.png](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/aad-conditional-access-flow.png)
  
  
  
 We can use our sample to create a Conditional Access scenario.
  1. **webapp**: [azure-spring-boot-sample-active-directory-webapp].
  1. **webapiA**:  [azure-spring-boot-sample-active-directory-resource-server-obo].
  1. **webapiB**: [azure-spring-boot-sample-active-directory-resource-server]. 
  
* Step 1: Follow the guide to create conditional access policy for webapiB.
  
    ![aad-create-conditional-access](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/aad-create-conditional-access.png)
  
    ![aad-conditional-access-add-application](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/aad-conditional-access-add-application.png) 
  
* Step 2: [Require MFA for all users] or specify the user account in your policy.

    ![aad-create-conditional-access](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/resource/aad-conditional-access-add-user.png)
    
* Step 3: Follow the guide, configure our samples.
   1. **webapiB**: [configure webapiB]
   1. **webapiA**: [configure webapiA]
   1. **webapp**: [configure webapp]
    
* Step 4: Add properties in application.yml.  
  	
    - webapp:
     ```yaml
     azure:
       activedirectory:
         client-id: <Web-API-A-client-id>
         client-secret: <Web-API-A-client-secret>
         tenant-id: <tenant-id-registered-by-application>
         app-id-uri: <Web-API-A-app-id-url>
         authorization-clients:
           webapiA:
             scopes:
               - <Web-API-A-app-id-url>/Obo.WebApiA.ExampleScope
     ```   
    - webapiA:
     ```yaml
     azure:
       activedirectory:
         client-id: <Web-API-A-client-id>
         client-secret: <Web-API-A-client-secret>
         tenant-id: <tenant-id-registered-by-application>
         app-id-uri: <Web-API-A-app-id-url>
         authorization-clients:
           webapiB:
             scopes:
               - <Web-API-B-app-id-url>/WebApiB.ExampleScope
     ```
    - webapiB:
     ```yaml
     azure:
       activedirectory:
          client-id: <Web-API-B-client-id>
          app-id-uri: <Web-API-B-app-id-url>
     ```
      
* Step 5: Write your Java code:  	
    - webapp :
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/src/main/java/com/azure/spring/sample/aad/controller/WebApiController.java#L34-L38 -->    
    ```java
    @GetMapping("/webapp/webapiA/webapiB")
    @ResponseBody
    public String callWebApi(@RegisteredOAuth2AuthorizedClient("webapiA") OAuth2AuthorizedClient webapiAClient) {
        return callWebApiAEndpoint(webapiAClient);
    }
    ```
    - webapiA:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server-obo/src/main/java/com/azure/spring/sample/aad/controller/SampleController.java#L76-L81 -->    
    ```java
    @PreAuthorize("hasAuthority('SCOPE_Obo.WebApiA.ExampleScope')")
    @GetMapping("webapiA/webapiB")
    public String callCustom(
        @RegisteredOAuth2AuthorizedClient("webapiB") OAuth2AuthorizedClient webapiBClient) {
        return callWebApiBEndpoint(webapiBClient);
    }
    ```
    - webapiB:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server/src/main/java/com/azure/spring/sample/aad/controller/HomeController.java#L16-L21 -->      
    ```java
    @GetMapping("/webapiB")
    @ResponseBody
    @PreAuthorize("hasAuthority('SCOPE_WebApiB.ExampleScope')")
    public String file() {
        return "Response from WebApiB.";
    }
    ```  
## Examples

### Web application visiting resource servers
Please refer to [azure-spring-boot-sample-active-directory-webapp].

### Resource server
Please refer to [azure-spring-boot-sample-active-directory-resource-server].

### Resource server visiting other resource servers
Please refer to [azure-spring-boot-sample-active-directory-resource-server-obo].

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc].

## Next steps

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here] to build from source or contribute.

<!-- LINKS -->
[Azure Portal]: https://ms.portal.azure.com/#home
[The OAuth 2.0 authorization code grant]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow
[azure-spring-boot-sample-active-directory-webapp]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp
[azure-spring-boot-sample-active-directory-resource-server]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server/README.md
[azure-spring-boot-sample-active-directory-resource-server-obo]: https://github.com/ZhuXiaoBing-cn/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server-obo
[azure-spring-boot-sample-active-directory-resource-server-by-filter]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server-by-filter
[AAD App Roles feature]: https://docs.microsoft.com/azure/architecture/multitenant-identity/app-roles#roles-using-azure-ad-app-roles
[client credentials grant flow]: https://docs.microsoft.com/azure/active-directory/develop/v1-oauth2-client-creds-grant-flow
[configured in your manifest]: https://docs.microsoft.com/azure/active-directory/develop/howto-add-app-roles-in-azure-ad-apps#examples
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory
[graph-api-list-member-of]: https://docs.microsoft.com/graph/api/user-list-memberof?view=graph-rest-1.0
[graph-api-list-transitive-member-of]: https://docs.microsoft.com/graph/api/user-list-transitivememberof?view=graph-rest-1.0
[instructions here]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[official doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[OAuth 2.0 implicit grant flow]: https://docs.microsoft.com/azure/active-directory/develop/v1-oauth2-implicit-grant-flow
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-active-directory
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples
[set up in the manifest of your application registration]: https://docs.microsoft.com/azure/active-directory/develop/howto-add-app-roles-in-azure-ad-apps
[Azure China]: https://docs.microsoft.com/azure/china/resources-developer-guide#check-endpoints-in-azure
[Incremental consent]: https://docs.microsoft.com/azure/active-directory/azuread-dev/azure-ad-endpoint-comparison#incremental-and-dynamic-consent
[register_an_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/quickstart-register-app
[prerequisite]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-starter-active-directory#prerequisites
[Accessing a web application]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-starter-active-directory#accessing-a-web-application
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Conditional Access]: https://docs.microsoft.com/azure/active-directory/conditional-access
[Grant Access]: https://docs.microsoft.com/azure/active-directory/conditional-access/concept-conditional-access-grant
[Block Access]: https://docs.microsoft.com/azure/active-directory/conditional-access/howto-conditional-access-policy-block-access
[Resource server visiting other resource server]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-starter-active-directory#resource-server-visiting-other-resource-servers
[multi-factor authentication]: https://docs.microsoft.com/azure/active-directory/authentication/concept-mfa-howitworks
[Require MFA for all users]: https://docs.microsoft.com/azure/active-directory/conditional-access/howto-conditional-access-policy-all-users-mfa
[configure webapiA]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server-obo#configure-your-middle-tier-web-api-a
[configure webapiB]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-resource-server/README.md#configure-web-api
[configure webapp]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp/README.md#configure-access-other-resources-server