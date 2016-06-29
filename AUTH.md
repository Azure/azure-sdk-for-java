#Authentication in Azure Libraries for Java

To use the management APIs in the Azure Libraries for Java, as the first step, you need to 
create an authenticated client.

There are several approaches possible. This document illustrates the simplest.

## Creating a Service Principal in Azure

In order for your application not to require its user to login manually, you can acquire the credentials for a given subscription using the Azure Active Directory *service principal* functionality. A service principal is analogous to a user account, but it is intended applications to authenticate themselves without human intervention.

If you then save such service principal-based credentials as a file or in environment variables, this can simplify and speed up your coding process.

>:warning: Note: exercise caution when saving credentials in a file. Anyone that gains access to that file will have the same access privileges to Azure as your application.

You can create a service principal and give it access privileges for a given subscription using these steps:

1. Log into [your Azure account](http://portal.azure.com).
1. Select **Browse > Active Directory**.
  <br/>![Browse > Active Directory](/media/auth/browse-ad.png)
1. Select the active directory (if you have more than one listed) that you want to register your app in.
1. Click the **Applications** link.
1. Click the **Add** button at the bottom of the page.
  <br/>![Select AD tenant > Applications > Add](/media/auth/add.png)
1. Type in a name for your application. (After your app is registered, it will be listed under that name in the active directory you have selected earlier.)
1. Select the **Web application and/or web APIWeb application and/or web API** option, *regardless* of whether your application is actually going to run on the web or on a desktop computer, and click the arrow to go to the next step.
  <br/>![Name and Type](/media/auth/app.png)
1. Type in some URL in the **Sign-on URL**. If your application is not a web app, it does not matter what you type in here, as long as it is a syntactically correct URL format.
1. Type in some **App ID URI**. This is just a unique identifier for your app in the URI format that needs to be unique in your selected Active Directory
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
  `client=123456-abcd-1234-abcd-1234567890ab`
1. In the **Keys** section, select a duration. This will create a new client secret.
1. Click **Save** at the bottom of the page
  <br/>![Generate client secret](/media/auth/keys.png)
1. After a few seconds, you will see the generated key and a prompt for you to save it:
  <br/>![Save client secret](/media/auth/key-generated.png)
1. Copy the shown key into your text file and prefix it with "`key=`", for example:
  `key=01234567890123456789abcdef01234567890abcdef0123456789abcdef02345`
1. In the current URL shown in your browser, select the text between the words: "Directory/" and "/RegisteredApp" and copy it.
  <br/>![Tenant ID from URL](/media/auth/tenant-id.png)
1. Paste the copied value into your text file and prefix it with "`tenant=`", for example:
  `tenant=abcdef01-1234-dcba-9876-abcdef012345`
1. Assuming you are using public Azure cloud in the U.S., also add the following to your text file:
```
managementURI=https\://management.core.windows.net/
baseURL=https\://management.azure.com/
authURL=https\://login.windows.net/
```
> Note that this file follows the Java properties file format, so certain characters need to be escaped with a backslash (\), for example: colons (\:) in URLs. 
You can now save the file.
1. You need to grant the created service principal a permission to access the desired Azure subscription. Go to the [Azure portal](http://portal.azure.com) again.
1. Click **Subscriptions** and select the subscription in the list that you want to enable your application to access.
  <br/>![Subscriptions](/media/auth/subscriptions.png)
1. Click **Users**, **Add**
  <br/>![Users](/media/auth/users.png)

After creating the service principal, you should have three pieces of information, a client id (GUID), client secret (string) and tenant id (GUID) or domain name (string). By feeding them into the `ApplicationTokenCredentials` and initialize the ARM client with it, you should be ready to go.
