// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry</a> is a
 * service in Microsoft Azure that enables users to manage the schemas for their applications and data. A schema is a
 * formal definition of the structure of data in a particular format, such as Avro or JSON. With Azure Schema Registry,
 * users can store, version, and manage these schemas in a central location, making it easier to ensure consistency and
 * compatibility across different applications and systems.</p>
 *
 * <p>In addition, Azure Schema Registry can integrate with other Azure services, such as Azure Event Hubs and
 * Azure Stream Analytics, to provide a complete data processing and analytics solution. By using Azure Schema
 * Registry, users can simplify the management of their data schemas, reduce errors and inconsistencies, and
 * accelerate the development of data-driven applications.</p>
 *
 * <p>The Azure Schema Registry client library allows Java developers to interact with Azure Schema Registry. It
 * provides a set of APIs that enable Java developers to perform operations such as registering, updating, and
 * retrieving schemas from the Azure Schema Registry.</p>
 *
 * <h2>Key Concepts</h2>
 *
 * <ul>
 *     <li><strong>Schema:</strong>  Text describing the how to deserialize and serialize an object.</li>
 *     <li><strong>Schema Registry:</strong>  Centralized location for event producers and consumers to fetch schemas
 *     used to serialize and deserialized structured data.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Schema Registry.
 * {@link com.azure.data.schemaregistry.SchemaRegistryClient} is the synchronous service client and
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient} is the asynchronous service client.  The examples
 * shown in this document use a credential object named DefaultAzureCredential for authentication, which is appropriate
 * for most scenarios, including local development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a service client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.data.schemaregistry.SchemaRegistryClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs
 * Namespace's host name.  It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via
 * Azure Portal.  The credential used is {@code DefaultAzureCredential} because it combines commonly used credentials
 * in deployment and development and chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.construct -->
 * <pre>
 * DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * SchemaRegistryClient client = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;https:&#47;&#47;&lt;your-schema-registry-endpoint&gt;.servicebus.windows.net&quot;&#41;
 *     .credential&#40;azureCredential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.construct -->
 * <hr/>
 *
 * <h2>Using the Client</h2>
 *
 * <p>The samples below use the synchronous client, {@link com.azure.data.schemaregistry.SchemaRegistryClient}.
 * More samples can be found in the class's Javadoc.  In addition, samples using the asynchronous
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient} can be found in the class's JavaDocs.</p>
 *
 * <p><strong>Sample: Register a schema</strong></p>
 *
 * <p>The following code sample demonstrates registering an Avro schema.  The
 * {@link com.azure.data.schemaregistry.models.SchemaProperties} returned contains the schema's id.  This id uniquely
 * identifies the schema and can be used to quickly associate payloads with that schema.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * SchemaProperties properties = client.registerSchema&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema,
 *     SchemaFormat.AVRO&#41;;
 *
 * System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, properties.getId&#40;&#41;, properties.getFormat&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro -->
 *
 * <p><strong>Sample: Getting the schema using a schema id</strong></p>
 *
 * <p>The following code sample demonstrates how to fetch a schema using its schema id.  The schema id can be found in
 * {@link com.azure.data.schemaregistry.models.SchemaProperties#getId()} when a schema is registered or using
 * {@link com.azure.data.schemaregistry.SchemaRegistryClient#getSchemaProperties(java.lang.String, java.lang.String, java.lang.String, com.azure.data.schemaregistry.models.SchemaFormat)}.
 * </p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.getschema -->
 * <pre>
 * SchemaRegistrySchema schema = client.getSchema&#40;&quot;&#123;schema-id&#125;&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, schema.getProperties&#40;&#41;.getId&#40;&#41;,
 *     schema.getProperties&#40;&#41;.getFormat&#40;&#41;&#41;;
 * System.out.println&#40;&quot;Schema contents: &quot; + schema.getDefinition&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.getschema -->
 *
 * @see com.azure.data.schemaregistry.SchemaRegistryClient
 * @see com.azure.data.schemaregistry.SchemaRegistryAsyncClient
 * @see <a href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry</a>
 */
package com.azure.data.schemaregistry;
