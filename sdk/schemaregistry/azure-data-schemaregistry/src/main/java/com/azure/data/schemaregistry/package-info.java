// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>
 * The Azure Schema Registry client library provides support for <a
 * href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry</a>.  The library
 * focuses on registering and fetching schemas stored in Schema Registry.
 *
 * <h2>Getting Started</h2>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Schema Registry.
 * {@link com.azure.data.schemaregistry.SchemaRegistryClient} is the synchronous service client and
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient} is the asynchronous service client.</p>
 *
 * <p><strong>Sample: Construct a service client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.data.schemaregistry.SchemaRegistryClient}.  The credential used is {@code DefaultAzureCredential}
 * because it combines commonly used credentials in deployment and development and chooses the credential to used based
 * on its running environment.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.construct -->
 * <pre>
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;&#123;schema-registry-endpoint&quot;&#41;
 *     .credential&#40;tokenCredential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.construct -->
 *
 * <h2>Using the Client</h2>
 *
 * <p><strong>Sample: Register a schema</strong></p>
 *
 * <p>The following code sample demonstrates registering an Avro schema.  The
 * {@link com.azure.data.schemaregistry.models.SchemaProperties} returned contains the schema's id.  This id uniquely
 * identifies the schema in Schema Registry.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.registerschema-avro -->
 * <pre>
 * SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;&#123;schema-registry-endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 *
 * String schemaContent = &quot;&#123;&#92;n&quot;
 *     + &quot;    &#92;&quot;type&#92;&quot; : &#92;&quot;record&#92;&quot;,  &#92;n&quot;
 *     + &quot;    &#92;&quot;namespace&#92;&quot; : &#92;&quot;SampleSchemaNameSpace&#92;&quot;, &#92;n&quot;
 *     + &quot;    &#92;&quot;name&#92;&quot; : &#92;&quot;Person&#92;&quot;, &#92;n&quot;
 *     + &quot;    &#92;&quot;fields&#92;&quot; : [&#92;n&quot;
 *     + &quot;        &#123; &#92;n&quot;
 *     + &quot;            &#92;&quot;name&#92;&quot; : &#92;&quot;FirstName&#92;&quot; , &#92;&quot;type&#92;&quot; : &#92;&quot;string&#92;&quot; &#92;n&quot;
 *     + &quot;        &#125;, &#92;n&quot;
 *     + &quot;        &#123; &#92;n&quot;
 *     + &quot;            &#92;&quot;name&#92;&quot; : &#92;&quot;LastName&#92;&quot;, &#92;&quot;type&#92;&quot; : &#92;&quot;string&#92;&quot; &#92;n&quot;
 *     + &quot;        &#125;&#92;n&quot;
 *     + &quot;    ]&#92;n&quot;
 *     + &quot;&#125;&quot;;
 * SchemaProperties schemaProperties = schemaRegistryClient.registerSchema&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;,
 *     schemaContent, SchemaFormat.AVRO&#41;;
 *
 * System.out.println&#40;&quot;Registered schema: &quot; + schemaProperties.getId&#40;&#41;&#41;;
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
 * SchemaRegistrySchema schema = schemaRegistryClient.getSchema&#40;&quot;&#123;schema-id&#125;&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved schema: '%s'. Contents: %s%n&quot;, schema.getProperties&#40;&#41;.getId&#40;&#41;,
 *     schema.getDefinition&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.getschema -->
 *
 * @see com.azure.data.schemaregistry.SchemaRegistryClient
 * @see com.azure.data.schemaregistry.SchemaRegistryAsyncClient
 * @see <a href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry</a>
 */
package com.azure.data.schemaregistry;
