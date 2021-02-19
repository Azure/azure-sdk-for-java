# OAuth 2.0 Sample for Azure AD Spring Boot Starter client library for Java

## Key concepts
This sample illustrates how to use `azure-spring-boot-starter-active-directory` package to work with OAuth 2.0 and OpenID Connect protocols on Auzre. This sample will use Microsoft Graph API to retrieve user infomation.

## Getting started
### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Configure web app
1. Search for and select your tenant in **Azure Active Directory**.
1. Under Manage In the same tenant, select **App registrations** -> **New registration**.![Portal manage](docs/image-portal-manage.png "Portal manage")
1. The registered application name is filled into `webapp`, select **Accounts in this organizational directory only**, click the **register** button.![Register a web app](docs/image-register-a-web-app.png "Register a web app")
1. Under **webapp** application, select **Certificates & secrets** -> **new client secret**, click the **add** button.(Remember to save the secrets here and use them later.)![Create secrets](docs/image-create-app-secrets.png "Create secrets")![Create secrets](docs/image-secret-value.png "Create Secrets")
1. Under **webapp** application, select **Authentication** -> **Add a platform**, select **web** platform, redirect urls set to `http://localhost:8080/login/oauth2/code/`, click **configure** button.![Add a platform](docs/image-add-a-platform.png "Add a platform")
1. Under **webapp** application, select **API permissions** -> **Add a permission**, select **Microsoft Graph**. Next, search `Directory.Read.All` via **select Permissions**, check the check box, click **add permissions** button.(`User.Read` is created automatically, we need to keep it.)![Request Api permission](docs/image-request-api-permissions.png "Request Api permission")
1. Similarly, add permission **user_impersonation** in **Azure Service Management**,
   ![Added permissions](docs/image-permissions.png "Added permissions")

See [Register app], [Grant scoped permission] for more information about web app.

### Configure groups for sign in user
In order to try the authorization action with this sample with minimum effort, [configure the user and groups in Azure Active Directory], configure the user with `group1`.

## Examples
### Configure application.yml
```yaml
azure:
  activedirectory:
    authorization-clients:
      arm:
        on-demand: true
        scopes: https://management.core.windows.net/user_impersonation
      graph:
        scopes:
          - https://graph.microsoft.com/User.Read
          - https://graph.microsoft.com/Directory.Read.All
    client-id: <client-id>
    client-secret: <client-secret>
    tenant-id: <tenant-id>
    user-group:
      allowed-groups: group1, group2
    post-logout-redirect-uri: http://localhost:8080
# It's suggested the logged in user should at least belong to one of the above groups
# If not, the logged in user will not be able to access any authorization controller rest APIs
```

### Run with Maven
```shell
cd azure-spring-boot-samples/azure-spring-boot-sample-active-directory-webapp
mvn spring-boot:run
```

### Check the authentication and authorization
1. Access http://localhost:8080
2. Login
3. Access `Group1 Message` link: success
4. Access `Group2 Message` link: fail with forbidden error message
5. Access `Graph Client` link: access token for `Microsoft Graph` will be acquired, and the content of customized **OAuth2AuthorizedClient** instance for `Microsoft Graph` resource will be displayed.
6. Access `Office Client` link: access token for `Office 365 Management APIs` will be acquired, the content of customized **OAuth2AuthorizedClient** instance for `Office 365 Management APIs` resource will be displayed.
7. Access `Arm Client` link: page will be redirected to Consent page for on-demand authorization of `user_impersonation` permission in `Azure Service Management` resource. Clicking on `Consent`, access token for `Azure Service Management` will be acquired, the content of customized **OAuth2AuthorizedClient** instance for `Azure Service Management` resource will be displayed.

## Troubleshooting
### If registered application is multi-tenanted, how to run this sample?
In your application.yml file:
```yaml
azure:
  activedirectory:
    tenant-id: common
```
---
### Meet with `AADSTS240002: Input id_token cannot be used as 'urn:ietf:params:oauth:grant-type:jwt-bearer' grant` error.
In Azure portal, app registration manifest page, configure `oauth2AllowImplicitFlow` in your application manifest to `true`. See [this issue] for details on this workaround.

## Next steps
## Contributing

<!-- LINKS -->
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[Register app]: https://docs.microsoft.com/azure/active-directory/develop/quickstart-register-app
[Grant scoped permission]: https://docs.microsoft.com/azure/active-directory/develop/quickstart-configure-app-access-web-apis
[configure the user and groups in Azure Active Directory]: https://docs.microsoft.com/azure/active-directory/active-directory-groups-create-azure-portal
[this issue]: https://github.com/MicrosoftDocs/azure-docs/issues/8121#issuecomment-387090099
