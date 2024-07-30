# Migrate from Purview Catalog to Purview DataMap

This guide is intended to assist in the migration to Azure Purview DataMap client library [`azure-analytics-purview-datamap`](https://central.sonatype.com/artifact/com.azure/azure-analytics-purview-datamap) from [`azure-analytics-purview-catalog`](https://central.sonatype.com/artifact/com.azure/azure-analytics-purview-catalog). It will focus on side-by-side comparisons for similar operations between the two packages.

For those new to the Purview Data Map library for Java, please refer to the [`azure-analytics-purview-datamap` readme](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-datamap/README.md) and [`azure-analytics-purview-datamap` samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/purview/azure-analytics-purview-datamap/src/samples) for the `azure-analytics-purview-datamap` library rather than this guide.

## Table of contents

- [Migration benefits](#migration-benefits)
- [General changes](#general-changes)
  - [Package and client name](#package-and-client-name)
- [Additional samples](#additional-samples)

## Migration benefits

> Note: `azure-analytics-purview-catalog` has been <b>deprecated</b>. Please upgrade to `azure-analytics-purview-datamap` for continued support.


The new Purview DataMap library `azure-analytics-purview-datamap` includes the service models together with the DataMap APIs [API Document](https://learn.microsoft.com/rest/api/purview/datamapdataplane/operation-groups). The client name and the operation names have slightly changed but the main functionality remains the same.

## General changes

### Package and client name

Previously in `azure-analytics-purview-catalog`, to create a glossary client, users should use GlossaryClientBuilder.

```java
GlossaryClient client = new GlossaryClientBuilder()
    .endpoint("https://<account-name>.purview.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

Now in `azure-analytics-purview-datamap`, users should create a DataMapClient first and then build the client for each operation groups.

```java
DataMapClientBuilder clientBuilder = new DataMapClientBuilder()
    .endpoint("https://<account-name>.purview.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build());
GlossaryClient client = clientBuilder.buildGlossaryClient();
```

### Operation name

The operation names have slightly changed but the main functionality remains the same. Please check the below examples.

#### Get type by name

Using `azure-analytics-purview-catalog`

```java
TypesClient typesClient = new TypesClientBuilder()
    .endpoint(endPoint)
    .credential(cred)
    .buildClient();
Response<BinaryData> response = typesClient.getEntityDefinitionByNameWithResponse("AtlasGlossary", null);
```

Using `azure-analytics-purview-datamap`

```java
TypeDefinitionClient typeDefinitionClient = clientBuilder.buildTypeDefinitionClient();
AtlasEntityDef type = typeDefinitionClient.getEntityByName("AtlasGlossary");
```

## Additional samples

For more examples, see [Samples for Purview DataMap](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/purview/azure-analytics-purview-datamap#examples).