# Access to multi-tenant app

* Do not configure **tenantId** in the **application.properties/.yml** of a multi-tenant app or configure it to "common" to use the login process of multi-tenant access. Otherwise, users of other tenants will not be able to access the protected resources of the app.

* The administrator's consent is required for the first visit.

![need-admin-approval](resource/access-to-multi-tenant-app/need-admin-approval.png)

* Sign in with an administrator account and approve.
* Before accepting, make sure that **"Consent on behalf of your organization"** is checked.

![permissions-requested](resource/access-to-multi-tenant-app/permissions-requested.png)

* If you forget to check "Consent on behalf of your organization", there are two ways to revoke your consent.

* Users revoke access to individual applications by removing them from their [Access Panel Applications list](https://myapps.microsoft.com/).

![users-revoke-access-1](resource/access-to-multi-tenant-app/users-revoke-access-1.png)
![users-revoke-access-2](resource/access-to-multi-tenant-app/users-revoke-access-2.png)
![users-revoke-access-3](resource/access-to-multi-tenant-app/users-revoke-access-3.png)

* Administrators revoke access to applications by removing them using the Enterprise applications section of the [Azure portal](https://portal.azure.com/).

![administrators-revoke-access-1](resource/access-to-multi-tenant-app/administrators-revoke-access-1.png)
![administrators-revoke-access-2](resource/access-to-multi-tenant-app/administrators-revoke-access-2.png)
