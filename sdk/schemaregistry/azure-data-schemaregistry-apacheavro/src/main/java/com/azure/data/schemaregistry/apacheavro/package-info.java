// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>Microsoft Azure client library for Schema Registry Apache Avro Serializer provides support for serializing and
 * deserializing Apache Avro objects with schemas stored in
 * <a
 * href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry</a>.
 *
 * <h2>Key Concepts</h2>
 *
 * <ul>
 *     <li><strong>Schema:</strong>  Text describing the how to deserialize and serialize an object.</li>
 *     <li><strong>Schema Registry:</strong>  Centralized location for event producers and consumers to fetch schemas
 *     used to serialize and deserialized structured data.</li>
 *     <li><strong><a href="https://avro.apache.org/">Apache Avro</a>:</strong>  Serialization format for data.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The starting point for creating clients is via builders.  The examples shown in this document use a credential
 * object named DefaultAzureCredential for authentication, which is appropriate for most scenarios, including local
 * development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.  You can find more information on different ways of authenticating
 * and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct the serializer</strong></p>
 *
 * <p>The following code demonstrates the creation of
 * {@link com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer}. The credential used is
 * {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development and
 * chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.construct -->
 * <pre>
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder&#40;&#41;
 *     .credential&#40;tokenCredential&#41;
 *     .fullyQualifiedNamespace&#40;&quot;&#123;schema-registry-endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder&#40;&#41;
 *     .schemaRegistryClient&#40;schemaRegistryAsyncClient&#41;
 *     .schemaGroup&#40;&quot;&#123;schema-group&#125;&quot;&#41;
 *     .buildSerializer&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.construct -->
 *
 * <p><strong>Sample: Serialize an object</strong></p>
 *
 * <p>The serializer can serialize objects into any class extending from {@link com.azure.core.models.MessageContent}.
 * {@code EventData} extends from {@link com.azure.core.models.MessageContent}, so the object can be serialized
 * seamlessly.</p>
 *
 * <p>The serializer assumes there is a no argument constructor used to instantiate the
 * {@link com.azure.core.models.MessageContent} type.  If there is a different way to instantiate the concrete type,
 * use the overload which takes a message factory function,
 * {@link com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer#serialize(java.lang.Object, com.azure.core.util.serializer.TypeReference, java.util.function.Function)}.
 * </p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize-eventdata -->
 * <pre>
 * &#47;&#47; The object to encode. The Avro schema is:
 * &#47;&#47; &#123;
 * &#47;&#47;     &quot;namespace&quot;: &quot;com.azure.data.schemaregistry.apacheavro.generatedtestsources&quot;,
 * &#47;&#47;     &quot;type&quot;: &quot;record&quot;,
 * &#47;&#47;     &quot;name&quot;: &quot;Person&quot;,
 * &#47;&#47;     &quot;fields&quot;: [
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;name&quot;, &quot;type&quot;: &quot;string&quot;&#125;,
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;favourite_number&quot;, &quot;type&quot;: [&quot;int&quot;, &quot;null&quot;]&#125;,
 * &#47;&#47;         &#123;&quot;name&quot;:&quot;favourite_colour&quot;, &quot;type&quot;: [&quot;string&quot;, &quot;null&quot;]&#125;
 * &#47;&#47;   ]
 * &#47;&#47; &#125;
 * Person person = Person.newBuilder&#40;&#41;
 *     .setName&#40;&quot;Chase&quot;&#41;
 *     .setFavouriteColour&#40;&quot;Turquoise&quot;&#41;
 *     .build&#40;&#41;;
 *
 * EventData eventData = serializer.serialize&#40;person, TypeReference.createInstance&#40;EventData.class&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize-eventdata -->
 *
 * <p><strong>Sample: Deserialize an object</strong></p>
 *
 * <p>The serializer can deserialize messages that were created using any of the serialize methods.  In the sample,
 * {@code EventData} is created by serializing the Avro-generated object, Person.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize-eventdata -->
 * <pre>
 * &#47;&#47; EventData created from the Avro generated object, person.
 * EventData eventData = serializer.serialize&#40;person, TypeReference.createInstance&#40;EventData.class&#41;&#41;;
 *
 * Person deserialized = serializer.deserialize&#40;eventData, TypeReference.createInstance&#40;Person.class&#41;&#41;;
 *
 * System.out.printf&#40;&quot;Name: %s, Number: %s%n&quot;, deserialized.getName&#40;&#41;, deserialized.getFavouriteNumber&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize-eventdata -->
 */
package com.azure.data.schemaregistry.apacheavro;

