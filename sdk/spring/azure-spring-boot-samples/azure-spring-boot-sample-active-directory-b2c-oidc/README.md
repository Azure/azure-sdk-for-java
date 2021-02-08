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
Please make sure that your b2c application `reply URL` contains `http://localhost:8080/login/oauth2/code`.

### Create user flows

Follow the guide of [AAD B2C user flows creation](https://docs.microsoft.com/azure/active-directory-b2c/tutorial-create-user-flows).

## Examples
### Configure the sample

#### Application.yml

1. Fill in `${your-tenant-name}` from **Azure AD B2C** portal `Overviews` domain name (format may looks like
`${your-tenant-name}.onmicrosoft.com`).
2. Select one registered instance under `Applications` from portal, and then:
    1. Fill in `${your-client-id}` from `Application ID`.
    2. Fill in `${your-client-secret}` from one of `Keys`.
3. Select `User flows`, and then:
    1. Fill in the `${your-sign-up-or-in-user-flow}` with the name of `sign-in-or-up` user flow.
    2. Fill in the `${your-profile-edit-user-flow}` with the name of `profile-edit` user flow.
    3. Fill in the `${your-password-reset-user-flow}` with the name of `password-reset` user flow.
4. Replace `${your-reply-url}` to `http://localhost:8080/login/oauth2/code`.
5. Replace `${your-logout-success-url}` to `http://localhost:8080/login`.

```yaml
azure:
  activedirectory:
    b2c:
      tenant: ${your-tenant-name} # ❗not tenant id
      client-id: ${your-client-id}
      client-secret: ${your-client-secret}
      reply-url: ${your-reply-url} # should be absolute url.
      logout-success-url: ${your-logout-success-url}
      user-flows:
        sign-up-or-sign-in: ${your-sign-up-or-in-user-flow}
        profile-edit: ${your-profile-edit-user-flow}      # optional
        password-reset: ${your-password-reset-user-flow}  # optional
```

#### Templates greeting.html and home.html
1. Fill in the `${your-profile-edit-user-flow}` and `${your-password-reset-user-flow}` from the portal `User flows`.
Please make sure that these two placeholders should be the same as `application.yml` respectively.

### Run with Maven
```
cd azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc
mvn spring-boot:run
```

### Validation

1. Access `http://localhost:8080/` as index page.
2. Sign up/in.
3. Access greeting button.
4. Logout.
5. Sign in.
6. Profile edit.
7. Password reset.
8. Logout
9. Sign in.

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
