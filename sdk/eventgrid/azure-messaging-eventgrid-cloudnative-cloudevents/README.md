# Azure Cloud Native Cloud Event support Azure for Event Grid Java

This library can be used to enable publishing CloudNative CloudEvents using the Azure Event Grid library.

## Getting started

### Prerequisites
If you use the Azure CLI, replace `<your-resource-group-name>` and `<your-resource-name>` with your own unique names
and `<location>` with a valid Azure service location.

```bash
az eventgrid domain create --location <location> --resource-group <your-resource-group-name> --name <your-resource-name>
```

### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-messaging-eventgrid-cloudnative-cloudevents</artifactId>
      <version>1.0.0-beta.1</version> <!-- {x-version-update;com.azure:azure-messaging-eventgrid-cloudnative-cloudevents;current} -->
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid</artifactId>
    <version>4.7.0</version>
</dependency>
```
[//]: # ({x-version-update-end})
