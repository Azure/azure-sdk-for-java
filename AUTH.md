#Authentication in Azure Management Libraries for Java

To use the APIs in the Azure Management Libraries for Java, as the first step you need to 
create an authenticated client. There are several possible approaches to authentication. This document illustrates a couple of the simpler ones.

## Using an authentication file

> :warning: Note, file-based authentication is an experimental feature that may or may not be available in later releases. The file format it relies on is subject to change as well.

To create an authenticated Azure client:

```java
Azure azure = Azure.authenticate(new File("my.azureauth")).withDefaultSubscription();
```

The authentication file, referenced as "my.azureauth" in the example above, uses the Java properties file format and must contain the following information:
```
subscription=########-####-####-####-############
client=########-####-####-####-############
key=XXXXXXXXXXXXXXXX
tenant=########-####-####-####-############
managementURI=https\://management.core.windows.net/
baseURL=https\://management.azure.com/
authURL=https\://login.windows.net/
```

This approach enables unattended authentication for your application (i.e. no interactive user login, no token management needed). The `client`, `key` and `tenant` are from [your service principal registration](#creating-a-service-principal-in-azure). The `subscription` represents the subscription ID you want to use as the default subscription. The remaining URIs and URLs represent the end points for the needed Azure services, and the example above assumes you are using the Azure worldwide cloud.

## Using `ApplicationTokenCredentials`

Similarly to the [file-based approach](#using-an-authentication-file), this method requires a [service principal registration](#creating-a-service-principal-in-azure), but instead of storing the credentials in a local file, the required inputs can be supplied directly via an instance of the `ApplicationTokenCredentials` class:

```
ServiceClientCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AzureEnvironment.AZURE);
Azure azure = Azure.authenticate(credentials).withSubscription(subscriptionId);
```

where `client`, `tenant`, `key` and `subscriptionId` are strings with the required pieces of information about your service principal and subscription. The last parameter, `AzureEnvironment.AZURE` represents the Azure worldwide public cloud. You can use a different value out of the currently supported alternatives in the `AzureEnvironment` enum.

## Creating a Service Principal in Azure

In order for your application to log into your Azure subscription without requiring the user to log in manually, you can take advantage of credentials based on the Azure Active Directory *service principal* functionality. A service principal is analogous to a user account, but it is intended for applications to authenticate themselves without human intervention.

If you save such service principal-based credentials as a file, or store them in environment variables, this can simplify and speed up your coding process.

>:warning: Note: exercise caution when saving credentials in a file. Anyone that gains access to that file will have the same access privileges to Azure as your application. In general, file-based authentication is not recommended in production scenarios and should only be used as a quick shortcut to getting started in dev/test scenarios.

You can create a service principal and grant it access privileges for a given subscription by following these steps (or through [Azure PowerShell/Xplat CLI](https://azure.microsoft.com/en-us/documentation/articles/resource-group-authenticate-service-principal/)):

1. Log into [your Azure account](http://portal.azure.com).
1. Select **Browse > Active Directory**.
  <br/>![Browse > Active Directory](/media/auth/browse-ad.png)
1. Select the active directory (if you have more than one listed) that you want to register your app in.
1. Click the **Applications** link.
1. Click the **Add** button at the bottom of the page.
  <br/>![Select AD tenant > Applications > Add](/media/auth/add.png)
1. Type in a name for your application. After your app is registered, it will be listed under that name in the Active Directory instance you have selected earlier. You will need to reference that name in a later step.
1. Select the **Web application and/or web API** option, *regardless* of whether your application is actually going to run on the web or on a desktop computer, and click the arrow to go to the next step.
  <br/>![Name and Type](/media/auth/app.png)
1. One the next screen, type in some URL in the **Sign-on URL**. If your application is not a web app, it does not matter what you type in here, as long as it is a syntactically correct URL format.
1. Type in some **App ID URI**. This is just a unique identifier of your choice for your app in the proper URI format that needs to be unique in your selected Active Directory instance.
1. Click the checkmark when done.<br>
  <br/>![Application properties](/media/auth/app-props.png)
1. Wait for the task to complete. You may see a notification like the following in the meantime:
  <br/>![Adding...](/media/auth/adding.png)
1. When you see the app dashboard page, click **Configure**.
  <br/>![Application added](/media/auth/added.png)
1. Scroll down the page till you see **Client ID** and **Keys**
  <br/>![Client ID and Keys](/media/auth/client-id.png)
1. Create a new blank text file to put your credential information into, and save it as - for example - **"my.azureauth"**
1. Copy the **client ID** value into your text file, typing "`client=`" in front of it, for example:
  <br>`client=123456-abcd-1234-abcd-1234567890ab`
1. In the **Keys** section, select a duration.
1. Click **Save** at the bottom of the page
  <br/>![Generate client secret](/media/auth/keys.png)
1. After a few seconds, you will see the generated key and a prompt for you to save it:
  <br/>![Save client secret](/media/auth/key-generated.png)
1. Copy the shown key into your text file and prefix it with "`key=`", for example:
  <br>`key=01234567890123456789abcdef01234567890abcdef0123456789abcdef02345`
1. In the current URL shown in your browser, select the text between the word: "Directory/" and the next slash (/) and copy it. This the ID of the active directory (tenant) where your application is being registered.
  <br/>![Tenant ID](/media/auth/tenant-id.png)
1. Paste the copied value into your text file and prefix it with "`tenant=`", for example:
  <br>`tenant=72f988bf-86f1-41af-91ab-2d7cd011db47`
  <br>This represents the Active Directory instance you selected earlier.
1. Assuming you are using the Azure worldwide public cloud, also add the following to your text file: \(Note that this file follows the Java properties file format, so certain characters, such as colons, need to be escaped with a backslash\)<br/>
    `managementURI=https\://management.core.windows.net/`<br/>
    `baseURL=https\://management.azure.com/`</br>
    `authURL=https\://login.windows.net/`<br/>
  Make sure to escape the colons (:) with backslashes (\\).
1. You need to grant the created service principal a permission to access the desired Azure subscription. Go to the [Azure portal](http://portal.azure.com) again.
1. Click **Subscriptions** and select the subscription in the list that you want to enable your application to access.
  <br/>![Subscriptions](/media/auth/subscriptions.png)
1. Click **Users**, **Add**
  <br/>![Users](/media/auth/users.png)
1. In the **Add Access** page, for the **Select a role** question, select **Owner** if you want to give your application the ability to modify resources in this subscription.
  <br/>![Access](/media/auth/access.png)
1. Click **Add Users**, then find and select your application using the name you provided earlier, and click the **Select** button near the bottom of the page.
  <br/>![Add user](/media/auth/add-user.png)

Now all the pieces are in place to enable authenticating your code without requiring an interactive login nor the need to manage access tokens.

