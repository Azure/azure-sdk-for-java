# OAuth 2.0 Sample for Azure AD Spring Boot Starter client library for Java

## Key concepts
This sample illustrates how to use `azure-active-directory-spring-boot-starter` package to work with OAuth 2.0 and OpenID Connect protocols on Auzre. This sample will use default Azure Active Directory Graph API to query user info.

## Getting started

### Register your application with your Azure Active Directory Tenant

Follow the guide [here](https://docs.microsoft.com/azure/active-directory/develop/active-directory-protocols-oauth-code#register-your-application-with-your-ad-tenant).

The sample retrieves user's group membership using Azure AD graph API which requires the registered app to have `Direcory.AccessAsUser.All` "Access the directory as the signed-in user" under `Delegated Permissions`. You need AAD admin privilege to be able to grant the permission in API ACCESS -> Required permission. You can follow the below steps:

* In the list of pages for the app, select **API permissions**
   - Click the **Add a permission** button and then,
   - Ensure that the **Microsoft APIs** tab is selected
   - In the *Supported legacy APIs* section, click on **Azure Active Directory Graph**
   - In the **Delegated permissions** section, ensure that the right permissions are checked: **Directory.AccessAsUser.All**
   - Select the **Add permissions** button
### Configure groups for sign in user

In order to try the authorization action with this sample with minimum effort, [configure the user and groups in Azure Active Directory](https://docs.microsoft.com/azure/active-directory/active-directory-groups-create-azure-portal), configure the user with `group1`. 

## Examples

### Configure application.properties
```properties
spring.security.oauth2.client.registration.azure.client-id=xxxxxx-your-client-id-xxxxxx
spring.security.oauth2.client.registration.azure.client-secret=xxxxxx-your-client-secret-xxxxxx

azure.activedirectory.tenant-id=xxxxxx-your-tenant-id-xxxxxx
# It's suggested the logged in user should at least belong to one of the below groups
# If not, the logged in user will not be able to access any authorization controller rest APIs
azure.activedirectory.user-group.allowed-groups=group1, group2
```

If `azure.activedirectory.tenant-id` is configured, `AADOAuth2LoginSecurityConfig` will take effect and this app will use AAD to authentication and authorization.
If `azure.activedirectory.tenant-id` is **NOT** configured, `NoLoginSecurityConfig` will take effect and this app will **NOT** use AAD to  authentication and authorization.

### Run with Maven
```shell
# Under sdk/spring project root directory
mvn clean install -DskipTests
cd azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend
mvn spring-boot:run
```

### Check the authentication and authorization
	
1. Access http://localhost:8080
2. Login
3. Access `group1 Message` link, should success
4. Access `group2 Message` link, should fail with forbidden error message


### <strong>*</strong> Take full control over every configuration property

If you want to adjust the configuration properties according to certain requirements, try below application.properties and change accordingly.

```properties
spring.security.oauth2.client.registration.azure.client-id=xxxxxx-your-client-id-xxxxxx
spring.security.oauth2.client.registration.azure.client-secret=xxxxxx-your-client-secret-xxxxxx
spring.security.oauth2.client.registration.azure.client-name=Azure
spring.security.oauth2.client.registration.azure.provider=azure-oauth-provider
spring.security.oauth2.client.registration.azure.scope=openid, https://graph.microsoft.com/user.read
spring.security.oauth2.client.registration.azure.redirect-uri-template={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.azure.client-authentication-method=basic
spring.security.oauth2.client.registration.azure.authorization-grant-type=authorization_code

spring.security.oauth2.client.provider.azure-oauth-provider.authorization-uri=https://login.microsoftonline.com/common/oauth2/authorize
spring.security.oauth2.client.provider.azure-oauth-provider.token-uri=https://login.microsoftonline.com/common/oauth2/token
spring.security.oauth2.client.provider.azure-oauth-provider.user-info-uri=https://login.microsoftonline.com/common/openid/userinfo
spring.security.oauth2.client.provider.azure-oauth-provider.jwk-set-uri=https://login.microsoftonline.com/common/discovery/keys
spring.security.oauth2.client.provider.azure-oauth-provider.user-name-attribute=name

azure.activedirectory.tenant-id=xxxxxx-your-tenant-id-xxxxxx
azure.activedirectory.user-group.allowed-groups=group1, group2
```

## Troubleshooting

### If registered application is not multi-tenanted, how to run this sample?
In this auto-configuration, by [default](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot/src/main/resources/aad-oauth2-common.properties#L1-L4) `/common` is used for the tenant value. According to [Active Directory Sign In Request format](https://docs.microsoft.com/azure/active-directory/develop/active-directory-protocols-openid-connect-code#send-the-sign-in-request), if your application is not multi-tenanted, you have to configure a tenant specific authorization endpoints.

Configure endpoints with specific tenant-id by replacing `common` in your application.properties file:
```properties
spring.security.oauth2.client.provider.azure.authorization-uri=https://login.microsoftonline.com/{your-tenant-id}/oauth2/authorize
spring.security.oauth2.client.provider.azure.token-uri=https://login.microsoftonline.com/{your-tenant-id}/oauth2/token
spring.security.oauth2.client.provider.azure.user-info-uri=https://login.microsoftonline.com/{your-tenant-id}/openid/userinfo
spring.security.oauth2.client.provider.azure.jwk-set-uri=https://login.microsoftonline.com/{your-tenant-id}/discovery/keys
```
----
### Meet with `AADSTS240002: Input id_token cannot be used as 'urn:ietf:params:oauth:grant-type:jwt-bearer' grant` error.
In Azure portal, app registration manifest page, configure `oauth2AllowImplicitFlow` in your application manifest to `true`. See [this issue](https://github.com/MicrosoftDocs/azure-docs/issues/8121#issuecomment-387090099) for details on this workaround.

## Next steps
## Contributing
