// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridCustomization extends Customization {

    @SuppressWarnings("deprecation")
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization systemEvents = customization.getPackage("com.azure.messaging.eventgrid.systemevents");

        // Workaround to a bug where 'Editor.classesInPackage' cut off the first character in the class name.
        String packagePath = "src/main/java/com/azure/messaging/eventgrid/systemevents/";
        List<ClassCustomization> classCustomizations = customization.getRawEditor().getContents().keySet().stream()
            .filter(fileName -> fileName.startsWith(packagePath))
            .map(fileName -> fileName.substring(packagePath.length(), fileName.length() - 5))
            .map(systemEvents::getClass)
            .collect(Collectors.toList());
        // TODO (alzimmer): Replace above with this once bug is fixed.
//        List<ClassCustomization> classCustomizations = systemEvents.listClasses();

        List<String> imports = new ArrayList<>();

        Map<String, String> nameMap = new TreeMap<>();
        Map<String, String> descriptionMap = new TreeMap<>();
        Map<String, String> constantNameMap = new TreeMap<>();

        logger.info("Total number of classes " + classCustomizations.size());

        List<ClassCustomization> eventData = classCustomizations
            .stream()
            .filter(classCustomization -> classCustomization.getClassName().endsWith("EventData"))
            .collect(Collectors.toList());

        List<String> validEventDescription = eventData.stream()
            .map(classCustomization -> {
                String className = classCustomization.getClassName();
                AtomicReference<String> javadocRef = new AtomicReference<>();
                classCustomization.customizeAst(ast -> ast.getClassByName(className)
                    .flatMap(NodeWithJavadoc::getJavadoc)
                    .ifPresent(javadoc -> javadocRef.set(javadoc.getDescription().toText())));

                String javadoc = javadocRef.get();
                int startIndex = javadoc.indexOf("Microsoft.");
                int endIndex = javadoc.lastIndexOf(" event.");
                boolean hasEventName = startIndex > 0 && endIndex > 0;
                if (!hasEventName) {
                    logger.info("Class " + classCustomization.getClassName() + " " + javadoc);
                    return null;
                }

                endIndex = javadoc.indexOf(" ", startIndex);
                String eventName = javadoc.substring(startIndex, endIndex);
                String constantName = getReplacementName(getConstantName(className.replace("EventData", "")));

                constantNameMap.put(className, constantName);
                nameMap.put(className, eventName);
                descriptionMap.put(className, javadoc);
                imports.add(className);
                return eventName;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());


        Collections.sort(imports);
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.addOrphanComment(new LineComment(" Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment(" Licensed under the MIT License."));

        compilationUnit.setPackageDeclaration("com.azure.messaging.eventgrid");

        compilationUnit.addImport("com.azure.core.models.CloudEvent");
        compilationUnit.addImport("com.azure.messaging.eventgrid.EventGridEvent");
        compilationUnit.addImport("java.util.Collections");
        compilationUnit.addImport("java.util.HashMap");
        compilationUnit.addImport("java.util.Map");
        // these two imports are for deprecated events.
        compilationUnit.addImport("com.azure.messaging.eventgrid.systemevents.AcsChatMemberAddedToThreadWithUserEventData");
        compilationUnit.addImport("com.azure.messaging.eventgrid.systemevents.AcsChatMemberRemovedFromThreadWithUserEventData");
        for (String className : imports) {
            compilationUnit.addImport("com.azure.messaging.eventgrid.systemevents." + className);
        }

        ClassOrInterfaceDeclaration clazz = compilationUnit.addClass("SystemEventNames", Keyword.PUBLIC, Keyword.FINAL)
            .setJavadocComment("This class contains a number of constants that correspond to the value of "
                + "{@code eventType} of {@link EventGridEvent}s and {@code type} of {@link CloudEvent}s, when the "
                + "event originated from an Azure service. This list should be updated with all the service event "
                + "strings. It also contains a mapping from each service event string to the model class that the "
                + "event string corresponds to in the {@code data} field, which is used to automatically deserialize "
                + "system events by their known string.");

        Keyword[] publicStaticFinal = new Keyword[] { Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL };
        for (String className : imports) {
            clazz.addFieldWithInitializer("String", constantNameMap.get(className),
                new StringLiteralExpr(nameMap.get(className)), publicStaticFinal)
                .setJavadocComment(descriptionMap.get(className));
        }

        // Add deprecated events
        clazz.addFieldWithInitializer("String", "COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER",
            new StringLiteralExpr("Microsoft.Communication.ChatMemberAddedToThreadWithUser"), publicStaticFinal)
            .addMarkerAnnotation(Deprecated.class)
            .setJavadocComment(new Javadoc(new JavadocDescription())
                .addBlockTag("deprecated", "This event does not exist."));

        clazz.addFieldWithInitializer("String", "COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER",
                new StringLiteralExpr("Microsoft.Communication.ChatMemberRemovedFromThreadWithUser"), publicStaticFinal)
            .addMarkerAnnotation(Deprecated.class)
            .setJavadocComment(new Javadoc(new JavadocDescription())
                .addBlockTag("deprecated", "This event does not exist."));

        clazz.addFieldWithInitializer("String", "COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD",
                new StringLiteralExpr("Microsoft.Communication.ChatThreadParticipantRemoved"), publicStaticFinal)
            .addMarkerAnnotation(Deprecated.class)
            .setJavadocComment(new Javadoc(new JavadocDescription())
                .addBlockTag("deprecated", "As of 4.1.0, replaced by {@link #COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD}."));

        clazz.addFieldWithInitializer("String", "COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER",
                new StringLiteralExpr("Microsoft.Communication.ChatParticipantRemovedFromThreadWithUser"), publicStaticFinal)
            .addMarkerAnnotation(Deprecated.class)
            .setJavadocComment(new Javadoc(new JavadocDescription())
                .addBlockTag("deprecated", "As of 4.1.0, replaced by {@link #COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD_WITH_USER}."));

        clazz.addFieldWithInitializer("Map<String, Class<?>>", "SYSTEM_EVENT_MAPPINGS",
            parseExpression("new HashMap<>()"), Keyword.PRIVATE, Keyword.STATIC, Keyword.FINAL);

        BlockStmt staticInitializer = clazz.addStaticInitializer();
        for (String className : imports) {
            staticInitializer.addStatement(putCall(constantNameMap.get(className), className));
        }
        staticInitializer.addStatement(putCall("COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD",
            "AcsChatParticipantRemovedFromThreadEventData"));
        staticInitializer.addStatement(putCall("COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER",
            "AcsChatParticipantRemovedFromThreadWithUserEventData"));
        staticInitializer.addStatement(putCall("COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER",
            "AcsChatMemberAddedToThreadWithUserEventData"));
        staticInitializer.addStatement(putCall("COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER",
            "AcsChatMemberRemovedFromThreadWithUserEventData"));

        // Getter for system event mappings.
        clazz.addMethod("getSystemEventMappings", Keyword.PUBLIC, Keyword.STATIC)
            .setType("Map<String, Class<?>>")
            .setBody(parseBlock("{ return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS); }"))
            .setJavadocComment(new Javadoc(parseText("Get a mapping of all the system event type strings to their "
                + "respective class. This is used by default in the {@link EventGridEvent} and {@link CloudEvent} classes."))
                .addBlockTag("return", "a mapping of all the system event strings to system event objects."));

        // Private no-args constructor.
        clazz.addConstructor(Keyword.PRIVATE);

        logger.info("Total number of events " + eventData.size());
        logger.info("Total number of events with proper description " + validEventDescription.size());

        customization.getRawEditor()
            .addFile("src/main/java/com/azure/messaging/eventgrid/SystemEventNames.java", compilationUnit.toString());

        addDeprecationWarnings(customization, classCustomizations);
        customizeMediaJobOutputAsset(systemEvents);
        customizeStorageDirectoryDeletedEventData(systemEvents);
        customizeAcsRecordingFileStatusUpdatedEventDataDuration(systemEvents);
        customizeMediaLiveEventChannelArchiveHeartbeatEventDataDuration(systemEvents);
        customizeMediaLiveEventIngestHeartbeatEventData(systemEvents);
        customizeResourceEvents(systemEvents);
        customizeEventGridClientImplImports(customization);
        customizeAcsRouterEvents(systemEvents);
        customizeResourceNotificationEvents(systemEvents);
        customizeAcsMessageChannelEventError(systemEvents);
        customizeCommuicationSMSEvents(systemEvents);
        customizeAcsCallEndedEventDataDuration(systemEvents);
    }

    private void addDeprecationWarnings(LibraryCustomization customization, List<ClassCustomization> classCustomizations) {
        PackageCustomization systemEvent = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        String packagePath = "src/main/java/com/azure/messaging/eventgrid/systemevents/";
        customization.getRawEditor().getContents().keySet().stream()
            .filter(fileName -> fileName.startsWith(packagePath))
            .map(fileName -> fileName.substring(packagePath.length(), fileName.length() - 5))
            .filter(className -> !className.contains("/") && !"package-info".equals(className))
            .map(systemEvent::getClass)
            .forEach(classCustomization -> {
            classCustomization.customizeAst(ast -> {
                String className = classCustomization.getClassName();
                
                // Handle classes
                ast.getClassByName(className).ifPresent(clazz -> {
                    addDeprecationToTypeDeclaration(clazz);
                });
                
                // Handle enums
                ast.getEnumByName(className).ifPresent(enumDecl -> {
                    addDeprecationToTypeDeclaration(enumDecl);
                });
            });
        });
    }

    @SuppressWarnings("deprecation")
    private static ExpressionStmt putCall(String mapKey, String mapValue) {
        // Creates a call such as 'SYSTEM_EVENT_MAPPINGS.put(COMMUNICATION_CALL_ENDED, AcsCallEndedEventData.class);'
        return new ExpressionStmt(new MethodCallExpr(new NameExpr("SYSTEM_EVENT_MAPPINGS"), "put")
            .addArgument(new NameExpr(mapKey))
            .addArgument(new ClassExpr(new ClassOrInterfaceType(mapValue))));
    }

    public void customizeCommuicationSMSEvents(PackageCustomization customization) {
        customization.getClass("AcsSmsReceivedEventData").customizeAst(ast ->
            ast.getClassByName("AcsSmsReceivedEventData").ifPresent(clazz -> {
            clazz.getFieldByName("segmentCount").ifPresent(field -> field.getVariable(0).setType("Integer"));
                clazz.getMethodsByName("setSegmentCount").forEach(m -> m.getParameter(0).setType(Integer.class));
                clazz.getMethodsByName("getSegmentCount").forEach(m -> m.setType(Integer.class));
        }));
    }

    public void customizeResourceEvents(PackageCustomization customization) {
        List<String> actions = Arrays.asList("Action", "Delete", "Write");
        List<String> results = Arrays.asList("Cancel", "Failure", "Success");

        for (String action : actions) {
            for (String result : results) {
                String className = String.format("Resource%s%sEventData", action, result);
                customizeResourceEvent(customization.getClass(className), className);
            }
        }

    }

    private void customizeResourceEvent(ClassCustomization customization, String className) {
        customization.customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.addImport("com.azure.core.util.serializer.JacksonAdapter");
            ast.addImport("com.azure.core.util.serializer.SerializerAdapter");
            ast.addImport("com.azure.core.util.serializer.SerializerEncoding");
            ast.addImport("java.io.IOException");
            ast.addImport("java.io.UncheckedIOException");

            ast.getClassByName(className).ifPresent(clazz -> {
                addClientLoggerField(className, clazz);
                clazz.addFieldWithInitializer("SerializerAdapter", "DEFAULT_SERIALIZER_ADAPTER",
                    parseExpression("JacksonAdapter.createDefaultSerializerAdapter()"), Keyword.PRIVATE, Keyword.STATIC,
                    Keyword.FINAL);

                Arrays.asList("Authorization", "Claims", "HttpRequest").forEach(method -> {
                    clazz.getMethodsByName("get" + method).forEach(methodDeclaration -> methodDeclaration.setName("getResource" + method));
                    clazz.getMethodsByName("set" + method).forEach(methodDeclaration -> methodDeclaration.setName("setResource" + method));
                });

                clazz.addMethod("getClaims", Keyword.PUBLIC)
                    .setType("String")
                    .addMarkerAnnotation("Deprecated")
                    .setBody(parseBlock("{ final Map<String, String> resourceClaims = getResourceClaims(); "
                        + "if (!resourceClaims.isEmpty()) { try { return DEFAULT_SERIALIZER_ADAPTER.serialize("
                        + "resourceClaims, SerializerEncoding.JSON); } catch (IOException ex) { throw "
                        + "LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } } return null; }"))
                    .setJavadocComment(new Javadoc(parseText("Get the claims property: The properties of the claims."))
                        .addBlockTag("return", "the claims value.")
                        .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#getResourceClaims()} instead."));

                clazz.addMethod("setClaims", Keyword.PUBLIC)
                    .setType(className)
                    .addMarkerAnnotation("Deprecated")
                    .addParameter("String", "claims")
                    .setBody(parseBlock("{ try { setResourceClaims(DEFAULT_SERIALIZER_ADAPTER.deserialize(claims, "
                        + "Map.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw "
                        + "LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the claims property: The properties of the claims."))
                        .addBlockTag("param", "claims the claims value to set.")
                        .addBlockTag("return", "the " + className + " object itself.")
                        .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#setResourceClaims(Map)} instead."));

                clazz.addMethod("getHttpRequest", Keyword.PUBLIC)
                    .setType("String")
                    .addMarkerAnnotation("Deprecated")
                    .setBody(parseBlock("{ ResourceHttpRequest resourceHttpRequest = getResourceHttpRequest(); try { "
                        + "return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceHttpRequest, SerializerEncoding.JSON); } "
                        + "catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } }"))
                    .setJavadocComment(new Javadoc(parseText("Get the httpRequest property: The details of the operation."))
                        .addBlockTag("return", "the httpRequest value.")
                        .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#getResourceHttpRequest()} instead."));

                clazz.addMethod("setHttpRequest", Keyword.PUBLIC)
                    .setType(className)
                    .addMarkerAnnotation("Deprecated")
                    .addParameter("String", "httpRequest")
                    .setBody(parseBlock("{ try { setResourceHttpRequest( DEFAULT_SERIALIZER_ADAPTER.deserialize("
                        + "httpRequest, ResourceHttpRequest.class, SerializerEncoding.JSON)); } catch (IOException ex) "
                        + "{ throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the httpRequest property: The details of the operation."))
                        .addBlockTag("param", "httpRequest the httpRequest value to set.")
                        .addBlockTag("return", "the " + className + " object itself.")
                        .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#setResourceHttpRequest(ResourceHttpRequest)} instead."));

                clazz.addMethod("getAuthorization", Keyword.PUBLIC)
                    .setType("String")
                    .addMarkerAnnotation("Deprecated")
                    .setBody(parseBlock("{ final ResourceAuthorization resourceAuthorization = getResourceAuthorization(); "
                        + "try { return DEFAULT_SERIALIZER_ADAPTER.serialize(resourceAuthorization, SerializerEncoding.JSON); } "
                        + "catch (IOException ex) { throw LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } }"))
                    .setJavadocComment(new Javadoc(parseText("Get the authorization property: The requested authorization for the operation."))
                        .addBlockTag("return", "the authorization value.")
                        .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#getResourceAuthorization()} instead."));

                clazz.addMethod("setAuthorization", Keyword.PUBLIC)
                    .setType(className)
                    .addMarkerAnnotation("Deprecated")
                    .addParameter("String", "authorization")
                    .setBody(parseBlock("{ try { setResourceAuthorization( DEFAULT_SERIALIZER_ADAPTER.deserialize(authorization, "
                        + "ResourceAuthorization.class, SerializerEncoding.JSON)); } catch (IOException ex) { throw "
                        + "LOGGER.logExceptionAsError(new UncheckedIOException(ex)); } return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the authorization property: The requested authorization for the operation."))
                        .addBlockTag("param", "authorization the authorization value to set.")
                        .addBlockTag("return", "the " + className + " object itself.")
                        .addBlockTag("deprecated", "This method is no longer supported since v4.9.0. <p> Use {@link " + className + "#setResourceAuthorization(ResourceAuthorization)} instead."));
            });
        });
    }


    public void customizeAcsMessageChannelEventError(PackageCustomization customization) {
        customization.getClass("AcsMessageEventData").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");

            ast.getClassByName("AcsMessageEventData").ifPresent(clazz -> {
                // Fix up the getError method to always return a ResponseError.
                clazz.getMethodsByName("getError").forEach(m -> m.setType("ResponseError")
                    .setBody(parseBlock("{ return new ResponseError(this.error.getChannelCode(), this.error.getChannelMessage()); }"))
                    .setJavadocComment(new Javadoc(parseText("Get the error property: The channel error code and message."))
                        .addBlockTag("return", "the error value.")));

                // Fix up the existing setError method to be private. It's used for deserializing.
                clazz.getMethodsByName("setError").forEach(m -> m.setType("void")
                    .removeModifier(Keyword.PUBLIC)
                    .setBody(parseBlock("{ this.error = error; }"))
                    .setJavadocComment(new Javadoc(parseText("Used for json deserialization in derived types."))
                        .addBlockTag("param", "error The error value to set")));

                // Add the new setError method that takes a ResonseError.
                clazz.addMethod("setError", Keyword.PUBLIC)
                    .setType("AcsMessageEventData")
                    .addParameter("ResponseError", "error")
                    .setBody(parseBlock("{ this.error = new AcsMessageChannelEventError().setChannelCode("
                        + "error.getCode()).setChannelMessage(error.getMessage()); return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the error property: The channel error code and message."))
                        .addBlockTag("param", "error The ResponseError object containing error code and message.")
                        .addBlockTag("return", "the AcsMessageEventData object itself."));
            });
        });

        Arrays.asList("AcsMessageDeliveryStatusUpdatedEventData", "AcsMessageReceivedEventData").forEach(name -> {
            customization.getClass(name).customizeAst(ast -> {
                ast.addImport("com.azure.core.models.ResponseError");
                ast.getClassByName(name).ifPresent(clazz -> {
                    // the existing setError method isn't necessary any longer as the base type doesn't return anything now.
                    clazz.getMethodsByName("setError").forEach(Node::remove);

                    // add the new setError method that takes a ResponseError
                    clazz.addMethod("setError", Keyword.PUBLIC)
                        .setType(name)
                        .addParameter("ResponseError", "error")
                        .setBody(parseBlock("{ super.setError(error); return this; }"))
                        .setJavadocComment(new Javadoc(parseText("Set the error property: The channel error code and message."))
                            .addBlockTag("param", "error The ResponseError object containing error code and message.")
                            .addBlockTag("return", "the " + name + " object itself."));
                });
            });
        });
    }

    public void customizeMediaLiveEventIngestHeartbeatEventData(PackageCustomization customization) {
        customization.getClass("MediaLiveEventIngestHeartbeatEventData").customizeAst(ast -> {
            ast.addImport("java.time.OffsetDateTime");
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.getClassByName("MediaLiveEventIngestHeartbeatEventData").ifPresent(clazz -> {
                addClientLoggerField("MediaLiveEventIngestHeartbeatEventData", clazz);

                clazz.getMethodsByName("getIngestDriftValue").forEach(m -> m.setType("Integer")
                    .setBody(parseBlock("{ if (\"n/a\".equals(this.ingestDriftValue)) { return null; } try { return "
                        + "Integer.parseInt(this.ingestDriftValue); } catch (NumberFormatException ex) { "
                        + "LOGGER.logExceptionAsError(ex); return null; } }")));
                clazz.getMethodsByName("getLastFragmentArrivalTime").forEach(m -> m.setType("OffsetDateTime")
                    .setBody(parseBlock("{ return OffsetDateTime.parse(this.lastFragmentArrivalTime); }")));
            });
        });

    }

    public void customizeMediaLiveEventChannelArchiveHeartbeatEventDataDuration(PackageCustomization customization) {
        customization.getClass("MediaLiveEventChannelArchiveHeartbeatEventData").customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.addImport("com.azure.core.util.logging.ClientLogger");

            ast.getClassByName("MediaLiveEventChannelArchiveHeartbeatEventData").ifPresent(clazz -> {
                addClientLoggerField("MediaLiveEventChannelArchiveHeartbeatEventData", clazz);

                clazz.getMethodsByName("getChannelLatencyMs").forEach(m -> m.setName("getChannelLatency")
                    .setType("Duration")
                    .setBody(parseBlock("{ if (\"n/a\".equals(this.channelLatencyMs)) { return null; } "
                        + "Long channelLatencyMsLong; try { channelLatencyMsLong = Long.parseLong(this.channelLatencyMs); } "
                        + "catch (NumberFormatException ex) { LOGGER.logExceptionAsError(ex); return null; } "
                        + "return Duration.ofMillis(channelLatencyMsLong); }"))
                    .setJavadocComment(new Javadoc(parseText("Gets the duration of channel latency."))
                        .addBlockTag("return", "the duration of channel latency.")));

                clazz.getMethodsByName("setChannelLatencyMs").forEach(Node::remove);
                clazz.getMethodsByName("setLatencyResultCode").forEach(Node::remove);
                clazz.getMethodsByName("getLogger").forEach(Node::remove);
            });
        });
    }

    public void customizeAcsRecordingFileStatusUpdatedEventDataDuration(PackageCustomization customization) {
        customization.getClass("AcsRecordingFileStatusUpdatedEventData").customizeAst(ast -> {
            ast.addImport("java.time.Duration");

            ast.getClassByName("AcsRecordingFileStatusUpdatedEventData").ifPresent(clazz -> {
                clazz.getMethodsByName("getRecordingDurationMs").forEach(m -> m.setName("getRecordingDuration")
                    .setType("Duration")
                    .setBody(parseBlock("{ if (this.recordingDurationMs != null) { return "
                        + "Duration.ofMillis(this.recordingDurationMs); } return null; }"))
                    .setJavadocComment(new Javadoc(parseText("Get the recordingDuration property: The recording duration."))
                        .addBlockTag("return", "the recordingDuration value.")));

                clazz.getMethodsByName("setRecordingDurationMs").forEach(m -> m.setName("setRecordingDuration")
                    .setType("AcsRecordingFileStatusUpdatedEventData")
                    .setParameters(new NodeList<>( new Parameter().setType("Duration").setName("recordingDuration")))
                    .setBody(parseBlock("{ if (recordingDuration != null) { this.recordingDurationMs = recordingDuration.toMillis(); }"
                        + " else { this.recordingDurationMs = null; } return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the recordingDuration property: The recording duration."))
                        .addBlockTag("param", "recordingDuration the recordingDuration value to set.")
                        .addBlockTag("return", "the AcsRecordingFileStatusUpdatedEventData object itself.")));
            });
        });

    }

    public void customizeStorageDirectoryDeletedEventData(PackageCustomization customization) {
        customization.getClass("StorageDirectoryDeletedEventData").customizeAst(ast -> ast
            .getClassByName("StorageDirectoryDeletedEventData").ifPresent(clazz -> {
                clazz.getMethodsByName("getRecursive").forEach(method -> method.setName("isRecursive")
                    .setType(Boolean.class)
                    .setBody(parseBlock("{ return Boolean.getBoolean(this.recursive); }")));
                clazz.getMethodsByName("setRecursive").forEach(method ->
                    method.setParameters(new NodeList<>(new Parameter().setType(Boolean.class).setName("recursive")))
                        .setBody(parseBlock("{ this.recursive = String.valueOf(recursive); return this; }")));
            }));
    }

    public void customizeMediaJobOutputAsset(PackageCustomization customization) {
        customization.getClass("MediaJobOutputAsset").customizeAst(ast -> ast.getClassByName("MediaJobOutputAsset")
            .ifPresent(clazz -> clazz.setModifiers(Keyword.PUBLIC, Keyword.FINAL)));
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
        customization.getClass("com.azure.messaging.eventgrid.implementation", "EventGridPublisherClientImpl")
            .customizeAst(ast -> {
                ast.getImports().removeIf(p -> p.getNameAsString()
                    .equals("com.azure.messaging.eventgrid.implementation.models.CloudEvent"));
                ast.addImport("com.azure.core.models.CloudEvent");
            });
    }

    public void customizeAcsRouterEvents(PackageCustomization customization) {
        customization.getClass("AcsRouterWorkerSelector").customizeAst(ast -> ast.getClassByName("AcsRouterWorkerSelector")
            .ifPresent(clazz -> {
                clazz.getMethodsByName("getTtlSeconds").forEach(m -> m.setType(Duration.class)
                    .setName("getTimeToLive")
                    .setBody(parseBlock("{ return ttlSeconds == null ? null : Duration.ofSeconds(ttlSeconds.longValue()); }")));

                clazz.getMethodsByName("setTtlSeconds").forEach(m -> m.setType("AcsRouterWorkerSelector")
                    .setName("setTimeToLive")
                    .setParameter(0, new Parameter().setName("timeToLive").setType(Duration.class))
                    .setBody(parseBlock("{ if (timeToLive != null) { this.ttlSeconds = (float) timeToLive.getSeconds(); } return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the timeToLive property: Router Job Worker Selector Time to Live in Seconds."))
                        .addBlockTag("param", "timeToLive", "the timeToLive value to set.")
                        .addBlockTag("return", "the AcsRouterWorkerSelector object itself.")));
            }));

        customization.getClass("AcsRouterJobClassificationFailedEventData").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");
            ast.addImport("java.util.stream.Collectors");

            ast.getClassByName("AcsRouterJobClassificationFailedEventData").ifPresent(clazz -> {
                clazz.getMethodsByName("getErrors").forEach(m -> m.setType("List<ResponseError>")
                    .setBody(parseBlock("{ return this.errors.stream().map(e -> new ResponseError(e.getCode(), "
                        + "e.getMessage())).collect(Collectors.toList()); }")));

                clazz.getMethodsByName("setErrors").forEach(m -> m
                    .setParameter(0, new Parameter().setType("List<ResponseError>").setName("errors"))
                    .setBody(parseBlock("{ this.errors = errors.stream().map(e -> new AcsRouterCommunicationError()"
                        + ".setCode(e.getCode()).setMessage(e.getMessage())).collect(Collectors.toList()); return this; }")));
            });
        });

        for (String name : Arrays.asList("AcsRouterJobReceivedEventData", "AcsRouterJobWaitingForActivationEventData")) {
            customization.getClass(name).customizeAst(ast -> ast.getClassByName(name).ifPresent(clazz -> {
                clazz.getMethodsByName("setUnavailableForMatching").forEach(m -> m.getParameter(0).setType(Boolean.class));
                clazz.getMethodsByName("isUnavailableForMatching").forEach(m -> m.setType(Boolean.class));
            }));
        }
    }

    public static void customizeResourceNotificationEvents(PackageCustomization customization) {
        customization.getClass("ResourceNotificationsResourceUpdatedDetails").customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.addImport("com.azure.core.util.logging.LogLevel");
            ast.getClassByName("ResourceNotificationsResourceUpdatedDetails").ifPresent(clazz -> {
                addClientLoggerField("ResourceNotificationsResourceUpdatedDetails", clazz);

                clazz.getMethodsByName("getTags").forEach(m -> m.setName("getResourceTags"));
                clazz.getMethodsByName("setTags").forEach(m -> m.setName("setResourceTags"));
                clazz.addMethod("getTags", Keyword.PUBLIC)
                    .setType("String")
                    .setBody(parseBlock("{ LOGGER.log(LogLevel.INFORMATIONAL, "
                        + "() -> \"This method has been replaced with getResourceTags().\"); return null; }"))
                    .setJavadocComment(new Javadoc(parseText("Get the tags property: The resource tags."))
                        .addBlockTag("return", "the tags value.")
                        .addBlockTag("deprecated", "This property has been replaced with {@link #getResourceTags()}."))
                    .addMarkerAnnotation("Deprecated");
                clazz.addMethod("setTags", Keyword.PUBLIC)
                    .addParameter("String", "tags")
                    .setType("ResourceNotificationsResourceUpdatedDetails")
                    .setBody(parseBlock("{ LOGGER.log(LogLevel.INFORMATIONAL, "
                        + "() -> \"This method has been replaced with setResourceTags(Map).\"); return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the tags property: The resource tags."))
                        .addBlockTag("param", "tags the tags value to set.")
                        .addBlockTag("return", "the ResourceNotificationsResourceUpdatedDetails object itself.")
                        .addBlockTag("deprecated", "This property has been replaced with {@link #setResourceTags(Map)}."))
                    .addMarkerAnnotation("Deprecated");
            });
        });
    }

    public void customizeAcsCallEndedEventDataDuration(PackageCustomization customization) {
        customization.getClass("AcsCallEndedEventData").customizeAst(ast -> {
            ast.addImport("java.time.Duration");

            ast.getClassByName("AcsCallEndedEventData").ifPresent(clazz -> {
                clazz.getMethodsByName("getCallDurationInSeconds").forEach(m -> m.setName("getCallDuration")
                    .setType("Duration")
                    .setBody(parseBlock("{ if (this.callDurationInSeconds != null) { return Duration.ofNanos((long) "
                        + "(this.callDurationInSeconds * 1000_000_000L)); } return null; }"))
                    .setJavadocComment(new Javadoc(parseText(
                        "Get the callDuration property: Duration of the call in seconds."))
                        .addBlockTag("return", "the callDuration value.")));

                clazz.getMethodsByName("setCallDurationInSeconds").forEach(m -> m.setName("setCallDuration")
                    .setType("AcsCallEndedEventData")
                    .setParameters(new NodeList<>( new Parameter().setType("Duration").setName("callDuration")))
                    .setBody(parseBlock("{ if (callDuration != null) { "
                        + "this.callDurationInSeconds = callDuration.toNanos() / 1_000_000_000f; } else {"
                        + "this.callDurationInSeconds = null; } return this; }"))
                    .setJavadocComment(new Javadoc(parseText(
                        "Set the callDuration property: Duration of the call in seconds."))
                        .addBlockTag("param", "callDuration the callDuration value to set.")
                        .addBlockTag("return", "the AcsCallEndedEventData object itself.")));
            });
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

    private static void addClientLoggerField(String name, ClassOrInterfaceDeclaration clazz) {
        clazz.addFieldWithInitializer("ClientLogger", "LOGGER", parseExpression("new ClientLogger(" + name +".class)"),
            Keyword.PRIVATE, Keyword.STATIC, Keyword.FINAL);
    }

    private void addDeprecationToTypeDeclaration(NodeWithJavadoc<?> typeDeclaration) {
        // Add @Deprecated annotation
        if (typeDeclaration instanceof ClassOrInterfaceDeclaration) {
            ((ClassOrInterfaceDeclaration) typeDeclaration).addMarkerAnnotation("Deprecated");
        } else if (typeDeclaration instanceof EnumDeclaration) {
            ((EnumDeclaration) typeDeclaration).addMarkerAnnotation("Deprecated");
        }
        
        // Get existing Javadoc or create new one
        String existingJavadoc = typeDeclaration.getJavadocComment()
            .map(comment -> comment.getContent())
            .orElse("");
        
        // Append deprecation tag to existing Javadoc
        String deprecationTag = "@deprecated This class is deprecated and may be removed in future releases. System events are now available in the azure-messaging-eventgrid-systemevents package.";
        String newJavadocContent;
        
        if (existingJavadoc.isEmpty()) {
            newJavadocContent = deprecationTag;
        } else {
            // Remove existing asterisks and whitespace, then append
            String cleanedJavadoc = existingJavadoc.trim();
            if (cleanedJavadoc.endsWith("*/")) {
                cleanedJavadoc = cleanedJavadoc.substring(0, cleanedJavadoc.length() - 2).trim();
            }
            if (cleanedJavadoc.startsWith("/**")) {
                cleanedJavadoc = cleanedJavadoc.substring(3).trim();
            }
            if (cleanedJavadoc.startsWith("*")) {
                cleanedJavadoc = cleanedJavadoc.substring(1).trim();
            }
            newJavadocContent = cleanedJavadoc + "\n * " + deprecationTag;
        }
        
        typeDeclaration.setJavadocComment(newJavadocContent);
    }
}
