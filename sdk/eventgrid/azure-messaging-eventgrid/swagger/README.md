# Instructions for Generation
This file is used to generate the OpenAPI files for track 2 EventGrid
## Requirements/Installation
You need the following to start generating code
> NodeJS v10.x - v13.x
>
> Java 8+
>
> Maven 3.x

Install Autorest beta with NPM:

`npm i -g @autorest/autorest`

## Using

run `autorest readme.md`

If you are adding or updating swagger files, please make sure that they are included 
in the input file list. Also, if you are adding an additional service event, make sure that 
its `eventType` string is added as a constant and a mapping to the event data model in the
`SystemEventMappings` file.

```yaml
use: '@autorest/java@4.0.24'
java: true
title: EventGridPublisherClient
description: EventGrid Publisher Client
output-folder: ../
namespace: com.azure.messaging.eventgrid
license-header: MICROSOFT_MIT_SMALL
generate-client-as-impl: true
context-client-method-parameter: true
models-subpackage: systemevents
customization-class: EventGridCustomization
directive:
    - rename-model:
        from: ResourceActionCancelData
        to: ResourceActionCancelEventData
    - rename-model:
        from: ResourceActionFailureData
        to: ResourceActionFailureEventData
    - rename-model:
        from: ResourceActionSuccessData
        to: ResourceActionSuccessEventData
    - rename-model:
        from: ResourceDeleteCancelData
        to: ResourceDeleteCancelEventData
    - rename-model:
        from: ResourceDeleteFailureData
        to: ResourceDeleteFailureEventData
    - rename-model:
        from: ResourceDeleteSuccessData
        to: ResourceDeleteSuccessEventData
    - rename-model:
        from: ResourceWriteCancelData
        to: ResourceWriteCancelEventData
    - rename-model:
        from: ResourceWriteFailureData
        to: ResourceWriteFailureEventData
    - rename-model:
        from: ResourceWriteSuccessData
        to: ResourceWriteSuccessEventData
    - rename-model:
        from: RedisImportRDBCompletedEventData
        to: RedisImportRdbCompletedEventData
    - rename-model:
        from: RedisExportRDBCompletedEventData
        to: RedisExportRdbCompletedEventData

custom-types-subpackage: implementation.models
custom-types: CloudEvent,EventGridEvent
model-override-setter-from-superclass: true

input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Storage/stable/2018-01-01/Storage.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.EventHub/stable/2018-01-01/EventHub.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Resources/stable/2018-01-01/Resources.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.EventGrid/stable/2018-01-01/EventGrid.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Devices/stable/2018-01-01/IotHub.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.ContainerRegistry/stable/2018-01-01/ContainerRegistry.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.ServiceBus/stable/2018-01-01/ServiceBus.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Media/stable/2018-01-01/MediaServices.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Maps/stable/2018-01-01/Maps.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.AppConfiguration/stable/2018-01-01/AppConfiguration.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.SignalRService/stable/2018-01-01/SignalRService.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.KeyVault/stable/2018-01-01/KeyVault.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.MachineLearningServices/stable/2018-01-01/MachineLearningServices.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Cache/stable/2018-01-01/RedisCache.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Web/stable/2018-01-01/Web.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.Communication/stable/2018-01-01/AzureCommunicationServices.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.PolicyInsights/stable/2018-01-01/PolicyInsights.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/eventgrid/data-plane/Microsoft.ContainerService/stable/2018-01-01/ContainerService.json
```

### Customization

```java
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridCustomization extends Customization {

    private static final String SYSTEM_EVENT_CLASS_HEADER = "// Copyright (c) Microsoft Corporation. All rights reserved.\n" +
        "// Licensed under the MIT License.\n" +
        "\n" +
        "package com.azure.messaging.eventgrid.implementation;\n\n";

    private static final String CLASS_DEF = "/**\n" +
        " * This class contains a number of constants that correspond to the value of {@code eventType} of {@link EventGridEvent}s\n" +
        " * and {@code type} of {@link CloudEvent}s, when the event originated from an Azure service. This list should be\n" +
        " * updated with all the service event strings. It also contains a mapping from each service event string to the\n" +
        " * model class that the event string corresponds to in the {@code data} field, which is used to automatically deserialize\n" +
        " * system events by their known string.\n" +
        " */\n" +
        "public final class SystemEventMappingNames {\n";

    private static final String PRIVATE_CTOR = "/**\n" +
        "     * Get a mapping of all the system event type strings to their respective class. This is used by default in\n" +
        "     * the {@link EventGridEvent} and {@link CloudEvent} classes.\n" +
        "     * @return a mapping of all the system event strings to system event objects.\n" +
        "     */\n" +
        "    public static Map<String, Class<?>> getSystemEventMappings() {\n" +
        "        return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS);\n" +
        "    }\n" +
        "\n" +
        "    private SystemEventMappingNames() { \n" +
        "    }";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        List<ClassCustomization> classCustomizations = customization.getPackage("com.azure.messaging.eventgrid.systemevents")
            .listClasses();

        StringBuilder sb = new StringBuilder();
        List<String> imports = new ArrayList<>();

        Map<String, String> nameMap = new TreeMap<>();
        Map<String, String> classMap = new TreeMap<>();
        Map<String, String> descriptionMap = new TreeMap<>();
        Map<String, String> constantNameMap = new TreeMap<>();

        logger.info("Total number of classes " + classCustomizations.size());

        List<ClassCustomization> eventData = classCustomizations
            .stream()
            .filter(classCustomization -> classCustomization.getClassName().endsWith("EventData"))
            .collect(Collectors.toList());

        List<String> validEventDescription = eventData.stream()
            .filter(classCustomization -> {
                int startIndex =
                    classCustomization.getJavadoc().getDescription().indexOf("Microsoft.");
                int endIndex = classCustomization.getJavadoc().getDescription().lastIndexOf(" event.");
                boolean hasEventName = startIndex > 0 && endIndex > 0;
                if (!hasEventName) {
                    logger.info("Class " + classCustomization.getClassName() + " " + classCustomization.getJavadoc().getDescription());
                }
                return hasEventName;
            })
            .map(classCustomization -> {
                int startIndex =
                    classCustomization.getJavadoc().getDescription().indexOf("Microsoft.");
                int endIndex = classCustomization.getJavadoc().getDescription().indexOf(" ", startIndex);
                String eventName = classCustomization.getJavadoc().getDescription().substring(startIndex, endIndex);
                String className = classCustomization.getClassName();
                String constantName = getConstantName(className.replace("EventData", ""));

                constantNameMap.put(className, constantName);
                nameMap.put(className, eventName);
                classMap.put(className, className + ".class");
                descriptionMap.put(className, classCustomization.getJavadoc().getDescription());
                imports.add(className);
                return eventName;
            })
            .collect(Collectors.toList());


        Collections.sort(imports);
        sb.append(SYSTEM_EVENT_CLASS_HEADER);

        sb.append("import com.azure.core.models.CloudEvent;\n");
        sb.append("import com.azure.messaging.eventgrid.EventGridEvent;\n");
        sb.append("import java.util.Collections;\n");
        sb.append("import java.util.HashMap;\n");
        sb.append("import java.util.Map;\n");

        for (String className : imports) {
            sb.append("import com.azure.messaging.eventgrid.systemevents." + className + ";\n");
        }
        sb.append(CLASS_DEF);

        for (String className : imports) {
            sb.append("/**\n");
            sb.append("* " + descriptionMap.get(className) + "\n");
            sb.append("*/\n");
            sb.append("public static final String " + constantNameMap.get(className) + " = \"" + nameMap.get(className) +
                "\";\n\n");
        }

        sb.append("private static final Map<String, Class<?>> SYSTEM_EVENT_MAPPINGS = new HashMap<String, Class<?>>()" +
            " {{\n");

        for (String className : imports) {
            sb.append("put(" + constantNameMap.get(className) + ", " + classMap.get(className) + ");\n");
        }
        sb.append("}\n};\n");

        sb.append(PRIVATE_CTOR);

        sb.append("}\n");
        logger.info("Total number of events " + eventData.size());
        logger.info("Total number of events with proper description " + validEventDescription.size());

        customization.getRawEditor()
            .addFile("src/main/java/com/azure/messaging/eventgrid/implementation/SystemEventMappingNames.java", sb.toString());
    }

    public static String getConstantName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        // trim leading and trailing '_'
        if ((name.startsWith("_") || name.endsWith("_")) && !name.chars().allMatch(c -> c == '_')) {
            StringBuilder sb = new StringBuilder(name);
            while (sb.length() > 0 && sb.charAt(0) == '_') {
                sb.deleteCharAt(0);
            }
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '_') {
                sb.setLength(sb.length() - 1);
            }
            name = sb.toString();
        }

        String result = name;
        result = result.replaceAll("_{2,}", "_");  // merge multiple underlines
        Function<Character, Boolean> isUpper = c -> c >= 'A' && c <= 'Z';
        Function<Character, Boolean> isLower = c -> c >= 'a' && c <= 'z';
        for (int i = 1; i < result.length() - 1; i++) {
            if (isUpper.apply(result.charAt(i))) {
                if (result.charAt(i - 1) != '_' && isLower.apply(result.charAt(i - 1))) {
                    result = result.substring(0, i) + "_" + result.substring(i);
                }
            }
        }
        return result.toUpperCase();
    }
}
```
