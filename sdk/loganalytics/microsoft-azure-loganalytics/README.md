# Azure Log Analytics

This project provides client tools or utilities in Java that make it easy to query data in [Azure Log Analytics](https://azure.microsoft.com/services/log-analytics/). For reference documentation on classes and models, please see the [Azure SDK for Java reference](https://docs.microsoft.com/java/api/overview/azure/?view=azure-java-stable). 

Azure Log Analytics provides agents for telemtry collection and enables deep analytics via a [rich query language](https://docs.microsoft.com/azure/data-explorer/kusto/query/). This SDK provides query access to data already stored in Log Analytics. To start collecting data from different sources, take a look at these [quickstarts](https://docs.microsoft.com/azure/log-analytics/log-analytics-quick-collect-azurevm). 

## Examples

Please see [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/loganalytics/microsoft-azure-loganalytics/samples) for code examples using this SDK. 


## Download

### Latest release

To get the binaries of the official Microsoft Azure Log Analytics SDK as distributed by Microsoft, reade for use within your project, you can use Maven.

[//]: # ({x-version-update-start;com.microsoft.azure:azure-loganalytics;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-loganalytics</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Prerequisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven

## Help and Issues

If you encounter any bugs with these SDKs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contribute Code

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## More information
- [Azure Java SDKs](https://docs.microsoft.com/java/azure/)
- If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](https://go.microsoft.com/fwlink/?LinkId=330212)

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Floganalytics%2Fmicrosoft-azure-loganalytics%2FREADME.png)
