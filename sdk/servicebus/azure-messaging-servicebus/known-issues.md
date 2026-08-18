# Azure Service Bus SDK known issues

This article lists known issues in Azure Service Bus SDK releases. The list is updated as new issues are identified.

### Can not resolve `BinaryData` or `NoClassDefFoundError` (version 7.0.0) 
NoClassDefFoundError When using `azure-messaging-servicebus:7.0.0` and other Azure SDKs in the same pom.xml file.

- **Applicable**: This issue applies to `azure-messaging-servicebus:7.0.0` when it is listed with other Azure SDKs in 
   the same pom.xml file.
- **Cause**: This error occurs due to a known `azure-core:1.11.0` dependency conflict between 
  `azure-messaging-servicebus:7.0.0` and other Azure SDKs, such as `azure-identity:1.2.0`, which depend on older 
  versions of azure-core. When two libraries depend on different versions of the same package, Maven will simply pick 
  the first version that it sees and ignores the others. This issue is resolved if you use `azure-identity:1.2.1`.
- **Remediation**: A workaround is to change the order of dependencies in the application's pom.xml file to list 
  `azure-messaging-servicebus` before any other `azure-*` client libraries. A Fix for the January 2021 release is being 
  tracked at https://github.com/Azure/azure-sdk-for-java/issues/17942.
- **Additional Information**: More information on diagnosing and resolving dependency conflicts can be found at
  https://github.com/Azure/azure-sdk-for-java/wiki/Frequently-Asked-Questions#im-getting-a-nosuchmethoderror-or-noclassdeffounderror.
