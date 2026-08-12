# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-deviceprovisioningservices]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-deviceprovisioningservices`
Package version: `1.2.0-beta.1`

New user-facing interface operation in
`sdk/deviceprovisioningservices/azure-resourcemanager-deviceprovisioningservices/src/main/java/com/azure/resourcemanager/deviceprovisioningservices/models/DpsCertificates.java`:

```diff
+ CertificateListDescription listCertificates(
+     String resourceGroupName, String provisioningServiceName);
```

`CertificateListDescription` is a response model containing the certificate
collection:

```java
List<CertificateResponse> value();
```

The `listCertificates` operation did not exist on the base branch.
