# Sample for Azure AD B2C Spring Boot client library for Java

## Key concepts
This sample illustrates how to use `azure-spring-boot-starter-active-directory-b2c` package to work with OAuth 2.0 and OpenID Connect protocols with Azure Active Diretory B2C.

## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Create your Azure Active Directory B2C tenant

Follow the guide of [AAD B2C tenant creation](https://docs.microsoft.com/azure/active-directory-b2c/tutorial-create-tenant).

### Register your Azure Active Directory B2C application

Follow the guide of [AAD B2C application registry](https://docs.microsoft.com/azure/active-directory-b2c/tutorial-register-applications).
Please ensure that your b2c application's `Redirect URL` is configured to `http://localhost:8080/login/oauth2/code/`.

### Create user flows

Follow the guide of [AAD B2C user flows creation](https://docs.microsoft.com/azure/active-directory-b2c/tutorial-create-user-flows).

## Examples
### Configure the sample

#### application.yml

1. Fill in `${your-tenant-authorization-server-base-uri}` from **Azure AD B2C** portal `App registrations` blade, select **Endpoints**, copy the base endpoint uri(Global cloud format may looks like
`https://{your-tenant-name}.b2clogin.com/{your-tenant-name}.onmicrosoft.com`, China Cloud looks like `https://{your-tenant-name}.b2clogin.cn/{your-tenant-name}.partner.onmschina.cn`). 

    **NOTE**: The `azure.activedirectory.b2c.tenant` has been deprecated. Please `use azure.activedirectory.b2c.base-uri` instead.

2. Select one registered instance under `Applications` from portal, and then:
    1. Fill in `${your-client-id}` from `Application ID`.
    2. Fill in `${your-client-secret}` from one of `Keys`.
3. Add your user flows defined on the Azure Portal under the `user-flows` configuration, which is a map, you can give each user flow a key and the value will be the name of user flow defined in AAD B2C. 
   By default, we use the key `sign-up-or-sign-in` for a **login** user flow and `password-reset` for the **Password reset** type user flow, you can choose to override them.
4. Fill in `${your-login-user-flow-key}` with the key of your login user flow, we will use the value `sign-up-or-sign-in` to look up the user-flows map if this property is not provided.   
5. Replace `${your-logout-success-url}` to `http://localhost:8080/login`.

```yaml
azure:
  activedirectory:
    b2c:
      base-uri: ${your-tenant-authorization-server-base-uri}
      client-id: ${your-client-id}
      client-secret: ${your-client-secret}
      login-flow: ${your-login-user-flow-key}               # default to sign-up-or-sign-in, will look up the user-flows map with provided key.
      logout-success-url: ${your-logout-success-url}
      user-flows:
        password-reset: ${your-profile-edit-user-flow}
        profile-edit: ${your-password-reset-user-flow}
        sign-up-or-sign-in: ${your-sign-up-or-in-user-flow}
      user-name-attribute-name: ${your-user-name-claim}
```

**NOTE**: If both `tenant` and `baseUri` are configured at the same time, only `baseUri` takes effect.

### Run with Maven
```
cd azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc
mvn spring-boot:run
```

### Validation

1. Access `http://localhost:8080/` as index page.
2. Sign up/in.
3. Profile edit.
4. Password reset.
5. Log out.
6. Sign in.

## Troubleshooting
- `Missing attribute 'name' in attributes `

  ```
  java.lang.IllegalArgumentException: Missing attribute 'name' in attributes
  	at org.springframework.security.oauth2.core.user.DefaultOAuth2User.<init>(DefaultOAuth2User.java:67) ~[spring-security-oauth2-core-5.3.6.RELEASE.jar:5.3.6.RELEASE]
  	at org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser.<init>(DefaultOidcUser.java:89) ~[spring-security-oauth2-core-5.3.6.RELEASE.jar:5.3.6.RELEASE]
  	at org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService.loadUser(OidcUserService.java:144) ~[spring-security-oauth2-client-5.3.6.RELEASE.jar:5.3.6.RELEASE]
  	at org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService.loadUser(OidcUserService.java:63) ~[spring-security-oauth2-client-5.3.6.RELEASE.jar:5.3.6.RELEASE]
  ```

  While running sample, if error occurs with logs above:

  - make sure that while creating user workflow by following this [guide](https://docs.microsoft.com/azure/active-directory-b2c/tutorial-create-user-flows), for **User attributes and claims** , attributes and claims for **Display Name** should be chosen.

### FAQ

#### Sign in with loops to B2C endpoint ?
This issue almost due to polluted cookies of `localhost`. Clean up cookies of `localhost` and try it again.

#### More identity providers from AAD B2C login ?
Follow the guide of [Set up Google account with AAD B2C](https://docs.microsoft.com/azure/active-directory-b2c/active-directory-b2c-setup-goog-app).
And also available for Amazon, Azure AD, FaceBook, Github, Linkedin and Twitter.

## Next steps
## Contributing
<!-- LINKS -->

[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
