# Single-Service Packages Latest Releases

The single-service packages provide easy-to-use APIs for each Azure service following the design principals of [Azure Management Libraries for Java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager). If you have specific requirement on certain service API version, you may find appropriate package below. If not, you could always choose the latest release.

According to [Azure REST API reference](https://docs.microsoft.com/rest/api/azure/), most request URIs of Azure services require `api-version` as the query-string parameter. All supported API versions for each Azure service can be found via [Azure REST API Specifications](https://github.com/Azure/azure-rest-api-specs/tree/main/specification). For your convenience, we provide more details of the published packages by format below.

```
service
* package-tag
    * maven.version
```

- `service` for Azure service.
- `package-tag` for included resources with API versions.
- `maven.version` for maven version of the artifact.


<br/>
<details>
<summary> advisor </summary>

* [package-2020-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/advisor/resource-manager#tag-package-2020-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-advisor/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> apimanagement </summary>

* [package-2021-08](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/apimanagement/resource-manager#tag-package-2021-08)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-apimanagement/1.0.0-beta.3/jar)
* [package-2020-12](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/apimanagement/resource-manager#tag-package-2020-12)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-apimanagement/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-apimanagement/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> appconfiguration </summary>

* [package-2021-10-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/appconfiguration/resource-manager#tag-package-2021-10-01-preview)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appconfiguration/1.0.0-beta.5/jar)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appconfiguration/1.0.0-beta.4/jar)
* [package-2021-03-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/appconfiguration/resource-manager#tag-package-2021-03-01-preview)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appconfiguration/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appconfiguration/1.0.0-beta.2/jar)
* [package-2020-06-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/appconfiguration/resource-manager#tag-package-2020-06-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appconfiguration/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> appcontainers </summary>

* [package-2022-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/app/resource-manager#tag-package-2022-03)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appcontainers/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appcontainers/1.0.0-beta.2/jar)
* [package-2022-01-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/app/resource-manager#tag-package-2022-01-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-appcontainers/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> applicationinsights </summary>

* [package-2022-02-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/applicationinsights/resource-manager#tag-package-2022-02-01)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-applicationinsights/1.0.0-beta.4/jar)
* [package-2022-01-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/applicationinsights/resource-manager#tag-package-2022-01-11)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-applicationinsights/1.0.0-beta.3/jar)
* [package-2021-11-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/applicationinsights/resource-manager#tag-package-2021-11-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-applicationinsights/1.0.0-beta.2/jar)
* [package-2020-10-20](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/applicationinsights/resource-manager#tag-package-2020-10-20)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-applicationinsights/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> attestation </summary>

* [package-2020-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/attestation/resource-manager#tag-package-2020-10-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-attestation/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> automation </summary>

* [package-2019-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/automation/resource-manager#tag-package-2019-06)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-automation/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> avs </summary>

* [package-2021-12-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/vmware/resource-manager#tag-package-2021-12-01)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-avs/1.0.0-beta.3/jar)
* [package-2021-06-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/vmware/resource-manager#tag-package-2021-06-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-avs/1.0.0-beta.2/jar)
* [package-2020-03-20](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/vmware/resource-manager#tag-package-2020-03-20)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-avs/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> azurearcdata </summary>

* [package-2021-08-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azurearcdata/resource-manager#tag-package-2021-08-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-azurearcdata/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-azurearcdata/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> azurestack </summary>

* [package-preview-2020-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azurestack/resource-manager#tag-package-preview-2020-06)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-azurestack/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> azurestackhci </summary>

* [package-2022-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azurestackhci/resource-manager#tag-package-2022-05)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-azurestackhci/1.0.0-beta.3/jar)
* [package-2022-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azurestackhci/resource-manager#tag-package-2022-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-azurestackhci/1.0.0-beta.2/jar)
* [package-2020-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azurestackhci/resource-manager#tag-package-2020-10)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-azurestackhci/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> baremetalinfrastructure </summary>

* [package-2021-08-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/baremetalinfrastructure/resource-manager#tag-package-2021-08-09)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-baremetalinfrastructure/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> batch </summary>

* [package-2022-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/batch/resource-manager#tag-package-2022-01)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-batch/1.0.0/jar)
* [package-2021-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/batch/resource-manager#tag-package-2021-06)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-batch/1.0.0-beta.2/jar)
* [package-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/batch/resource-manager#tag-package-2021-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-batch/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> batchai </summary>

* package-2018-05
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-batchai/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> billing </summary>

* [package-2020-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/billing/resource-manager#tag-package-2020-05)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-billing/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-billing/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> botservice </summary>

* [package-preview-2021-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/botservice/resource-manager#tag-package-preview-2021-05)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-botservice/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-botservice/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-botservice/1.0.0-beta.2/jar)
* [package-2021-03-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/botservice/resource-manager#tag-package-2021-03-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-botservice/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> changeanalysis </summary>

* [package-2021-04-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/changeanalysis/resource-manager#tag-package-2021-04-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-changeanalysis/1.0.0-beta.1/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-changeanalysis/1.0.0/jar)
</details>

<br/>
<details>
<summary> cognitiveservices </summary>

* [package-2022-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cognitiveservices/resource-manager#tag-package-2022-03)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-cognitiveservices/1.0.0-beta.4/jar)
* [package-2021-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cognitiveservices/resource-manager#tag-package-2021-10)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-cognitiveservices/1.0.0-beta.3/jar)
* [package-2021-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cognitiveservices/resource-manager#tag-package-2021-04)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-cognitiveservices/1.0.0-beta.2/jar)
* [package-2017-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cognitiveservices/resource-manager#tag-package-2017-04)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-cognitiveservices/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> commerce </summary>

* [package-2015-06-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/commerce/resource-manager#tag-package-2015-06-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-commerce/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> communication </summary>

* [package-2020-08-20](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/communication/resource-manager#tag-package-2020-08-20)
    * [1.1.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-communication/1.1.0-beta.1/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-communication/1.0.0-beta.1/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-communication/1.0.0/jar)
</details>

<br/>
<details>
<summary> confidentialledger </summary>

* [package-2022-05-13](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/confidentialledger/resource-manager#tag-package-2022-05-13)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-confidentialledger/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> confluent </summary>

* [package-preview-2021-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/confluent/resource-manager#tag-package-preview-2021-09)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-confluent/1.0.0-beta.3/jar)
* [package-2021-03-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/confluent/resource-manager#tag-package-2021-03-01-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-confluent/1.0.0-beta.2/jar)
* [package-2020-03-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/confluent/resource-manager#tag-package-2020-03-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-confluent/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> consumption </summary>

* [package-2021-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/consumption/resource-manager#tag-package-2021-10)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-consumption/1.0.0-beta.3/jar)
* [package-2019-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/consumption/resource-manager#tag-package-2019-10)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-consumption/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-consumption/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> costmanagement </summary>

* [package-2020-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cost-management/resource-manager#tag-package-2020-06)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-costmanagement/1.0.0-beta.1/jar)
* [package-2019-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cost-management/resource-manager#tag-package-2019-11)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-costmanagement/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-costmanagement/1.0.0-beta.2/jar)
</details>

<br/>
<details>
<summary> customerinsights </summary>

* [package-2017-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/customer-insights/resource-manager#tag-package-2017-04)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-customerinsights/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> dashboard </summary>

* [package-2021-09-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/dashboard/resource-manager#tag-package-2021-09-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-dashboard/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> databox </summary>

* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/databox/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-databox/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> databoxedge </summary>

* [package-2019-08](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/databoxedge/resource-manager#tag-package-2019-08)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-databoxedge/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> databricks </summary>

* [package-2021-04-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/databricks/resource-manager#tag-package-2021-04-01-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-databricks/1.0.0-beta.2/jar)
* [package-2018-04-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/databricks/resource-manager#tag-package-2018-04-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-databricks/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> datadog </summary>

* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/datadog/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datadog/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datadog/1.0.0-beta.2/jar)
* [package-2020-02-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/datadog/resource-manager#tag-package-2020-02-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datadog/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> datafactory </summary>

* [package-2018-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/datafactory/resource-manager#tag-package-2018-06)
    * [1.0.0-beta.9](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.9/jar)
    * [1.0.0-beta.8](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.8/jar)
    * [1.0.0-beta.7](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.7/jar)
    * [1.0.0-beta.6](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.6/jar)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.5/jar)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.2/jar)
    * [1.0.0-beta.15](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.15/jar)
    * [1.0.0-beta.14](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.14/jar)
    * [1.0.0-beta.13](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.13/jar)
    * [1.0.0-beta.12](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.12/jar)
    * [1.0.0-beta.11](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.11/jar)
    * [1.0.0-beta.10](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.10/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datafactory/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> datalakeanalytics </summary>

* [package-2016-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/datalake-analytics/resource-manager#tag-package-2016-11)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datalakeanalytics/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> datalakestore </summary>

* [package-2016-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/datalake-store/resource-manager#tag-package-2016-11)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datalakestore/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> datamigration </summary>

* [package-2018-04-19](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/datamigration/resource-manager#tag-package-2018-04-19)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-datamigration/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> dataprotection </summary>

* [package-2021-07](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/dataprotection/resource-manager#tag-package-2021-07)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-dataprotection/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> delegatednetwork </summary>

* [package-2021-03-15](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/dnc/resource-manager#tag-package-2021-03-15)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-delegatednetwork/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> deploymentmanager </summary>

* [package-2019-11-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/deploymentmanager/resource-manager#tag-package-2019-11-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-deploymentmanager/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> desktopvirtualization </summary>

* [package-preview-2021-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/desktopvirtualization/resource-manager#tag-package-preview-2021-09)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-desktopvirtualization/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> deviceprovisioningservices </summary>

* [package-2021-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/deviceprovisioningservices/resource-manager#tag-package-2021-10)
    * [1.1.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-deviceprovisioningservices/1.1.0-beta.1/jar)
* [package-2020-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/deviceprovisioningservices/resource-manager#tag-package-2020-03)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-deviceprovisioningservices/1.0.0/jar)
</details>

<br/>
<details>
<summary> deviceupdate </summary>

* [package-2022-04-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/deviceupdate/resource-manager#tag-package-2022-04-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-deviceupdate/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> devspaces </summary>

* [package-2019-04-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/devspaces/resource-manager#tag-package-2019-04-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-devspaces/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> devtestlabs </summary>

* [package-2018-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/devtestlabs/resource-manager#tag-package-2018-09)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-devtestlabs/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> digitaltwins </summary>

* [package-2021-06-30-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/digitaltwins/resource-manager#tag-package-2021-06-30-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-digitaltwins/1.0.0-beta.2/jar)
* [package-2020-12](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/digitaltwins/resource-manager#tag-package-2020-12)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-digitaltwins/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> dnsresolver </summary>

* [package-2020-04-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/dnsresolver/resource-manager#tag-package-2020-04-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-dnsresolver/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> dynatrace </summary>

* [package-2021-09-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/dynatrace/resource-manager#tag-package-2021-09-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-dynatrace/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> edgeorder </summary>

* [package-2021-12](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/edgeorder/resource-manager#tag-package-2021-12)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-edgeorder/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> elastic </summary>

* [package-2020-07-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/elastic/resource-manager#tag-package-2020-07-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-elastic/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> eventgrid </summary>

* [package-2021-12](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/eventgrid/resource-manager#tag-package-2021-12)
    * [1.1.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.1.0-beta.5/jar)
    * [1.1.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.1.0-beta.4/jar)
    * [1.1.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.1.0-beta.3/jar)
    * [1.1.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.1.0/jar)
* [package-2021-10-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/eventgrid/resource-manager#tag-package-2021-10-preview)
    * [1.2.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.2.0-beta.2/jar)
    * [1.2.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.2.0-beta.1/jar)
* [package-2021-06-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/eventgrid/resource-manager#tag-package-2021-06-preview)
    * [1.1.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.1.0-beta.2/jar)
    * [1.1.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.1.0-beta.1/jar)
* [package-2020-10-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/eventgrid/resource-manager#tag-package-2020-10-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.0.0-beta.2/jar)
* [package-2020-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/eventgrid/resource-manager#tag-package-2020-06)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.0.0-beta.1/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-eventgrid/1.0.0/jar)
</details>

<br/>
<details>
<summary> extendedlocation </summary>

* [package-2021-08-15](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/extendedlocation/resource-manager#tag-package-2021-08-15)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-extendedlocation/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> frontdoor </summary>

* [package-2020-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/frontdoor/resource-manager#tag-package-2020-11)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-frontdoor/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> hanaonazure </summary>

* [package-2017-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hanaonazure/resource-manager#tag-package-2017-11)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hanaonazure/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> hardwaresecuritymodules </summary>

* [package-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hardwaresecuritymodules/resource-manager#tag-package-2021-11)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hardwaresecuritymodules/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> hdinsight </summary>

* [package-2021-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hdinsight/resource-manager#tag-package-2021-06)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hdinsight/1.0.0-beta.5/jar)
* [package-2018-06-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hdinsight/resource-manager#tag-package-2018-06-preview)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hdinsight/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hdinsight/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hdinsight/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hdinsight/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> healthbot </summary>

* [package-2020-12-08](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/healthbot/resource-manager#tag-package-2020-12-08)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-healthbot/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> healthcareapis </summary>

* [package-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/healthcareapis/resource-manager#tag-package-2021-11)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-healthcareapis/1.0.0-beta.2/jar)
* [package-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/healthcareapis/resource-manager#tag-package-2021-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-healthcareapis/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> hybridcompute </summary>

* [package-preview-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hybridcompute/resource-manager#tag-package-preview-2021-03)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hybridcompute/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> hybridkubernetes </summary>

* [package-2021-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hybridkubernetes/resource-manager#tag-package-2021-10-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hybridkubernetes/1.0.0-beta.2/jar)
* [package-2021-03-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hybridkubernetes/resource-manager#tag-package-2021-03-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hybridkubernetes/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> hybridnetwork </summary>

* [package-2021-05-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/hybridnetwork/resource-manager#tag-package-2021-05-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-hybridnetwork/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> imagebuilder </summary>

* [package-2021-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/imagebuilder/resource-manager#tag-package-2021-10)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-imagebuilder/1.0.0-beta.2/jar)
* [package-2020-02](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/imagebuilder/resource-manager#tag-package-2020-02)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-imagebuilder/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> iotcentral </summary>

* [package-preview-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iotcentral/resource-manager#tag-package-preview-2021-11)
    * [1.1.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iotcentral/1.1.0-beta.1/jar)
* [package-2021-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iotcentral/resource-manager#tag-package-2021-06)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iotcentral/1.0.0-beta.2/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iotcentral/1.0.0/jar)
* [package-2018-09-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iotcentral/resource-manager#tag-package-2018-09-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iotcentral/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> iothub </summary>

* [package-2021-07-02](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iothub/resource-manager#tag-package-2021-07-02)
    * [1.2.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iothub/1.2.0-beta.1/jar)
* [package-2021-07](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iothub/resource-manager#tag-package-2021-07)
    * [1.1.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iothub/1.1.0/jar)
* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iothub/resource-manager#tag-package-2021-03)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iothub/1.0.0/jar)
* [package-2020-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/iothub/resource-manager#tag-package-2020-03)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iothub/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-iothub/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> kubernetesconfiguration </summary>

* [package-2022-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/kubernetesconfiguration/resource-manager#tag-package-2022-03)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kubernetesconfiguration/1.0.0-beta.3/jar)
* [package-2021-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/kubernetesconfiguration/resource-manager#tag-package-2021-09)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kubernetesconfiguration/1.0.0-beta.2/jar)
* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/kubernetesconfiguration/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kubernetesconfiguration/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> kusto </summary>

* [package-2022-02](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azure-kusto/resource-manager#tag-package-2022-02)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kusto/1.0.0-beta.4/jar)
* [package-2021-08-27](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azure-kusto/resource-manager#tag-package-2021-08-27)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kusto/1.0.0-beta.3/jar)
* [package-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azure-kusto/resource-manager#tag-package-2021-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kusto/1.0.0-beta.2/jar)
* [package-2020-09-18](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/azure-kusto/resource-manager#tag-package-2020-09-18)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-kusto/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> labservices </summary>

* [package-preview-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/labservices/resource-manager#tag-package-preview-2021-11)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-labservices/1.0.0-beta.2/jar)
* [package-2018-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/labservices/resource-manager#tag-package-2018-10)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-labservices/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> loadtestservice </summary>

* [package-2021-12-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/loadtestservice/resource-manager#tag-package-2021-12-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-loadtestservice/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> loganalytics </summary>

* [package-2020-08](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/operationalinsights/resource-manager#tag-package-2020-08)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-loganalytics/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-loganalytics/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> logic </summary>

* [package-2019-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/logic/resource-manager#tag-package-2019-05)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-logic/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> logz </summary>

* [package-2020-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/logz/resource-manager#tag-package-2020-10-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-logz/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> machinelearningservices </summary>

* [package-2021-04-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/machinelearningservices/resource-manager#tag-package-2021-04-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-machinelearningservices/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> maintenance </summary>

* [package-2021-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/maintenance/resource-manager#tag-package-2021-05)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-maintenance/1.0.0-beta.2/jar)
* [package-2020-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/maintenance/resource-manager#tag-package-2020-04)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-maintenance/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> managedapplications </summary>

* [package-managedapplications-2018-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/resources/resource-manager#tag-package-managedapplications-2018-06)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-managedapplications/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> maps </summary>

* [package-2021-02](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/maps/resource-manager#tag-package-2021-02)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-maps/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> mariadb </summary>

* [package-2020-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mariadb/resource-manager#tag-package-2020-01-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mariadb/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> marketplaceordering </summary>

* [package-2021-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/marketplaceordering/resource-manager#tag-package-2021-01-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-marketplaceordering/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-marketplaceordering/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> mediaservices </summary>

* [package-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mediaservices/resource-manager#tag-package-2021-11)
    * [2.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/2.0.0/jar)
    * [1.1.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/1.1.0-beta.3/jar)
* [package-2021-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mediaservices/resource-manager#tag-package-2021-06)
    * [1.1.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/1.1.0-beta.2/jar)
* [package-2021-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mediaservices/resource-manager#tag-package-2021-05)
    * [1.1.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/1.1.0-beta.1/jar)
* [package-2020-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mediaservices/resource-manager#tag-package-2020-05)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/1.0.0-beta.1/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mediaservices/1.0.0/jar)
</details>

<br/>
<details>
<summary> mixedreality </summary>

* [package-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mixedreality/resource-manager#tag-package-2021-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mixedreality/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> mobilenetwork </summary>

* [package-2022-03-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mobilenetwork/resource-manager#tag-package-2022-03-01-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mobilenetwork/1.0.0-beta.2/jar)
* [package-2022-01-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mobilenetwork/resource-manager#tag-package-2022-01-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mobilenetwork/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> mysql </summary>

* [package-2020-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mysql/resource-manager#tag-package-2020-01-01)
    * [1.0.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mysql/1.0.2/jar)
    * [1.0.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mysql/1.0.1/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mysql/1.0.0-beta.1/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mysql/1.0.0/jar)
</details>

<br/>
<details>
<summary> mysqlflexibleserver </summary>

* [package-flexibleserver-2021-05-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/mysql/resource-manager#tag-package-flexibleserver-2021-05-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mysqlflexibleserver/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-mysqlflexibleserver/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> netapp </summary>

* [package-netapp-2021-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2021-10-01)
    * [1.0.0-beta.8](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.8/jar)
* [package-netapp-2021-08-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2021-08-01)
    * [1.0.0-beta.7](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.7/jar)
* [package-netapp-2021-06-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2021-06-01)
    * [1.0.0-beta.6](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.6/jar)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.5/jar)
* [package-netapp-2021-04-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2021-04-01)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.4/jar)
* [package-netapp-2021-02-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2021-02-01)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.3/jar)
* [package-netapp-2020-12-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2020-12-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.2/jar)
* [package-netapp-2020-11-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/netapp/resource-manager#tag-package-netapp-2020-11-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-netapp/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> notificationhubs </summary>

* [package-2017-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/notificationhubs/resource-manager#tag-package-2017-04)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-notificationhubs/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-notificationhubs/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-notificationhubs/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> oep </summary>

* [package-2021-06-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/oep/resource-manager#tag-package-2021-06-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-oep/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> operationsmanagement </summary>

* [package-2015-11-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/operationsmanagement/resource-manager#tag-package-2015-11-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-operationsmanagement/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> peering </summary>

* [package-2021-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/peering/resource-manager#tag-package-2021-01-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-peering/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> policyinsights </summary>

* [package-2021-10](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/policyinsights/resource-manager#tag-package-2021-10)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-policyinsights/1.0.0-beta.2/jar)
* [package-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/policyinsights/resource-manager#tag-package-2021-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-policyinsights/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> postgresql </summary>

* [package-2020-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/postgresql/resource-manager#tag-package-2020-01-01)
    * [1.0.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresql/1.0.2/jar)
    * [1.0.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresql/1.0.1/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresql/1.0.0-beta.1/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresql/1.0.0/jar)
</details>

<br/>
<details>
<summary> postgresqlflexibleserver </summary>

* [package-flexibleserver-2021-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/postgresql/resource-manager#tag-package-flexibleserver-2021-06)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresqlflexibleserver/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresqlflexibleserver/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresqlflexibleserver/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-postgresqlflexibleserver/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> powerbidedicated </summary>

* [package-2021-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/powerbidedicated/resource-manager#tag-package-2021-01-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-powerbidedicated/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> purview </summary>

* [package-2021-07-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/purview/resource-manager#tag-package-2021-07-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-purview/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> quota </summary>

* [package-2021-03-15-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/quota/resource-manager#tag-package-2021-03-15-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-quota/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-quota/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> recoveryservices </summary>

* [package-2016-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/recoveryservices/resource-manager#tag-package-2016-06)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-recoveryservices/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> recoveryservicesbackup </summary>

* [package-2022-02](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/recoveryservicesbackup/resource-manager#tag-package-2022-02)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-recoveryservicesbackup/1.0.0-beta.5/jar)
* [package-2021-12](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/recoveryservicesbackup/resource-manager#tag-package-2021-12)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-recoveryservicesbackup/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-recoveryservicesbackup/1.0.0-beta.3/jar)
* [package-2021-07](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/recoveryservicesbackup/resource-manager#tag-package-2021-07)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-recoveryservicesbackup/1.0.0-beta.2/jar)
* [package-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/recoveryservicesbackup/resource-manager#tag-package-2021-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-recoveryservicesbackup/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> redisenterprise </summary>

* [package-preview-2021-02](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/redisenterprise/resource-manager#tag-package-preview-2021-02)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-redisenterprise/1.0.0-beta.1/jar)
* [package-2022-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/redisenterprise/resource-manager#tag-package-2022-01)
    * [1.1.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-redisenterprise/1.1.0-beta.1/jar)
* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/redisenterprise/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-redisenterprise/1.0.0-beta.2/jar)
    * [1.0.0](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-redisenterprise/1.0.0/jar)
</details>

<br/>
<details>
<summary> relay </summary>

* [package-2017-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/relay/resource-manager#tag-package-2017-04)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-relay/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> resourcegraph </summary>

* [package-preview-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/resourcegraph/resource-manager#tag-package-preview-2021-03)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-resourcegraph/1.0.0-beta.2/jar)
* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/resourcegraph/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-resourcegraph/1.0.0-beta.3/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-resourcegraph/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> resourcehealth </summary>

* [package-2020-05-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/resourcehealth/resource-manager#tag-package-2020-05-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-resourcehealth/1.0.0-beta.2/jar)
* [package-2018-07-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/resourcehealth/resource-manager#tag-package-2018-07-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-resourcehealth/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> resourcemover </summary>

* [package-2021-01-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/resourcemover/resource-manager#tag-package-2021-01-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-resourcemover/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> scvmm </summary>

* [package-2020-06-05-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/scvmm/resource-manager#tag-package-2020-06-05-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-scvmm/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> security </summary>

* [package-composite-v3](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/security/resource-manager#tag-package-composite-v3)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-security/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> securityinsights </summary>

* [package-preview-2022-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/securityinsights/resource-manager#tag-package-preview-2022-01)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-securityinsights/1.0.0-beta.3/jar)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-securityinsights/1.0.0-beta.2/jar)
* [package-preview-2021-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/securityinsights/resource-manager#tag-package-preview-2021-09)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-securityinsights/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> servicefabric </summary>

* [package-2021-06](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/servicefabric/resource-manager#tag-package-2021-06)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-servicefabric/1.0.0-beta.2/jar)
* [package-2019-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/servicefabric/resource-manager#tag-package-2019-03)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-servicefabric/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> servicelinker </summary>

* [package-2022-05-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/servicelinker/resource-manager#tag-package-2022-05-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-servicelinker/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-servicelinker/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> signalr </summary>

* [package-2022-02-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/signalr/resource-manager#tag-package-2022-02-01)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-signalr/1.0.0-beta.4/jar)
* [package-2021-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/signalr/resource-manager#tag-package-2021-10-01)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-signalr/1.0.0-beta.3/jar)
* [package-2021-06-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/signalr/resource-manager#tag-package-2021-06-01-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-signalr/1.0.0-beta.2/jar)
* [package-2021-04-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/signalr/resource-manager#tag-package-2021-04-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-signalr/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> sqlvirtualmachine </summary>

* [package-preview-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/sqlvirtualmachine/resource-manager#tag-package-preview-2021-11)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-sqlvirtualmachine/1.0.0-beta.2/jar)
* [package-2017-03-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/sqlvirtualmachine/resource-manager#tag-package-2017-03-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-sqlvirtualmachine/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> storagecache </summary>

* [package-2022-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storagecache/resource-manager#tag-package-2022-01)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storagecache/1.0.0-beta.5/jar)
* [package-2021-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storagecache/resource-manager#tag-package-2021-09)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storagecache/1.0.0-beta.4/jar)
* [package-2021-05](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storagecache/resource-manager#tag-package-2021-05)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storagecache/1.0.0-beta.3/jar)
* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storagecache/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storagecache/1.0.0-beta.2/jar)
* [package-2020-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storagecache/resource-manager#tag-package-2020-10-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storagecache/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> storageimportexport </summary>

* [package-preview-2021-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storageimportexport/resource-manager#tag-package-preview-2021-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storageimportexport/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> storagepool </summary>

* [package-2021-08-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/storagepool/resource-manager#tag-package-2021-08-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-storagepool/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> streamanalytics </summary>

* [package-pure-2020-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/streamanalytics/resource-manager#tag-package-pure-2020-03)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-streamanalytics/1.0.0-beta.2/jar)
* [package-2020-03-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/streamanalytics/resource-manager#tag-package-2020-03-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-streamanalytics/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> subscription </summary>

* [package-2020-09](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/subscription/resource-manager#tag-package-2020-09)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-subscription/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> support </summary>

* [package-2020-04](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/support/resource-manager#tag-package-2020-04)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-support/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> synapse </summary>

* [package-composite-v2](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/synapse/resource-manager#tag-package-composite-v2)
    * [1.0.0-beta.6](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-synapse/1.0.0-beta.6/jar)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-synapse/1.0.0-beta.5/jar)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-synapse/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-synapse/1.0.0-beta.3/jar)
* [package-composite-v1](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/synapse/resource-manager#tag-package-composite-v1)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-synapse/1.0.0-beta.2/jar)
* [package-2021-03](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/synapse/resource-manager#tag-package-2021-03)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-synapse/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> timeseriesinsights </summary>

* [package-2020-05-15](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/timeseriesinsights/resource-manager#tag-package-2020-05-15)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-timeseriesinsights/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> videoanalyzer </summary>

* [package-preview-2021-11](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/videoanalyzer/resource-manager#tag-package-preview-2021-11)
    * [1.0.0-beta.5](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-videoanalyzer/1.0.0-beta.5/jar)
    * [1.0.0-beta.4](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-videoanalyzer/1.0.0-beta.4/jar)
    * [1.0.0-beta.3](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-videoanalyzer/1.0.0-beta.3/jar)
* [package-2021-05-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/videoanalyzer/resource-manager#tag-package-2021-05-01-preview)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-videoanalyzer/1.0.0-beta.2/jar)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-videoanalyzer/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> vmwarecloudsimple </summary>

* [package-2019-04-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/vmwarecloudsimple/resource-manager#tag-package-2019-04-01)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-vmwarecloudsimple/1.0.0-beta.1/jar)
</details>

<br/>
<details>
<summary> webpubsub </summary>

* [package-2021-10-01](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/webpubsub/resource-manager#tag-package-2021-10-01)
    * [1.0.0-beta.2](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-webpubsub/1.0.0-beta.2/jar)
* [package-2021-06-01-preview](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/webpubsub/resource-manager#tag-package-2021-06-01-preview)
    * [1.0.0-beta.1](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-webpubsub/1.0.0-beta.1/jar)
</details>
