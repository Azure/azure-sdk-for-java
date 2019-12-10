# Change Log azure-data-appconfiguration
## Version 1.0.0-preview.7 (2019-11-26)
For details on the Azure SDK for Java (November 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview7-java).

- Added support for Azure Activity Directory authentication.
- Added service API version support

#### Breaking Changes
- Removed clearReadOnly API, updated setReadOnly API to support setting and clearing read only based on the flag passed.
- Removed Range class, SettingSelector no longer supports Range.

## Version 1.0.0-preview.6 (2019-10-31)
For details on the Azure SDK for Java (October 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview6-java).
- Renamed addSetting, getSetting, deleteSetting, setSetting, listSettings, listSettingRevisions to
  addConfigurationSetting, getConfigurationSetting, deleteConfigurationSetting, setConfigurationSetting,
  listConfigurationSettings, listRevisions for consistency naming across languages.
- Ensured exceptions are consistent for certain operations (c.f. other languages).
- Renamed asOfDayTime to acceptDateTime, and lock to isReadOnly.
- ConfigurationCredentialsPolicy no longer explored to public and moved to implementation folder.
- Fixed AzConfig Revisions Range Returns 416 Status Code
- Added ConfigurationServiceVersion class for version
- Added more samples including conditional request, setReadOnly, clearReadOnly, listRevisions, etc.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 

## Version 1.0.0-preview.5 (2019-10-11)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-10-11/java.html).

- Fixed a explored bug that ConfigurationClientCredential is already pacakge-private. Using connection String instead.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## Version 1.0.0-preview.4 (2019-10-8)
For details on the Azure SDK for Java (October 2019 Preview) release refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-10-11/java.html).

- Updated addSetting, getSetting, deleteSetting, setSetting to support conditional request.
- Removed UpdateSetting.
- Allowed user to define custom equality of configuration setting.
- No public ConfigurationClientCredential.
- Removed credential and CredentialPolicy package.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## Version 1.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

- Removed dependency on Netty.
- Added logging when throwing `RutimeException`s.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## Version 1.0.0-preview.2 (2019-08-06)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

- Merged ConfigurationClientBuilder and ConfigurationAsyncClientBuilder into ConfigurationClientBuilder. Method to build each client were added.
- ConfigurationClientBuilder was made instantiable, static builder method removed from ConfigurationClient and ConfigurationAsyncClient.
- Builder method credentials renamed to credential and serviceEndpoint to endpoint.
- Listing operations return PagedFlux and PagedIterable in their respective clients.
- Asynchronous calls check subscriberContext for tracing context.
- Synchronous calls support passing tracing context in maximal overloads.

**Breaking changes: New API Design**
- Simplified API to return model types directly on non-maximal overloads. Maximal overloads return `Response<T>` and suffixed with WithResponse.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.2/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.2/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## Version 1.0.0-preview.1 (2019-06-28)
Version 1.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic 
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide 
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For details on the Azure SDK for Java (July 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.1/appconfiguration/client/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.1/appconfiguration/client/src/samples/java) 
demonstrate the new API.

- Initial release. Please see the README and wiki for information on the new design.
