# Sample for Azure AD B2C Resource server Spring Boot client library for Java

## Key concepts
This sample illustrates how to use `azure-spring-boot-starter-active-directory-b2c` package to work with OAuth 2.0 and OpenID Connect protocols with Azure AD B2C to secure web services.

## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Create and consent Application permissions 
1. On the **Azure AD B2C** Portal, select the application that requires roles to be added, select **Manifest**.
2. Find the `appRoles` configuration item, and add the following configuration, then click the **Save** button.
```json
  {
    "allowedMemberTypes": [
      "Application"
    ],
    "description": "Task.read",
    "displayName": "Task.read",
    "id": "d2bec026-b75f-418d-9493-8462f54f25d9",
    "isEnabled": true, 
    "value": "Test.read"
  },
  {
    "allowedMemberTypes": [
      "Application"
    ],
    "description": "Task.wirte",
    "displayName": "Task.wirte",
    "id": "1ab4eeda-d07e-4bce-8f77-b0a84c97c34f",
    "isEnabled": true,
    "value": "Test.wirte"
  }
```

![Configuration Application Roles](docs/image-configuration-application-roles.png "Configuration Application Roles")

3. Find the application permissions need to use.

![Selected Application](docs/image-selected-application.png "Selected Application")
![Add Application Roles](docs/image-add-application-roles.png "Add Application Roles")

4. Consent Application permissions.

![Consent Application permissions](docs/image-consent-application-permissions.png "Consent Application permissions")

5. In the end, configuration is as follows.
   
![Final Configuration](docs/image-final-configuration.png "Final Configuration")

## Examples
### Configure the sample
#### application.yml

```yaml
# In v2.0 tokens, this is always the client ID of the API, while in v1.0 tokens it can be the resource URI used in the request.
# If we configure azure.activedirectory.b2c.app-id-uri will be to check the audience.
# If you are using v1.0 tokens, configure app-id-uri to properly complete the audience validation.

azure:
  activedirectory:
    b2c:
      client-id: ${your-client-id}
      tenant-id: ${your-tenant-id}
      app-id-uri: ${your-app-id-uri}         # If you are using v1.0 tokens, configure app-id-uri to properly complete the audience validation. 
```

### Run with Maven
```
cd azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-resource-server
mvn spring-boot:run
```

### Access the Web API
We could use Postman to simulate a Web APP to send a request to a Web API.
**NOTE**: The `aud` in access token should be client id of the current application.

```http request
GET /write HTTP/1.1
Authorization: Bearer eyJ0eXAiO ... 0X2tnSQLEANnSPHY0gKcgw
```
```http request
GET /read HTTP/1.1
Authorization: Bearer eyJ0eXAiO ... 0X2tnSQLEANnSPHY0gKcgw
```

## Troubleshooting
- `Missing 'tenant-id' in application.yml`
    
    ```
    Method springSecurityFilterChain in org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration required a bean of type 'org.springframework.security.oauth2.jwt.JwtDecoder' that could not be found.
        The following candidates were found but could not be injected:
        - Bean method 'jwtDecoder' in 'AADResourceServerConfiguration' not loaded because @ConditionalOnResource did not find resource 'classpath:aad.enable.config'
        - Bean method 'jwtDecoder' in 'AADB2CResourceServerAutoConfiguration' not loaded because @ConditionalOnProperty (azure.activedirectory.b2c.tenant-id) did not find property 'tenant-id'
        - Bean method 'jwtDecoderByIssuerUri' in 'OAuth2ResourceServerJwtConfiguration.JwtDecoderConfiguration' not loaded because OpenID Connect Issuer URI Condition did not find issuer-uri property
        - Bean method 'jwtDecoderByJwkKeySetUri' in 'OAuth2ResourceServerJwtConfiguration.JwtDecoderConfiguration' not loaded because @ConditionalOnProperty (spring.security.oauth2.resourceserver.jwt.jwk-set-uri) did not find property 'spring.security.oauth2.resourceserver.jwt.jwk-set-uri'
        - Bean method 'jwtDecoderByPublicKeyValue' in 'OAuth2ResourceServerJwtConfiguration.JwtDecoderConfiguration' not loaded because Public Key Value Condition did not find public-key-location property
    ```

    While running sample, if error occurs with logs above:
    - `azure-activedirectory-b2c:tenant-id` should be added to the `application.yml`.
---
- `WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Couldn't retrieve remote JWK set: Read timed out",`
  
    While running sample, if error occurs with logs above:
    - `azure-activedirectory-b2c:jwt-read-timeout` to set longer read time in `application.yml`.
    
### FAQ
#### How do I delete or modify Application Permissions in Portal?
You can set `isEnabled` to `false` in the manifest's JSON configuration.Then delete or modify it.

## Next steps
## Contributing
<!-- LINKS -->

[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
