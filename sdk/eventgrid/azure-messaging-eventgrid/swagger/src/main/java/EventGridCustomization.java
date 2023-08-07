import org.slf4j.Logger;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridCustomization extends Customization {

    private static final String SYSTEM_EVENT_CLASS_HEADER = "// Copyright (c) Microsoft Corporation. All rights reserved." + System.lineSeparator() +
        "// Licensed under the MIT License." + System.lineSeparator() + System.lineSeparator() +
        "package com.azure.messaging.eventgrid;" + System.lineSeparator() + System.lineSeparator();

    private static final String CLASS_DEF = "/**" + System.lineSeparator() +
        " * This class contains a number of constants that correspond to the value of {@code eventType} of {@link EventGridEvent}s" + System.lineSeparator() +
        " * and {@code type} of {@link CloudEvent}s, when the event originated from an Azure service. This list should be" + System.lineSeparator() +
        " * updated with all the service event strings. It also contains a mapping from each service event string to the" + System.lineSeparator() +
        " * model class that the event string corresponds to in the {@code data} field, which is used to automatically deserialize" + System.lineSeparator() +
        " * system events by their known string." + System.lineSeparator() +
        " */" + System.lineSeparator() +
        "public final class SystemEventNames {" + System.lineSeparator();

    private static final String PRIVATE_CTOR = "/**" + System.lineSeparator() +
        "     * Get a mapping of all the system event type strings to their respective class. This is used by default in" + System.lineSeparator() +
        "     * the {@link EventGridEvent} and {@link CloudEvent} classes." + System.lineSeparator() +
        "     * @return a mapping of all the system event strings to system event objects." + System.lineSeparator() +
        "     */" + System.lineSeparator() +
        "    public static Map<String, Class<?>> getSystemEventMappings() {" + System.lineSeparator() +
        "        return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS);" + System.lineSeparator() +
        "    }" + System.lineSeparator() +
        System.lineSeparator()  +
        "    private SystemEventNames() { " + System.lineSeparator() +
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
                String constantName = getReplacementName(getConstantName(className.replace("EventData", "")));

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

        sb.append("import com.azure.core.models.CloudEvent;");
        sb.append(System.lineSeparator());
        sb.append("import com.azure.messaging.eventgrid.EventGridEvent;");
        sb.append(System.lineSeparator());
        sb.append("import java.util.Collections;");
        sb.append(System.lineSeparator());
        sb.append("import java.util.HashMap;");
        sb.append(System.lineSeparator());
        sb.append("import java.util.Map;");
        sb.append(System.lineSeparator());
        // these two imports are for deprecated events.
        sb.append("import com.azure.messaging.eventgrid.systemevents.AcsChatMemberAddedToThreadWithUserEventData;");
        sb.append(System.lineSeparator());
        sb.append("import com.azure.messaging.eventgrid.systemevents.AcsChatMemberRemovedFromThreadWithUserEventData;");
        sb.append(System.lineSeparator());
        for (String className : imports) {
            sb.append("import com.azure.messaging.eventgrid.systemevents." + className + ";");
            sb.append(System.lineSeparator());
        }
        sb.append(CLASS_DEF);

        for (String className : imports) {
            sb.append("/**");
            sb.append(System.lineSeparator());
            sb.append("* " + descriptionMap.get(className));
            sb.append(System.lineSeparator());
            sb.append("*/");
            sb.append(System.lineSeparator());
            sb.append("public static final String " + constantNameMap.get(className) + " = \"" + nameMap.get(className) +
                "\";");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
        // Add deprecated events
        sb.append("/**");
        sb.append(System.lineSeparator());
        sb.append(" * @deprecated This event does not exist.");
        sb.append(System.lineSeparator());
        sb.append(" */");
        sb.append(System.lineSeparator());
        sb.append("@Deprecated");
        sb.append(System.lineSeparator());
        sb.append("public static final String COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER =");
        sb.append(System.lineSeparator());
        sb.append("\"Microsoft.Communication.ChatMemberAddedToThreadWithUser\";");
        sb.append(System.lineSeparator());
        sb.append("/**");
        sb.append(System.lineSeparator());
        sb.append(" * @deprecated This event does not exist.");
        sb.append(System.lineSeparator());
        sb.append(" */");
        sb.append(System.lineSeparator());
        sb.append("@Deprecated");
        sb.append(System.lineSeparator());
        sb.append("public static final String COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER =");
        sb.append(System.lineSeparator());
        sb.append("\"Microsoft.Communication.ChatMemberRemovedFromThreadWithUser\";");
        sb.append(System.lineSeparator());
        sb.append("/**");
        sb.append(System.lineSeparator());
        sb.append(" * @deprecated As of 4.1.0, replaced by {@link #COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD}.");
        sb.append(System.lineSeparator());
        sb.append(" */");
        sb.append(System.lineSeparator());
        sb.append("@Deprecated");
        sb.append(System.lineSeparator());
        sb.append("public static final String COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD =");
        sb.append(System.lineSeparator());
        sb.append("\"Microsoft.Communication.ChatThreadParticipantRemoved\";");
        sb.append(System.lineSeparator());

        sb.append("/**");
        sb.append(System.lineSeparator());
        sb.append(" * @deprecated As of 4.1.0, replaced by {@link #COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD_WITH_USER}.");
        sb.append(System.lineSeparator());
        sb.append(" */");
        sb.append(System.lineSeparator());
        sb.append("@Deprecated");
        sb.append(System.lineSeparator());
        sb.append("public static final String COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER =");
        sb.append(System.lineSeparator());
        sb.append("\"Microsoft.Communication.ChatParticipantRemovedFromThreadWithUser\";");
        sb.append(System.lineSeparator());

        sb.append("private static final Map<String, Class<?>> SYSTEM_EVENT_MAPPINGS = new HashMap<String, Class<?>>()" +
            " {{");
        sb.append(System.lineSeparator());

        for (String className : imports) {
            sb.append("put(" + constantNameMap.get(className) + ", " + classMap.get(className) + ");");
            sb.append(System.lineSeparator());
        }
        sb.append("put(COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD, AcsChatParticipantRemovedFromThreadEventData.class);");
        sb.append(System.lineSeparator());
        sb.append("put(COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER, AcsChatParticipantRemovedFromThreadWithUserEventData.class);");
        sb.append(System.lineSeparator());
        sb.append("put(COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER, AcsChatMemberAddedToThreadWithUserEventData.class);");
        sb.append(System.lineSeparator());
        sb.append("put(COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER, AcsChatMemberRemovedFromThreadWithUserEventData.class);");
        sb.append(System.lineSeparator());
        sb.append("}};");
        sb.append(System.lineSeparator());

        sb.append(PRIVATE_CTOR);

        sb.append("}");
        sb.append(System.lineSeparator());
        logger.info("Total number of events " + eventData.size());
        logger.info("Total number of events with proper description " + validEventDescription.size());

        customization.getRawEditor()
        .addFile("src/main/java/com/azure/messaging/eventgrid/SystemEventNames.java", sb.toString());


        customizeMediaJobOutputAsset(customization);
        customizeStorageDirectoryDeletedEventData(customization);
        customizeAcsRecordingFileStatusUpdatedEventDataDuration(customization);
        customizeMediaLiveEventChannelArchiveHeartbeatEventDataDuration(customization);
        customizeMediaLiveEventIngestHeartbeatEventData(customization);
        customizeResourceEvents(customization, logger);
        customizeEventGridClientImplImports(customization);
    }

    public void customizeResourceEvents(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");

        Arrays.asList("Action", "Delete", "Write").forEach(action -> {
            Arrays.asList("Cancel", "Failure", "Success").forEach(result -> {
                String className = String.format("Resource%s%sEventData", action, result);
                ClassCustomization classCustomization = packageModels.getClass(className);
                classCustomization.addStaticBlock(String.format("static final ClientLogger LOGGER = new ClientLogger(%s.class);", className),
                        Arrays.asList("com.azure.core.util.logging.ClientLogger"));
                classCustomization.addStaticBlock("static final SerializerAdapter DEFAULT_SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();",
                        Arrays.asList("com.azure.core.util.serializer.JacksonAdapter", "com.azure.core.util.serializer.SerializerAdapter", "com.azure.core.util.serializer.SerializerEncoding"));
                Arrays.asList("Authorization", "Claims", "HttpRequest").forEach(method -> {
                    classCustomization.getMethod(String.format("get%s", method)).rename(String.format("getResource%s", method));
                    classCustomization.getMethod(String.format("set%s", method)).rename(String.format("setResource%s", method));
                });

                classCustomization.addMethod("@Deprecated public String getClaims() { final Map<String, String> resourceClaims = getResourceClaims(); if (!resourceClaims.isEmpty()) { try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceClaims, SerializerEncoding.JSON); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } } return null; }");
                classCustomization.addMethod(String.format("@Deprecated public %s setClaims(String claims) { try { setResourceClaims(DEFAULT_SERIALIZER_ADAPTER.deserialize(claims, Map.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }", className), Arrays.asList("java.io.IOException", "java.io.UncheckedIOException"));
                classCustomization.addMethod("@Deprecated public String getHttpRequest() { ResourceHttpRequest resourceHttpRequest = getResourceHttpRequest(); try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceHttpRequest, SerializerEncoding.JSON); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } }");
                classCustomization.addMethod(String.format("@Deprecated public %s setHttpRequest(String httpRequest) { try { setResourceHttpRequest( DEFAULT_SERIALIZER_ADAPTER.deserialize(httpRequest, ResourceHttpRequest.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }", className));
                classCustomization.addMethod("@Deprecated public String getAuthorization() { final ResourceAuthorization resourceAuthorization = getResourceAuthorization(); try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceAuthorization, SerializerEncoding.JSON); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } }");
                classCustomization.addMethod(String.format("@Deprecated public %s setAuthorization(String authorization) { try { setResourceAuthorization( DEFAULT_SERIALIZER_ADAPTER.deserialize(authorization, ResourceAuthorization.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }", className));


                classCustomization.getMethod("getClaims")
                    .getJavadoc()
                    .setDescription("Get the claims property: The properties of the claims.")
                    .setReturn("the claims value.")
                    .setDeprecated(String.format("This method is no longer supported since v4.9.0. <p> Use {@link %s#getResourceClaims()} instead.", className));

                classCustomization.getMethod("setClaims")
                    .getJavadoc()
                    .setDescription("Set the claims property: The properties of the claims.")
                    .setParam("claims", "the claims value to set.")
                    .setReturn(String.format("the %s object itself.", className))
                    .setDeprecated(String.format("This method is no longer supported since v4.9.0. <p> Use {@link %s#setResourceClaims(Map)} instead.", className));

                classCustomization.getMethod("getAuthorization")
                    .getJavadoc()
                    .setDescription("Get the authorization property: The requested authorization for the operation.")
                    .setReturn("the authorization value.")
                    .setDeprecated(String.format("This method is no longer supported since v4.9.0. <p> Use {@link %s#getResourceAuthorization()} instead.", className));

                classCustomization.getMethod("setAuthorization")
                    .getJavadoc()
                    .setDescription("Set the authorization property: The requested authorization for the operation.")
                    .setParam("authorization", "the authorization value to set.")
                    .setReturn(String.format("the %s object itself.", className))
                    .setDeprecated(String.format("This method is no longer supported since v4.9.0. <p> Use {@link %s#setResourceAuthorization(ResourceAuthorization)} instead.", className));

                classCustomization.getMethod("getHttpRequest")
                    .getJavadoc()
                    .setDescription("Get the httpRequest property: The details of the operation.")
                    .setReturn("the httpRequest value.")
                    .setDeprecated(String.format("This method is no longer supported since v4.9.0. <p> Use {@link %s#getResourceHttpRequest()} instead.", className));

                classCustomization.getMethod("setHttpRequest")
                    .getJavadoc()
                    .setDescription("Set the httpRequest property: The details of the operation.")
                    .setParam("httpRequest", "the httpRequest value to set.")
                    .setReturn(String.format("the %s object itself.", className))
                    .setDeprecated(String.format("This method is no longer supported since v4.9.0. <p> Use {@link %s#setResourceHttpRequest(ResourceHttpRequest)} instead.", className));


            });
        });
    }

    public void customizeMediaLiveEventIngestHeartbeatEventData(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("MediaLiveEventIngestHeartbeatEventData");
        classCustomization.addStaticBlock("static final ClientLogger LOGGER = new ClientLogger(MediaLiveEventIngestHeartbeatEventData.class);", Arrays.asList("com.azure.core.util.logging.ClientLogger"));
        classCustomization.getMethod("getIngestDriftValue")
            .setReturnType("Integer", "")
            .replaceBody("if (\"n/a\".equals(this.ingestDriftValue)) { return null; } try { return Integer.parseInt(this.ingestDriftValue); } catch (NumberFormatException ex) { LOGGER.logExceptionAsError(ex); return null; }");

        classCustomization.getMethod("getLastFragmentArrivalTime")
            .setReturnType("OffsetDateTime", "")
            .replaceBody("return OffsetDateTime.parse(this.lastFragmentArrivalTime);", Arrays.asList("java.time.OffsetDateTime"));

    }

    public void customizeMediaLiveEventChannelArchiveHeartbeatEventDataDuration(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("MediaLiveEventChannelArchiveHeartbeatEventData");
        classCustomization.addStaticBlock("static final ClientLogger LOGGER = new ClientLogger(MediaLiveEventChannelArchiveHeartbeatEventData.class);");

        PropertyCustomization property = classCustomization.getProperty("channelLatencyMs");
        property.generateGetterAndSetter();

        classCustomization.getMethod("getChannelLatencyMs")
            .rename("getChannelLatency")
            .setReturnType("Duration", "")
            .replaceBody("if (\"n/a\".equals(this.channelLatencyMs)) { return null; } Long channelLatencyMsLong; try { channelLatencyMsLong = Long.parseLong(this.channelLatencyMs); } catch (NumberFormatException ex) { LOGGER.logExceptionAsError(ex); return null; } return Duration.ofMillis(channelLatencyMsLong);", Arrays.asList("java.time.Duration"))
            .getJavadoc()
            .setDescription("Gets the duration of channel latency.")
            .setReturn("the duration of channel latency.");

        try {
            classCustomization.removeMethod("setChannelLatencyMs");
            classCustomization.removeMethod("setLatencyResultCode");
            classCustomization.removeMethod("getLogger");
        } catch (IllegalArgumentException none) {}
    }

    public void customizeAcsRecordingFileStatusUpdatedEventDataDuration(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("AcsRecordingFileStatusUpdatedEventData");
        PropertyCustomization property = classCustomization.getProperty("recordingDurationMs");
        property.generateGetterAndSetter();

        classCustomization.getMethod("getRecordingDurationMs")
            .rename("getRecordingDuration")
            .setReturnType("Duration", "Duration.ofMillis(%s)")
            .replaceBody("if (this.recordingDurationMs != null) { return Duration.ofMillis(this.recordingDurationMs); } return null;", Arrays.asList("java.time.Duration"))
            .getJavadoc()
            .setDescription("Get the recordingDuration property: The recording duration.")
            .setReturn("the recordingDuration value.");


        classCustomization.getMethod("setRecordingDurationMs")
            .rename("setRecordingDuration")
            .replaceParameters("Duration recordingDuration")
            .replaceBody("if (recordingDuration != null) { this.recordingDurationMs = recordingDuration.toMillis(); } else { this.recordingDurationMs = null; } return this;")
            .getJavadoc()
            .setDescription("Set the recordingDuration property: The recording duration.")
            .setParam("recordingDuration", "the recordingDuration value to set.")
            .removeParam("recordingDurationMs");

    }

    public void customizeStorageDirectoryDeletedEventData(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("StorageDirectoryDeletedEventData");
        classCustomization.getMethod("getRecursive").rename("isRecursive").setReturnType("Boolean", "Boolean.getBoolean(%s)");
        classCustomization.getMethod("setRecursive").replaceParameters("Boolean recursive").replaceBody("this.recursive = String.valueOf(recursive); return this;");
    }

    public void customizeMediaJobOutputAsset(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        packageModels.getClass("MediaJobOutputAsset").setModifier(Modifier.PUBLIC | Modifier.FINAL);
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

    public void customizeEventGridClientImplImports(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.implementation");
        ClassCustomization classCustomization = packageModels.getClass("EventGridPublisherClientImpl");

        classCustomization.customizeAst(comp -> {
            comp.getImports().removeIf(p -> {
                return p.getNameAsString().equals("com.azure.messaging.eventgrid.implementation.models.CloudEvent");
            });
            comp.addImport("com.azure.core.models.CloudEvent");
        });

    }

    private static final Map<String, String> replacementNames = new HashMap<String,String>() {
        {
            put("SUBSCRIPTION_DELETED","EVENT_GRID_SUBSCRIPTION_DELETED");
            put("SUBSCRIPTION_VALIDATION","EVENT_GRID_SUBSCRIPTION_VALIDATION");
            put("MACHINE_LEARNING_SERVICES_DATASET_DRIFT_DETECTED","MACHINE_LEARNING_DATASET_DRIFT_DETECTED");
            put("MACHINE_LEARNING_SERVICES_MODEL_DEPLOYED","MACHINE_LEARNING_MODEL_DEPLOYED");
            put("MACHINE_LEARNING_SERVICES_MODEL_REGISTERED","MACHINE_LEARNING_MODEL_REGISTERED");
            put("MACHINE_LEARNING_SERVICES_RUN_COMPLETED","MACHINE_LEARNING_RUN_COMPLETED");
            put("MACHINE_LEARNING_SERVICES_RUN_STATUS_CHANGED","MACHINE_LEARNING_RUN_STATUS_CHANGED");
            put("KEY_VAULT_ACCESS_POLICY_CHANGED","KEY_VAULT_VAULT_ACCESS_POLICY_CHANGED");
            put("MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUT_OF_SYNC","MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUTOFSYNC");
            put("MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUT_OF_SYNC","MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUTOFSYNC");
            put("SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATIONS","SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATION");
            put("SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATIONS","SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATION");
            put("SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENERS","SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER");
            put("SIGNAL_RSERVICE_CLIENT_CONNECTION_CONNECTED","SIGNAL_R_SERVICE_CLIENT_CONNECTION_CONNECTED");
            put("SIGNAL_RSERVICE_CLIENT_CONNECTION_DISCONNECTED","SIGNAL_R_SERVICE_CLIENT_CONNECTION_DISCONNECTED");
        }
    };

    private String getReplacementName(String name) {
        if (replacementNames.containsKey(name)) {
            return replacementNames.get(name);
        }
        if (name.startsWith("ACS_")) {
            return name.replace("ACS_", "COMMUNICATION_");
        }
        return name;
    }


}
