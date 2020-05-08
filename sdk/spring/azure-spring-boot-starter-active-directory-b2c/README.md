# Azure AD B2C Spring Boot Starter client library for Java
## Overview

Azure Active Directory (Azure AD) B2C is an identity management service that enables you to customize and control how
customers sign up, sign in, and manage their profiles when using your applications. Azure AD B2C enables these actions
while protecting the identities of your customers at the same time.

## Prerequisites

The following prerequisites are required in order to complete the steps in this article:

* A supported Java Development Kit (JDK). For more information about the JDKs available for use when developing on Azure, see <https://aka.ms/azure-jdks>.
* [Apache Maven](http://maven.apache.org/), version 3.0 or later.
* Azure subscription.

If you don't have an Azure subscription, create a [free account](https://azure.microsoft.com/free/?WT.mc_id=A261C142F) before you begin.

## Getting started

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

2. Specify your application **Name**, add `http://localhost:8080/home` for the **Reply URL**, record the
**Application ID** as your `${your-client-id}` and then click **Save**.

3. Select **Keys** from your application, click **Generate key** to generate `${your-client-secret}` and then **Save**.

4. Select **User flows** on your left, and then **Click** **New user flow **.

5. Choose **Sign up or in**, **Profile editing** and **Password reset** to create user flows
respectively. Specify your user flow **Name** and **User attributes and claims**, click **Create**.

## Examples
### Configure and compile your app

1. Extract the files from the project archive you created and downloaded earlier in this tutorial into a directory.

2. Navigate to the parent folder for your project, and open the `pom.xml` Maven project file in a text editor.

3. Add the dependencies for Spring OAuth2 security to the `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.azure</groupId>
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
         user-flows:
           sign-up-or-sign-in: ${your-sign-up-or-in-user-flow}
           profile-edit: ${your-profile-edit-user-flow}     # optional
           password-reset: ${your-password-reset-user-flow} # optional
   ```
   Where:

   | Parameter | Description |
   |---|---|
   | `azure.activedirectory.b2c.tenant` | Contains your AD B2C's `${your-tenant-name` from earlier. |
   | `azure.activedirectory.b2c.client-id` | Contains the `${your-client-id}` from your application that you completed earlier. |
   | `azure.activedirectory.b2c.client-secret` | Contains the `${your-client-secret}` from your application that you completed earlier. |
   | `azure.activedirectory.b2c.reply-url` | Contains one of the **Reply URL** from your application that you completed earlier. |
   | `azure.activedirectory.b2c.logout-success-url` | Specify the URL when your application logout successfully. |
   | `azure.activedirectory.b2c.user-flows` | Contains the name of the user flows that you completed earlier.

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
14. Copy the `greeting.html` and `home.html` from [Azure AD B2C Spring Boot Sample](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc/src/main/resources/templates), and replace the
`${your-profile-edit-user-flow}` and `${your-password-reset-user-flow}` with your user flow name
respectively that completed earlier.

### Build and test your app

1. Open a command prompt and change directory to the folder where your app's *pom.xml* file is located.

2. Build your Spring Boot application with Maven and run it; for example:

   ```shell
   mvn clean package
   mvn spring-boot:run
   ```

3. After your application is built and started by Maven, open <http://localhost:8080/> in a web browser; 
you should be redirected to login page.

4. Click linke with name of `${your-sign-up-or-in}` user flow, you should be rediected Azure AD B2C to start the authentication process.

4. After you have logged in successfully, you should see the sample `home page` from the browser.

## Key concepts

## Troubleshooting

## Next steps
#### Allow telemetry

Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.

```
azure.activedirectory.b2c.allow-telemetry=false
```

When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.

Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/en-us/privacystatement/OnlineServices/Default.aspx).

## Contributing

## Summary

In this documentation, you created a new Java web application using the Azure Active Directory B2C starter,
configured a new Azure AD B2C tenant and registered a new application in it, and then configured your
application to use the Spring annotations and classes to protect the web app.

