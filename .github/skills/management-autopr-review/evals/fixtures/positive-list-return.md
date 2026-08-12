# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-deviceprovisioningservices]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://example.com/release-plan

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-deviceprovisioningservices`
Package version: `1.2.0-beta.1`

Changed user-facing interface
`sdk/deviceprovisioningservices/azure-resourcemanager-deviceprovisioningservices/src/main/java/com/azure/resourcemanager/deviceprovisioningservices/models/DpsCertificates.java`:

```diff
- PagedIterable<CertificateResponse> list(String resourceGroupName, String provisioningServiceName);
+ CertificateListDescription list(String resourceGroupName, String provisioningServiceName);
```

`CertificateListDescription` is a response model containing the certificate
collection. The `list` return type changed in this pull request.
