# Azure Service Bus Management APIs for Java

> see https://aka.ms/autorest

## Getting Started

To build the SDK for ServiceBusAdministrationClient and ServiceBusAdministrationAsyncClient, simply [Install AutoRest](https://github.com/Azure/autorest/blob/master/docs/install/readme.md) and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

There is one swagger for Service Bus management APIs.

```ps
cd <swagger-folder>
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
use: '@autorest/java@4.1.29'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/1d5723dc330e9749d5ded6cb9db5a309b3705fa4/specification/servicebus/data-plane/Microsoft.ServiceBus/stable/2021-05/servicebus.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.messaging.servicebus.administration
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: essential
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types: AccessRights,EntityStatus,NamespaceProperties,MessagingSku,NamespaceType
custom-types-subpackage: models
context-client-method-parameter: true
customization-class: src/main/java/AdministrationClientCustomization.java
enable-sync-stack: true
generic-response-type: true
custom-strongly-typed-header-deserialization: true
disable-client-builder: true
stream-style-serialization: true
```

### Change Return Types of Subscription REST Methods

Replaces `object` as the return type for subscription REST APIs to either `SubscriptionDescriptionFeed` or
`SubscriptionDescriptionEntry` based on the API.

```yaml
directive:
  - from: swagger-document
    where: $.paths
    transform: >
      delete $["/{topicName}/subscriptions"].get.responses["200"].schema.type;
      $["/{topicName}/subscriptions"].get.responses["200"].schema["$ref"] = "#/definitions/SubscriptionDescriptionFeed";

      delete $["/{topicName}/subscriptions/{subscriptionName}"].get.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}"].get.responses["200"].schema["$ref"] = "#/definitions/SubscriptionDescriptionEntry";

      delete $["/{topicName}/subscriptions/{subscriptionName}"].put.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}"].put.responses["200"].schema["$ref"] = "#/definitions/SubscriptionDescriptionEntry";

      delete $["/{topicName}/subscriptions/{subscriptionName}"].put.responses["201"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}"].put.responses["201"].schema["$ref"] = "#/definitions/SubscriptionDescriptionEntry";

      delete $["/{topicName}/subscriptions/{subscriptionName}"].delete.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}"].delete.responses["200"].schema["$ref"] = "#/definitions/SubscriptionDescriptionEntry";
```

### Change Return Types of Rules REST Methods

Replaces `object` as the return type for rules REST APIs to either `RuleDescriptionFeed` or`RuleDescriptionEntry` based 
on the API.

```yaml
directive:
  - from: swagger-document
    where: $.paths
    transform: >
      delete $["/{topicName}/subscriptions/{subscriptionName}/rules"].get.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}/rules"].get.responses["200"].schema["$ref"] = "#/definitions/RuleDescriptionFeed";

      delete $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].get.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].get.responses["200"].schema["$ref"] = "#/definitions/RuleDescriptionEntry";

      delete $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].put.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].put.responses["200"].schema["$ref"] = "#/definitions/RuleDescriptionEntry";

      delete $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].put.responses["201"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].put.responses["201"].schema["$ref"] = "#/definitions/RuleDescriptionEntry";

      delete $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].delete.responses["200"].schema.type;
      $["/{topicName}/subscriptions/{subscriptionName}/rules/{ruleName}"].delete.responses["200"].schema["$ref"] = "#/definitions/RuleDescriptionEntry";
```

### Change Title Properties to Non-Object

Adds a new definition for `Title` to replace the `object` or `string` `title` properties for entry and feed models,
properly aligning the definition with what is returned by the service.

```yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.Title = {
        "type": "object",
        "xml": {
          "name": "title",
          "namespace": "http://www.w3.org/2005/Atom"
        },
        "properties": {
          "type": {
            "xml": {
              "attribute": true,
              "name": "type",
              "namespace": "http://www.w3.org/2005/Atom"
            },
            "type": "string",
            "description": "The type of the title."
          },
          "content": {
            "xml": {
              "x-ms-text": true,
            },
            "type": "string",
            "description": "The title."
          }
        }
      };
      
      $.NamespacePropertiesEntry.properties.title["$ref"] = "#/definitions/Title";
      $.QueueDescriptionEntry.properties.title["$ref"] = "#/definitions/Title";
      $.QueueDescriptionFeed.properties.title["$ref"] = "#/definitions/Title";
      $.TopicDescriptionEntry.properties.title["$ref"] = "#/definitions/Title";
      $.TopicDescriptionFeed.properties.title["$ref"] = "#/definitions/Title";
      $.SubscriptionDescriptionEntry.properties.title["$ref"] = "#/definitions/Title";
      $.SubscriptionDescriptionFeed.properties.title["$ref"] = "#/definitions/Title";
      $.RuleDescriptionEntry.properties.title["$ref"] = "#/definitions/Title";
      $.RuleDescriptionFeed.properties.title["$ref"] = "#/definitions/Title";
```

### Add x-ms-discriminator-value to Polymorphic Types

Adds `x-ms-discriminator-value` to `CorrelationFilter`, `SqlFilter`, `TrueFilter`, and `FalseFilter` for `RuleFilter`
subtypes and `SqlRuleAction` and `EmptyRuleAction` for `RuleAction` subtypes.

```yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.CorrelationFilter["x-ms-discriminator-value"] = "CorrelationFilter";
      $.SqlFilter["x-ms-discriminator-value"] = "SqlFilter";
      $.TrueFilter["x-ms-discriminator-value"] = "TrueFilter";
      $.FalseFilter["x-ms-discriminator-value"] = "FalseFilter";
      $.SqlRuleAction["x-ms-discriminator-value"] = "SqlRuleAction";
      $.EmptyRuleAction["x-ms-discriminator-value"] = "EmptyRuleAction";
```

### Fix ServiceBusManagementError XML Definition

`ServiceBusManagementError`'s Swagger definition was missing a root `xml` definition of `name: "Error"`.

```yaml
directive:
  - from: swagger-document
    where: $.definitions.ServiceBusManagementError
    transform: >
      $.xml = { "name": "Error" };
```

### Remove Invalid Prefixes

`QueueDescriptionEntry` and `TopicDescriptionEntry` define a prefix without a namespace.

```yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      delete $.QueueDescriptionEntry.properties.base.xml.prefix;
      delete $.TopicDescriptionEntry.properties.base.xml.prefix;
```

### Fix RuleDescription definitions

`RuleDescription` gets used as a `$ref` property with different XML names based on the usage. Therefore, it cannot
define the `XML` name itself. Instead, rely on the Swagger model property name matching the expected XML serialization
name to support using a different XML element name based on the use case.

Instead of simply adding a new property with the correct name to `CreateRuleBody` and `SubscriptionDescription`, we need
use a custom transformation to ensure the property ordering is retained, as the property order matters some times.

```yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      delete $.RuleDescription.xml.name;

      const contentProperties = $.CreateRuleBody.properties.content.properties;
      const newContentProperties = {};
      Object.keys(contentProperties).forEach(key => {
        if (key === "ruleDescription") {
          newContentProperties["RuleDescription"] = contentProperties[key];
        } else {
          newContentProperties[key] = contentProperties[key];
        }
      });
      $.CreateRuleBody.properties.content.properties = newContentProperties;
        
      const subscriptionProperties = $.SubscriptionDescription.properties;
      const newSubscriptionProperties = {};
      Object.keys(subscriptionProperties).forEach(key => {
        if (key === "defaultRuleDescription") {
          newSubscriptionProperties["DefaultRuleDescription"] = subscriptionProperties[key];
        } else {
          newSubscriptionProperties[key] = subscriptionProperties[key];
        }
      });
      $.SubscriptionDescription.properties = newSubscriptionProperties;
```
