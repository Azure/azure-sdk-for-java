# Azure AD B2C Spring Boot Starter client library for Java

Azure Active Directory (Azure AD) B2C is an identity management service that enables you to customize and control how
customers sign up, sign in, and manage their profiles when using your applications. Azure AD B2C enables these actions
while protecting the identities of your customers at the same time.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
- (Optional) You can refer to sample project to learn how to use this artifact: [ms-identity-java-spring-tutorial].

### Prerequisites
- [Environment checklist][environment_checklist]
- [Tutorial create Active Directory B2C tenant][tutorial_create_tenant]

### Include the package
1. [Add azure-spring-boot-bom].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-boot-bom`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
</dependency>
```

### Configurable properties
This starter provides following properties to be customized:

| Parameter | Description |
   |---|---|
| `azure.activedirectory.b2c.base-uri` | Base uri for authorization server, if both `tenant` and `baseUri` are configured at the same time, only `baseUri` takes effect. |
| `azure.activedirectory.b2c.client-id` | The registered application ID in Azure AD B2C. |
| `azure.activedirectory.b2c.client-secret` | The client secret of a registered application. |
| `azure.activedirectory.b2c.authorization-clients` | A map to list all authorization clients created on Azure Portal.  |
| `azure.activedirectory.b2c.login-flow` | The key name of sign in user flow. |
| `azure.activedirectory.b2c.logout-success-url` | The target URL after a successful logout. |   
| `azure.activedirectory.b2c.tenant(Deprecated)` | The Azure AD B2C's tenant name, this is only suitable for Global cloud. |
| `azure.activedirectory.b2c.tenant-id` | The Azure AD B2C's tenant id. |
| `azure.activedirectory.b2c.user-flows` | A map to list all user flows defined on Azure Portal.  |
| `azure.activedirectory.b2c.user-name-attribute-name` | The the attribute name of the user name.|

## Key concepts

A `web application` is any web based application that allows user to login Azure AD, whereas a `resource server` will either 
accept or deny access after validating access_token obtained from Azure AD. We will cover 4 scenarios in this guide:

1. Accessing a web application.
1. Web application accessing resource servers.
1. Accessing a resource server.
1. Resource server accessing other resource servers.

![B2C Web application & Web Api Overall](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c/resource/b2c-webapp-webapi-overall.png)

### Accessing a web application

This scenario uses the [The OAuth 2.0 authorization code grant] flow to login in a user with your Azure AD B2C user.

1. Select **Azure AD B2C** from the portal menu, click **Applications**, and then click **Add**.

1. Specify your application **Name**, we call it `webapp`, add `http://localhost:8080/login/oauth2/code/` for the **Reply URL**, record the
   **Application ID** as your `${your-webapp-client-id}` and then click **Save**.

1. Select **Keys** from your application, click **Generate key** to generate `${your-webapp-client-secret}` and then **Save**.

1. Select **User flows** on your left, and then Click **New user flow**.

1. Choose **Sign up or in**, **Profile editing** and **Password reset** to create user flows
   respectively. Specify your user flow **Name** and **User attributes and claims**, click **Create**.

1. Select **API permissions** > **Add a permission** > **Microsoft APIs**, select ***Microsoft Graph***,
   select **Delegated permissions**, check **offline_access** and **openid** permissions, select **Add permission** to complete the process.

1. Grant admin consent for ***Graph*** permissions.
   ![Add Graph permissions](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c/resource/add-graph-permissions.png)
   
1. Add the following dependencies in your *pom.xml*.

   ```xml
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
   
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
      <groupId>org.thymeleaf.extras</groupId>
      <artifactId>thymeleaf-extras-springsecurity5</artifactId>
    </dependency>
   ```

1. Add properties in *application.yml* using the values you created earlier, for example:

   ```yaml
   azure:
     activedirectory:
       b2c:
         authenticate-additional-parameters: 
           domain_hint: xxxxxxxxx         # optional
           login_hint: xxxxxxxxx          # optional
           prompt: [login,none,consent]   # optional
         base-uri: ${your-tenant-authorization-server-base-uri}
         client-id: ${your-webapp-client-id}
         client-secret: ${your-webapp-client-secret}
         login-flow: ${your-login-user-flow-key}               # default to sign-up-or-sign-in, will look up the user-flows map with provided key.
         logout-success-url: ${you-logout-success-url}
         user-flows:
           ${your-user-flow-key}: ${your-user-flow-name-defined-on-azure-portal}
         user-name-attribute-name: ${your-user-name-attribute-name}
   ```
   
1. Write your Java code.
    
    Controller code can refer to the following:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc/src/main/java/com/azure/spring/sample/aad/b2c/controller/WebController.java#L12-L30 -->
    ```java
    @Controller
    public class WebController {
    
        private void initializeModel(Model model, OAuth2AuthenticationToken token) {
            if (token != null) {
                final OAuth2User user = token.getPrincipal();
    
                model.addAllAttributes(user.getAttributes());
                model.addAttribute("grant_type", user.getAuthorities());
                model.addAttribute("name", user.getName());
            }
        }
    
        @GetMapping(value = { "/", "/home" })
        public String index(Model model, OAuth2AuthenticationToken token) {
            initializeModel(model, token);
            return "home";
        }
    }
    ```
    
    Security configuration code can refer to the following:
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc/src/main/java/com/azure/spring/sample/aad/b2c/security/WebSecurityConfiguration.java#L11-L29 -->
    ```java
    @EnableWebSecurity
    public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    
        private final AADB2COidcLoginConfigurer configurer;
    
        public WebSecurityConfiguration(AADB2COidcLoginConfigurer configurer) {
            this.configurer = configurer;
        }
    
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                .apply(configurer);
            // @formatter:off
        }
    }
    ```
    
    Copy the *home.html* from [Azure AD B2C Spring Boot Sample](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc/src/main/resources/templates), and replace the
    `${your-profile-edit-user-flow}` and `${your-password-reset-user-flow}` with your user flow name
    respectively that completed earlier.
    
1. Build and test your app
   
   Let `Webapp` run on port *8080*.
   
    1. After your application is built and started by Maven, open `http://localhost:8080/` in a web browser; 
    you should be redirected to login page.

    1. Click link with the login user flow, you should be redirected Azure AD B2C to start the authentication process.

    1. After you have logged in successfully, you should see the sample `home page` from the browser.

### Web application accessing resource servers

This scenario is based on **Accessing a web application** scenario to allow application to access other resources, that is [The OAuth 2.0 client credentials grant] flow.

1. Select **Azure AD B2C** from the portal menu, click **Applications**, and then click **Add**.

1. Specify your application **Name**, we call it `webApiA`, record the **Application ID** as your `${your-web-api-a-client-id}` and then click **Save**.

1. Select **Keys** from your application, click **Generate key** to generate `${your-web-api-a-client-secret}` and then **Save**.

1. Select **Expose an API** on your left, and then Click the **Set** link, specify your resource app id url suffix, such as *web-api-a*, 
   record the **Application ID URI** as your `${your-web-api-a-app-id-url}`, then **Save**.
   
1. Select **Manifest** on your left, and then paste the below json segment into `appRoles` array, 
   record the **Application ID URI** as your `${your-web-api-a-app-id-url}`, record the value of the app role as your `${your-web-api-a-role-value}`, then **save**.
   
    ```json
    {
      "allowedMemberTypes": [
        "Application"
      ],
      "description": "WebApiA.SampleScope",
      "displayName": "WebApiA.SampleScope",
      "id": "04989db0-3efe-4db6-b716-ae378517d2b7",
      "isEnabled": true,
      "value": "WebApiA.SampleScope"
    }
    ```
   
   ![Configure WebApiA appRoles](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c/resource/configure-app-roles.png)

1. Select **API permissions** > **Add a permission** > **My APIs**, select ***WebApiA*** application name, 
   select **Application Permissions**, select **WebApiA.SampleScope** permission, select **Add permission** to complete the process.
   
1. Grant admin consent for ***WebApiA*** permissions.
   ![Add WebApiA permission](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c/resource/add-webapia-permission.png)
   
1. Add the following dependency on the basis of **Accessing a web application** scenario.

    ```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    ```

1. Add the following configuration on the basis of **Accessing a web application** scenario.

   ```yaml
   azure:
     activedirectory:
       b2c:
         base-uri: ${your-base-uri}             # Such as: https://xxxxb2c.b2clogin.com
         tenant-id: ${your-tenant-id}
         authorization-clients:
           ${your-resource-server-a-name}:
             authorization-grant-type: client_credentials
             scopes: ${your-web-api-a-app-id-url}/.default
   ```

1. Write your `Webapp` Java code.

   Controller code can refer to the following:
    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/autoconfigure/b2c/WebappAccessResourceController.java#L25-L43 -->
    ```java
    /**
     * Access to protected data from Webapp to WebApiA through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data from WebApi A.
     */
    @GetMapping("/webapp/webApiA")
    public String callWebApiA() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_A_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webApiA"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Call callWebApiA(), request '/webApiA/sample' returned: {}", body);
        return "Request '/webApiA/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
    }
    ```

   Security configuration code is the same with **Accessing a web application** scenario, another bean `webClient`is added as follows:
    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/autoconfigure/b2c/WebappAccessResourceConfiguration.java#33-L40 -->
    ```java
    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction function =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
        return WebClient.builder()
                        .apply(function.oauth2Configuration())
                        .build();
    }
    ```

1. Please refer to **Accessing a resource server** section to write your `WebApiA` Java code.

1. Build and test your app
   
   Let `Webapp` and `WebApiA` run on port *8080* and *8081* respectively.
   Start `Webapp` and `WebApiA` application, return to the home page after logging successfully, you can access `http://localhost:8080/webapp/webApiA` to get **WebApiA** resource response.

### Accessing a resource server

This scenario not support login. Just protect the server by validating the access token, and if valid, serves the request.

1. Refer to [Web application accessing resource servers][web_application_accessing_resource_servers] to build your `WebApiA` permission.
   
1. Add `WebApiA` permission and grant admin consent for your web application.

1. Add the following dependencies in your *pom.xml*.

   ```xml
   <dependency>
     <groupId>com.azure.spring</groupId>
     <artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
   </dependency>

   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   ```

1. Add the following configuration.

   ```yaml
   azure:
     activedirectory:
       b2c:
         base-uri: ${your-base-uri}             # Such as: https://xxxxb2c.b2clogin.com
         tenant-id: ${your-tenant-id}
         app-id-uri: ${your-app-id-uri}         # If you are using v1.0 token, please configure app-id-uri for `aud` verification
         client-id: ${your-client-id}           # If you are using v2.0 token, please configure client-id for `aud` verification
   ```

1. Write your Java code.

   Controller code can refer to the following:
    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/autoconfigure/b2c/ResourceServerController.java#L25-L34 -->
    ```java
    /**
     * webApiA resource api for web app
     * @return test content
     */
    @PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
    @GetMapping("/webApiA/sample")
    public String webApiASample() {
        LOGGER.info("Call webApiASample()");
        return "Request '/webApiA/sample'(WebApi A) returned successfully.";
    }
    ```

   Security configuration code can refer to the following:
    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/autoconfigure/b2c/ResourceServerConfiguration.java#L11-L22 -->
    ```java
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {
    
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests((requests) -> requests.anyRequest().authenticated())
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new AADJwtBearerTokenAuthenticationConverter());
        }
    }
    ```

1. Build and test your app

   Let `WebApiA` run on port *8081*.
   Get the access token for `webApiA` resource and access `http://localhost:8081/webApiA/sample` 
   as the Bearer authorization header.

### Resource server accessing other resource servers

This scenario is an upgrade of **Accessing a resource server**, supports access to other application resources, based on OAuth2 client credentials flow.

1. Referring to the previous steps, we create a `WebApiB` application and expose an application permission `WebApiB.SampleScope`.
   
    ```json
    {
        "allowedMemberTypes": [
            "Application"
        ],
        "description": "WebApiB.SampleScope",
        "displayName": "WebApiB.SampleScope",
        "id": "04989db0-3efe-4db6-b716-ae378517d2b7",
        "isEnabled": true,
        "lang": null,
        "origin": "Application",
        "value": "WebApiB.SampleScope"
    }
    ```

   ![Configure WebApiB appRoles](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c/resource/expose-web-api-b-approle.png)
   
1. Grant admin consent for ***WebApiB*** permissions.
   ![Add WebApiB permission](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c/resource/add-webapib-permission-to-webapia.png)

1. On the basis of **Accessing a resource server**, add a dependency in your *pom.xml*.
   
   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-webflux</artifactId>
   </dependency>
   ```
   
1. Add the following configuration on the basis of **Accessing a resource server** scenario configuration.

   ```yaml
   azure:
     activedirectory:
       b2c:
         client-secret: ${your-web-api-a-client-secret}
         authorization-clients:
           ${your-resource-server-b-name}:
             authorization-grant-type: client_credentials
             scopes: ${your-web-api-b-app-id-url}/.default
   ```

1. Write your Java code.

   WebApiA controller code can refer to the following:
    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/autoconfigure/b2c/ResourceServerController.java#L47-L66 -->
    ```java
    /**
     * Access to protected data from WebApiA to WebApiB through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data from WebApi B.
     */
    @GetMapping("/webApiA/webApiB/sample")
    @PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
    public String callWebApiB() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_B_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webApiB"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Call callWebApiB(), request '/webApiB/sample' returned: {}", body);
        return "Request 'webApiA/webApiB/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
    }
    ```
   
   WebApiB controller code can refer to the following:
    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/autoconfigure/b2c/ResourceServerController.java#L36-L45 -->
    ```java
    /**
     * webApiB resource api for other web application
     * @return test content
     */
    @PreAuthorize("hasAuthority('APPROLE_WebApiB.SampleScope')")
    @GetMapping("/webApiB/sample")
    public String webApiBSample() {
        LOGGER.info("Call webApiBSample()");
        return "Request '/webApiB/sample'(WebApi B) returned successfully.";
    }
    ```

   Security configuration code is the same with **Accessing a resource server** scenario, another bean `webClient`is added as follows

1. Build and test your app

   Let `WebApiA` and `WebApiB` run on port *8081* and *8082* respectively.
   Start `WebApiA` and `WebApiB` application, get the access token for `webApiA` resource and access `http://localhost:8081/webApiA/webApiB/sample`
   as the Bearer authorization header.

## Examples

### Accessing a web application
Please refer to [azure-spring-boot-sample-active-directory-b2c-oidc].

### Accessing a resource server
Please refer to [azure-spring-boot-sample-active-directory-b2c-resource-server].

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] doc for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging).

### Enable authority logging.

Add the following logging settings:

```properties
# logging settings for resource server scenario.
logging.level.com.azure.spring.aad.AADJwtGrantedAuthoritiesConverter=DEBUG
```

Then you will see logs like this:

```text
...
DEBUG .a.s.a.AADJwtGrantedAuthoritiesConverter : User TestUser's authorities created from jwt token: [SCOPE_Test.Read, APPROLE_WebApi.ExampleScope].
...
```

## Next steps

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory-b2c-oidc
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-active-directory-b2c
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples
[logging]: https://docs.microsoft.com/en-us/azure/developer/java/sdk/logging-overview
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
[tutorial_create_tenant]: https://docs.microsoft.com/azure/active-directory-b2c/tutorial-create-tenant
[The OAuth 2.0 authorization code grant]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow
[The OAuth 2.0 client credentials grant]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
[web_application_accessing_resource_servers]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c#web-application-accessing-resource-servers
[azure-spring-boot-sample-active-directory-b2c-oidc]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc
[azure-spring-boot-sample-active-directory-b2c-resource-server]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-resource-server
[ms-identity-java-spring-tutorial]:https://github.com/Azure-Samples/ms-identity-java-spring-tutorial
