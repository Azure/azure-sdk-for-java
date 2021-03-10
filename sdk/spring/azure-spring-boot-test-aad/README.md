# Azure Spring Boot Integration tests client library for Java

## Key concepts
## Getting started

### How to run AADConditionalAccessIT. 

#####  What is AAD Conditional Access?

Azure Active Directory is a token-based authentication platform, [On-Behalf-Of flow] is the middle-tier service to make authenticated requests to the downstream service through token.
[Conditional Access] is the tool used by Azure Active Directory to bring signals together, to make decisions, and enforce organizational policies. 
In some cases, Conditional Access may cause the token get from middle-tier service was useless.![Policy Flow](docs/image-conditional-access-flow.png)

##### Create applications and configure it.
1. First, we need create three applications. The registered application name is ***webapp***, ***webapiA*** and ***webapiB***.![Application Name](docs/image-application-name.png)
2. Under ***webapiA*** and ***webapiB*** application, select Expose an API -> Add a scope. Then set the Scope name to File.Read. click Add scope button.![API Permissions](docs/image-webapiA-add-scope.png) ![API Permissions](docs/image-webapiB-add-scope.png)
3. After creating the scope. Expose scopes for ***webapp*** and ***webapiA***. Select API permissions > Add a permission > My APIs, select ***webapiA*** or ***webapiB*** application name.![Select MyAPIs](docs/image-webapp-select-myapis.png) ![Select MyAPIs](docs/image-webapiA-select-myapis.png)
4. **Delegated permissions** is selected by default， Select **File** > **File.Read** permission, select **Add permission** to complete the process.![Add Permissions](docs/image-webapp-add-permissions.png) ![Add Permissions](docs/image-webapiA-add-permissions.png)
5. Grant admin consent for ***webapiA*** and ***webapiB*** permissions.![API Permissions](docs/image-webapp-add-grant-admin-consent.png) ![API Permissions](docs/image-webapiA-add-grant-admin-consent.png)


##### Config Conditional Access Policy.
1. Open the home of azure -> Select Security. ![Select Permissions](docs/image-conditional-access-home.png)
2. Select Conditional Access button.![Select Button](docs/image-conditional-access-button.png)
3. Then create a new policy.![Create Policy](docs/image-conditional-access-new-policy.png)
4. We need to prepare a user or a group. As below, we need to configure users or groups to follow this policy.![Add User](docs/image-conditional-access-add-user.png)
5. As before, we need to configure one or more applications to follow this policy. In our case, we need to configure the ***webapiB*** application.![Add Application](docs/image-conditional-access-add-application.png)
6. In our case, we use [Multi-Factor Authentication] as Conditional Access Policy. So select `Require multi-factor authentication` in ***Grant***.![API Permissions](docs/image-conditional-access-add-MFA.png)
7. Finally, enable it.![Enable Button](docs/image-conditional-access-enable.png)

##### Run AADConditionalAccessIT

This case is difficult to run automatically, we need to execute it manually. 
Here are the steps to start this case.

1. Prepare application and Conditional Access Policy as above.
2. Prepare environment variables for `AADConditionalAccessResourceServerIT`(***azure-spring-boot-test-aad-resource-server***). The environment variables are the parameters of our ***webapiB*** application and make sure `APPLICATION_SERVER_PORT` is 8082.
3. Same as `AADConditionalAccessResourceServerIT`, The environment variables of `AADConditionalAccessOboServerIT`(***azure-spring-boot-test-aad-obo***) is the parameters of our ***webapiA*** application and  make sure `APPLICATION_SERVER_PORT` is 8081.
4. At last, Prepare the environment variables of `AADConditionalAccessIT` through the parameters of ***webapp*** application.
5. Start the `conditionalAccessTest()` method.


## Examples
## Troubleshooting
## Next steps
## Contributing

<!-- LINKS -->
[On-Behalf-Of flow]: https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow
[Multi-Factor Authentication]: https://docs.microsoft.com/en-us/azure/active-directory/authentication/tutorial-enable-azure-mfa
[Conditional Access]: https://docs.microsoft.com/en-us/azure/active-directory/conditional-access/overview