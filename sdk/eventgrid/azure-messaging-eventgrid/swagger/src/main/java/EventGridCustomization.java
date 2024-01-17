import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.StaticJavaParser.parseExpression;

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
        customizeEventGridEvent(customization.getPackage("com.azure.messaging.eventgrid.implementation.models")
            .getClass("EventGridEvent"));

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
        customizeAcsRouterEvents(customization);
        customizeResourceNotificationEvents(customization);
    }

    public void customizeResourceEvents(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");

        Arrays.asList("Action", "Delete", "Write").forEach(action -> {
            Arrays.asList("Cancel", "Failure", "Success").forEach(result -> {
                String className = String.format("Resource%s%sEventData", action, result);
                ClassCustomization classCustomization = packageModels.getClass(className);


                classCustomization.customizeAst(compilationUnit -> {

                    ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName(className).get();
                    compilationUnit.addImport("com.azure.core.util.logging.ClientLogger");
                    compilationUnit.addImport("com.azure.core.util.serializer.JacksonAdapter");
                    compilationUnit.addImport("com.azure.core.util.serializer.SerializerAdapter");
                    compilationUnit.addImport("com.azure.core.util.serializer.SerializerEncoding");
                    compilationUnit.addImport("java.io.IOException");
                    compilationUnit.addImport("java.io.UncheckedIOException");
                    clazz.addFieldWithInitializer("ClientLogger", "LOGGER", parseExpression("new ClientLogger(" + className + ".class)"), Keyword.PRIVATE, Keyword.STATIC, Keyword.FINAL);
                    clazz.addFieldWithInitializer("SerializerAdapter", "DEFAULT_SERIALIZER_ADAPTER", parseExpression("JacksonAdapter.createDefaultSerializerAdapter()"), Keyword.PRIVATE, Keyword.STATIC, Keyword.FINAL);


                    Arrays.asList("Authorization", "Claims", "HttpRequest").forEach(method -> {
                        clazz.getMethodsByName("get" + method).forEach(methodDeclaration -> { methodDeclaration.setName("getResource" + method); });
                        clazz.getMethodsByName("set" + method).forEach(methodDeclaration -> { methodDeclaration.setName("setResource" + method); });
                    });

                    clazz.addMethod("getClaims", Keyword.PUBLIC)
                        .setType("String")
                        .addAnnotation(new MarkerAnnotationExpr("Deprecated"))
                        .setBody(parseBlock("{ final Map<String, String> resourceClaims = getResourceClaims(); if (!resourceClaims.isEmpty()) { try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceClaims, SerializerEncoding.JSON); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } } return null; }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Get the claims property: The properties of the claims."))))
                            .addBlockTag("return", "the claims value.")
                            .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#getResourceClaims()} instead.")
                        );

                    clazz.addMethod("setClaims", Keyword.PUBLIC)
                        .setType(className)
                        .addAnnotation(new MarkerAnnotationExpr("Deprecated"))
                        .addParameter("String", "claims")
                        .setBody(parseBlock("{ try { setResourceClaims(DEFAULT_SERIALIZER_ADAPTER.deserialize(claims, Map.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Set the claims property: The properties of the claims."))))
                            .addBlockTag("param", "claims the claims value to set.")
                            .addBlockTag("return", "the " + className + " object itself.")
                            .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#setResourceClaims(Map)} instead.")
                        );

                    clazz.addMethod("getHttpRequest", Keyword.PUBLIC)
                        .setType("String")
                        .addAnnotation(new MarkerAnnotationExpr("Deprecated"))
                        .setBody(parseBlock("{ ResourceHttpRequest resourceHttpRequest = getResourceHttpRequest(); try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceHttpRequest, SerializerEncoding.JSON); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Get the httpRequest property: The details of the operation."))))
                            .addBlockTag("return", "the httpRequest value.")
                            .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#getResourceHttpRequest()} instead.")
                        );

                    clazz.addMethod("setHttpRequest", Keyword.PUBLIC)
                        .setType(className)
                        .addAnnotation(new MarkerAnnotationExpr("Deprecated"))
                        .addParameter("String", "httpRequest")
                        .setBody(parseBlock("{ try { setResourceHttpRequest( DEFAULT_SERIALIZER_ADAPTER.deserialize(httpRequest, ResourceHttpRequest.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Set the httpRequest property: The details of the operation."))))
                            .addBlockTag("param", "httpRequest the httpRequest value to set.")
                            .addBlockTag("return", "the " + className + " object itself.")
                            .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#setResourceHttpRequest(ResourceHttpRequest)} instead.")
                        );

                    clazz.addMethod("getAuthorization", Keyword.PUBLIC)
                        .setType("String")
                        .addAnnotation(new MarkerAnnotationExpr("Deprecated"))
                        .setBody(parseBlock("{ final ResourceAuthorization resourceAuthorization = getResourceAuthorization(); try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceAuthorization, SerializerEncoding.JSON); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Get the authorization property: The requested authorization for the operation."))))
                            .addBlockTag("return", "the authorization value.")
                            .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#getResourceAuthorization()} instead.")
                        );

                    clazz.addMethod("setAuthorization", Keyword.PUBLIC)
                        .setType(className)
                        .addAnnotation(new MarkerAnnotationExpr("Deprecated"))
                        .addParameter("String", "authorization")
                        .setBody(parseBlock("{ try { setResourceAuthorization( DEFAULT_SERIALIZER_ADAPTER.deserialize(authorization, ResourceAuthorization.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Set the authorization property: The requested authorization for the operation."))))
                            .addBlockTag("param", "authorization the authorization value to set.")
                            .addBlockTag("return", "the " + className + " object itself.")
                            .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#setResourceAuthorization(ResourceAuthorization)} instead.")
                        );

                });
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
        } catch (IllegalArgumentException ignored) {

        }
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
            comp.getImports().removeIf(p -> p.getNameAsString().equals("com.azure.messaging.eventgrid.implementation.models.CloudEvent"));
            comp.addImport("com.azure.core.models.CloudEvent");
        });

    }

    public void customizeAcsRouterEvents(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("AcsRouterWorkerSelector");

        classCustomization.customizeAst(comp -> {
            ClassOrInterfaceDeclaration clazz = comp.getClassByName("AcsRouterWorkerSelector").get();
            clazz.getMethodsByName("getTtlSeconds").forEach(m -> {
                m.setType(Duration.class);
                m.setBody(parseBlock("{ return ttlSeconds == null ? null : Duration.ofSeconds(ttlSeconds.longValue()); }"));
                m.setName("getTimeToLive");
            });

            clazz.getMethodsByName("setTtlSeconds").forEach(m -> {
                m.setType("AcsRouterWorkerSelector");
                m.getParameter(0).setType(Duration.class);
                m.setBody(parseBlock("{ if (timeToLive != null) { this.ttlSeconds = (float) timeToLive.getSeconds(); } return this; }"));
                m.setName("setTimeToLive");
                m.getParameter(0).setName("timeToLive");
                m.setJavadocComment(
                      "     * Set the timeToLive property: Router Job Worker Selector Time to Live in Seconds.\n"
                    + "     *\n"
                    + "     * @param timeToLive the timeToLive value to set.\n"
                    + "     * @return the AcsRouterWorkerSelector object itself.\n");
            });
        });

        classCustomization = packageModels.getClass("AcsRouterJobClassificationFailedEventData");
        classCustomization.addImports("com.azure.core.models.ResponseError");
        classCustomization.addImports("java.util.stream.Collectors");
        classCustomization.customizeAst(comp -> {
           ClassOrInterfaceDeclaration clazz = comp.getClassByName("AcsRouterJobClassificationFailedEventData").get();
           clazz.getMethodsByName("getErrors").forEach(m -> {
               m.setType("List<ResponseError>");
               m.setBody(parseBlock("{ return this.errors.stream().map(e -> new ResponseError(e.getCode(), e.getMessage())).collect(Collectors.toList()); }"));
           });

           clazz.getMethodsByName("setErrors").forEach(m -> {
               m.setParameter(0, new Parameter().setType("List<ResponseError>").setName("errors"));
               m.setBody(parseBlock("{ this.errors = errors.stream().map(e -> new AcsRouterCommunicationError().setCode(e.getCode()).setMessage(e.getMessage())).collect(Collectors.toList()); return this; }"));
           });
        });

        classCustomization = packageModels.getClass("AcsRouterJobReceivedEventData");
        classCustomization.customizeAst(comp -> {
           ClassOrInterfaceDeclaration clazz = comp.getClassByName("AcsRouterJobReceivedEventData").get();
           clazz.getMethodsByName("setUnavailableForMatching").forEach(m -> {
               m.getParameter(0).setType(Boolean.class);
           });
           clazz.getMethodsByName("isUnavailableForMatching").forEach(m -> {
               m.setType(Boolean.class);
           });
        });

        classCustomization = packageModels.getClass("AcsRouterJobWaitingForActivationEventData");
        classCustomization.customizeAst(comp -> {
           ClassOrInterfaceDeclaration clazz = comp.getClassByName("AcsRouterJobWaitingForActivationEventData").get();
           clazz.getMethodsByName("setUnavailableForMatching").forEach(m -> m.getParameter(0).setType(Boolean.class));
           clazz.getMethodsByName("isUnavailableForMatching").forEach(m -> m.setType(Boolean.class));
        });
    }

    public static void customizeResourceNotificationEvents(LibraryCustomization customization) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("ResourceNotificationsResourceUpdatedDetails");
        classCustomization.customizeAst(comp -> {
            ClassOrInterfaceDeclaration clazz = comp.getClassByName("ResourceNotificationsResourceUpdatedDetails").get();
            comp.addImport("com.azure.core.util.logging.ClientLogger");
            comp.addImport("com.azure.core.util.logging.LogLevel");
            clazz.addFieldWithInitializer("ClientLogger", "LOGGER", parseExpression("new ClientLogger(ResourceNotificationsResourceUpdatedDetails.class)"), Keyword.PRIVATE, Keyword.STATIC, Keyword.FINAL);

            clazz.getMethodsByName("getTags").forEach(m -> m.setName("getResourceTags"));
            clazz.getMethodsByName("setTags").forEach(m -> m.setName("setResourceTags"));
            clazz.addMethod("getTags", Keyword.PUBLIC)
                .setType("String")
                .setBody(parseBlock("{ LOGGER.log(LogLevel.INFORMATIONAL, () -> \"This method has been replaced with getResourceTags().\"); return null; }"))
                .setJavadocComment(new Javadoc(
                    new JavadocDescription(List.of(new JavadocSnippet("Get the tags property: The resource tags."))))
                    .addBlockTag("return", "the tags value.")
                    .addBlockTag("deprecated", "This property has been replaced with {@link #getResourceTags()}."))
                .addAnnotation(new MarkerAnnotationExpr("Deprecated"));
            clazz.addMethod("setTags", Keyword.PUBLIC)
                .addParameter("String", "tags")
                .setType("ResourceNotificationsResourceUpdatedDetails")
                .setBody(parseBlock("{ LOGGER.log(LogLevel.INFORMATIONAL, () -> \"This method has been replaced with setResourceTags(Map).\"); return this; }"))
                .setJavadocComment(new Javadoc(
                    new JavadocDescription(List.of(new JavadocSnippet("Set the tags property: The resource tags."))))
                    .addBlockTag("param", "tags the tags value to set.")
                    .addBlockTag("return", "the ResourceNotificationsResourceUpdatedDetails object itself.")
                    .addBlockTag("deprecated", "This property has been replaced with {@link #setResourceTags(Map)}."))
                .addAnnotation("Deprecated");
        });
    }

    private static void customizeEventGridEvent(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(LocalDateTime.class);
            ast.addImport(ZoneOffset.class);
            ast.addImport(DateTimeParseException.class);
            ast.addImport(TemporalAccessor.class);
            ast.addImport(TemporalQueries.class);

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.addMethod("parseOffsetDateTimeBest", Keyword.PRIVATE, Keyword.STATIC)
                .setType(OffsetDateTime.class)
                .addParameter(String.class, "datetimeString")
                .setJavadocComment(StaticJavaParser.parseJavadoc(String.join("\n",
                    "Attempts to parse an ISO datetime string as best possible. The initial attempt will use",
                    "{@link OffsetDateTime#from(TemporalAccessor)} and will fall back to",
                    "{@link LocalDateTime#from(TemporalAccessor)} and apply {@link ZoneOffset#UTC} as the",
                    "timezone.",
                    "",
                    "@param datetimeString The datetime string to parse.",
                    "@return The {@link OffsetDateTime} representing the string.",
                    "@throws DateTimeParseException If the datetime is neither an ISO offset datetime or ISO local datetime."
                )))
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "    TemporalAccessor temporal = DateTimeFormatter.ISO_DATE_TIME",
                    "        .parseBest(datetimeString, OffsetDateTime::from, LocalDateTime::from);",
                    "",
                    "    return  (temporal.query(TemporalQueries.offset()) == null)",
                    "        ? LocalDateTime.from(temporal).atOffset(ZoneOffset.UTC)",
                    "        : OffsetDateTime.from(temporal);",
                    "}")));
        });

        Editor editor = classCustomization.getEditor();

        String originalContent = editor.getFileContent(classCustomization.getFileName());
        String replacedContent = originalContent.replace("= reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));",
            "= reader.getNullable(nonNullReader -> parseOffsetDateTimeBest(nonNullReader.getString()));");

        editor.replaceFile(classCustomization.getFileName(), replacedContent);
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
