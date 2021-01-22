# Azure AD B2C Spring Boot Starter client library for Java

Azure Active Directory (Azure AD) B2C is an identity management service that enables you to customize and control how
customers sign up, sign in, and manage their profiles when using your applications. Azure AD B2C enables these actions
while protecting the identities of your customers at the same time.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](https://maven.apache.org/) 3.0 and above

### Include the package
[//]: # "{x-version-update-start;com.azure.spring:azure-spring-boot-starter-active-directory-b2c;current}"
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
    <version>3.1.0</version>
</dependency>
```
[//]: # "{x-version-update-end}"

### Create the Active Directory instance

1. Log into <https://portal.azure.com>.

2. Click **+Create a resource**, then **Identity**, and then **Azure Active Directory B2C**.

3. Enter your **Organization name** and your **Initial domain name**, record the **domain name** as your
`${your-tenant-name}` and click **Create**.

4. Select your account name on the top-right of the Azure portal toolbar, then click **Switch directory**.

5. Select your new Azure Active Directory from the drop-down menu.

6. Search `b2c` and click `Azure AD B2C` service.

### Add an application registration for your Spring Boot app

1. Select **Azure AD B2C** from the portal menu, click **Applications**, and then click **Add**.

2. Specify your application **Name**, add `https://localhost:8080/home` for the **Reply URL**, record the
**Application ID** as your `${your-client-id}` and then click **Save**.

3. Select **Keys** from your application, click **Generate key** to generate `${your-client-secret}` and then **Save**.

4. Select **User flows** on your left, and then Click **New user flow**.

5. Choose **Sign up or in**, **Profile editing** and **Password reset** to create user flows
respectively. Specify your user flow **Name** and **User attributes and claims**, click **Create**.

## Key concepts
In this documentation, you created a new Java web application using the Azure Active Directory B2C starter,
configured a new Azure AD B2C tenant and registered a new application in it, and then configured your
application to use the Spring annotations and classes to protect the web app.

### Configurable properties
This starter provides following properties to be customized:


 | Parameter | Description |
   |---|---|
   | `azure.activedirectory.b2c.client-id` | The registered application ID in Azure AD B2C. |
   | `azure.activedirectory.b2c.client-secret` | The client secret of a registered application. |
   | `azure.activedirectory.b2c.logout-success-url` | The target URL after a successful logout. |
   | `azure.activedirectory.b2c.reply-url` | The reply URL of a registered application. It's the same as the **Redirect URI** configured on Azure Portal.|   
   | `azure.activedirectory.b2c.tenant` | The Azure AD B2C's tenant name. |
   | `azure.activedirectory.b2c.user-flows.signUpOrSignIn` | The name of the **sign up and sign in** user flow. |
   | `azure.activedirectory.b2c.user-flows.profileEdit` | The name of the **profile editing** user flow. |
   | `azure.activedirectory.b2c.user-flows.passwordReset` | The name of the **password reset** user flow. |
   | `azure.activedirectory.b2c.user-name-attribute-name` | The the attribute name of the user name.|   
   
## Examples
### Configure and compile your app

1. Extract the files from the project archive you created and downloaded earlier in this tutorial into a directory.

2. Navigate to the parent folder for your project, and open the `pom.xml` Maven project file in a text editor.

3. Add the dependencies for Spring OAuth2 security to the `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.azure.spring</groupId>
       <artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-thymeleaf</artifactId>
   </dependency>
   <dependency>
       <groupId>org.thymeleaf.extras</groupId>
       <artifactId>thymeleaf-extras-springsecurity5</artifactId>
   </dependency>
   ```

4. Save and close the *pom.xml* file.

5. Navigate to the *src/main/resources* folder in your project and open the *application.yml* file in a text editor.

6. Specify the settings for your app registration using the values you created earlier; for example:

   ```yaml
   azure:
     activedirectory:
       b2c:
         tenant: ${your-tenant-name}
         client-id: ${your-client-id}
         client-secret: ${your-client-secret}
         reply-url: ${your-reply-url-from-aad} # should be absolute url.
         logout-success-url: ${you-logout-success-url}
         user-name-attribute-name: ${your-user-name-attribute-name}
         user-flows:
           sign-up-or-sign-in: ${your-sign-up-or-in-user-flow}
           profile-edit: ${your-profile-edit-user-flow}     # optional
           password-reset: ${your-password-reset-user-flow} # optional
         authenticate-additional-parameters: 
           prompt: [login,none,consent]   # optional
           login_hint: xxxxxxxxx          # optional
           domain_hint: xxxxxxxxx         # optional
   ```
7. Save and close the *application.yml* file.

8. Create a folder named *controller* in the Java source folder for your application.

9. Create a new Java file named *AADB2CWebController.java* in the *controller* folder and open it in a text editor.

10. Enter the following code, then save and close the file:
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/btoc/AADB2CWebController.java#L18-L50 -->
```java
@Controller
public class AADB2CWebController {

    private void initializeModel(Model model, OAuth2AuthenticationToken token) {
        if (token != null) {
            final OAuth2User user = token.getPrincipal();

            model.addAttribute("grant_type", user.getAuthorities());
            model.addAllAttributes(user.getAttributes());
        }
    }

    @GetMapping(value = "/")
    public String index(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);

        return "home";
    }

    @GetMapping(value = "/greeting")
    public String greeting(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);

        return "greeting";
    }

    @GetMapping(value = "/home")
    public String home(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);

        return "home";
    }
}
```

11. Create a folder named *security* in the Java source folder for your application.

12. Create a new Java file named *AADB2COidcLoginConfigSample.java* in the *security* folder and open it in a text editor.

13. Enter the following code, then save and close the file:
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/btoc/AADB2COidcLoginConfigSample.java#L17-L34 -->
```java
@EnableWebSecurity
public class AADB2COidcLoginConfigSample extends WebSecurityConfigurerAdapter {

    private final AADB2COidcLoginConfigurer configurer;

    public AADB2COidcLoginConfigSample(AADB2COidcLoginConfigurer configurer) {
        this.configurer = configurer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .apply(configurer);
    }
}
```
14. Copy the `greeting.html` and `home.html` from [Azure AD B2C Spring Boot Sample](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc/src/main/resources/templates), and replace the
`${your-profile-edit-user-flow}` and `${your-password-reset-user-flow}` with your user flow name
respectively that completed earlier.

### Build and test your app

1. Open a command prompt and change directory to the folder where your app's *pom.xml* file is located.

2. Build your Spring Boot application with Maven and run it; for example:

   ```shell
   mvn clean package
   mvn spring-boot:run
   ```

3. After your application is built and started by Maven, open <https://localhost:8080/> in a web browser; 
you should be redirected to login page.

4. Click linke with name of `${your-sign-up-or-in}` user flow, you should be rediected Azure AD B2C to start the authentication process.

4. After you have logged in successfully, you should see the sample `home page` from the browser.

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

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).
 
## Next steps
The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Azure Active Directory B2C](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory-b2c-oidc
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
