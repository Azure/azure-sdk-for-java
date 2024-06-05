import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * This class contains the customization code to customize the AutoRest generated code for App Configuration.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class JobRouterSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.communication.jobrouter");
        packageCustomization.getClass("JobRouterAdministrationClientBuilder").customizeAst(ast ->
            ast.getClassByName("JobRouterAdministrationClientBuilder").ifPresent(clazz -> {
                addAuthTraits(clazz);
                addConnectionStringClientMethod(clazz, "JobRouterAdministrationClientBuilder");
                addHttpPipelineAuthPolicyMethod(clazz);
                updateHttpPipelineMethod(clazz);
            }));

        logger.info("Customizing the JobRouterClientBuilder class");
        packageCustomization.getClass("JobRouterClientBuilder").customizeAst(ast ->
            ast.getClassByName("JobRouterClientBuilder").ifPresent(clazz -> {
                addAuthTraits(clazz);
                addConnectionStringClientMethod(clazz, "JobRouterClientBuilder");
                addHttpPipelineAuthPolicyMethod(clazz);
                updateHttpPipelineMethod(clazz);
            }));

        logger.info("Customizing the ScoringRuleOptions class");
        PackageCustomization models = customization.getPackage("com.azure.communication.jobrouter.models");
        ClassCustomization classCustomizationForScoringRuleOptions = models.getClass("ScoringRuleOptions");
        classCustomizationForScoringRuleOptions
            .getMethod("setIsBatchScoringEnabled")
            .rename("setBatchScoringEnabled");

        customizeRouterWorkerSelector(models.getClass("RouterWorkerSelector"));
        customizeReclassifyExceptionAction(models.getClass("ReclassifyExceptionAction"));
        customizeWaitTimeExceptionTrigger(models.getClass("WaitTimeExceptionTrigger"));
        customizeStaticRouterRule(models.getClass("StaticRouterRule"));
        customizeRouterWorker(models.getClass("RouterWorker"));
        customizeRouterQueueSelector(models.getClass("RouterQueueSelector"));
        customizePassThroughWorkerSelectorAttachment(models.getClass("PassThroughWorkerSelectorAttachment"));
        customizeDistributionPolicy(models.getClass("DistributionPolicy"));
        customizeRouterJob(models.getClass("RouterJob"));
        customizeRouterQueue(models.getClass("RouterQueue"));
        customizeRouterQueueStatistics(models.getClass("RouterQueueStatistics"));

        // Add constructors removed by JSON merge patch code generation.
        addConstructor(models.getClass("ConditionalQueueSelectorAttachment"), "RouterRule", "condition",
            "List<RouterQueueSelector>", "queueSelectors");
        addConstructor(models.getClass("ConditionalWorkerSelectorAttachment"), "RouterRule", "condition",
            "List<RouterWorkerSelector>", "workerSelectors");
        addConstructor(models.getClass("ExceptionRule"), "String", "id", "ExceptionTrigger", "trigger",
            "List<ExceptionAction>", "actions");
        addConstructor(models.getClass("ExpressionRouterRule"), "String", "expression");
        addConstructor(models.getClass("FunctionRouterRule"), "String", "functionUri");
        addConstructor(models.getClass("PassThroughQueueSelectorAttachment"), "String", "key", "LabelOperator",
            "labelOperator");
        addConstructor(models.getClass("QueueLengthExceptionTrigger"), "int", "threshold");
        addConstructor(models.getClass("QueueWeightedAllocation"), "double", "weight", "List<RouterQueueSelector>",
            "queueSelectors");
        addConstructor(models.getClass("RouterChannel"), "String", "channelId", "int", "capacityCostPerJob");
        addConstructor(models.getClass("RouterJobNote"), "String", "message");
        addConstructor(models.getClass("RuleEngineQueueSelectorAttachment"), "RouterRule", "rule");
        addConstructor(models.getClass("RuleEngineWorkerSelectorAttachment"), "RouterRule", "rule");
        addConstructor(models.getClass("ScheduleAndSuspendMode"), "OffsetDateTime", "scheduleAt");
        addConstructor(models.getClass("StaticQueueSelectorAttachment"), "RouterQueueSelector", "queueSelector");
        addConstructor(models.getClass("StaticWorkerSelectorAttachment"), "RouterWorkerSelector", "workerSelector");
        addConstructor(models.getClass("WeightedAllocationQueueSelectorAttachment"), "List<QueueWeightedAllocation>",
            "allocations");
        addConstructor(models.getClass("WeightedAllocationWorkerSelectorAttachment"), "List<WorkerWeightedAllocation>",
            "allocations");
        addConstructor(models.getClass("WorkerWeightedAllocation"), "double", "weight", "List<RouterWorkerSelector>",
            "workerSelectors");

        // Add setter for DistributionPolicy.id in JsonMergePatchHelper.
        customization.getPackage("com.azure.communication.jobrouter.implementation")
            .getClass("JsonMergePatchHelper")
            .customizeAst(ast -> ast.getClassByName("JsonMergePatchHelper")
                .flatMap(clazz -> clazz.getMembers()
                    .stream()
                    .filter(BodyDeclaration::isTypeDeclaration)
                    .map(BodyDeclaration::asTypeDeclaration)
                    .filter(type -> "DistributionPolicyAccessor".equals(type.getNameAsString()))
                    .findFirst())
                .ifPresent(type -> type.addMethod("setId")
                    .addParameter("DistributionPolicy", "policy")
                    .addParameter("String", "id")
                    .removeBody()));
    }

    private void addAuthTraits(ClassOrInterfaceDeclaration clazz) {
        clazz.findCompilationUnit().ifPresent(ast -> {
            ast.addImport("com.azure.core.client.traits.TokenCredentialTrait");
            ast.addImport("com.azure.core.client.traits.KeyCredentialTrait");
            ast.addImport("com.azure.core.client.traits.ConnectionStringTrait");
        });

        String className = clazz.getNameAsString();
        NodeList<ClassOrInterfaceType> implementedTypes = clazz.getImplementedTypes();
        boolean hasTokenCredentialTrait = implementedTypes.stream()
            .anyMatch(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"));
        if (!hasTokenCredentialTrait) {
            clazz.addImplementedType("TokenCredentialTrait<" + className + ">");
        }

        boolean hasKeyCredentialTrait = implementedTypes.stream()
            .anyMatch(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"));
        if (!hasKeyCredentialTrait) {
            clazz.addImplementedType("KeyCredentialTrait<" + className + ">");
        }

        boolean hasConnectionStringTrait = implementedTypes.stream()
            .anyMatch(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait"));
        if (!hasConnectionStringTrait) {
            clazz.addImplementedType("ConnectionStringTrait<" + className + ">");
        }
    }

    private void addConnectionStringClientMethod(ClassOrInterfaceDeclaration clazz, String methodReturnType) {
        clazz.findCompilationUnit().ifPresent(ast -> {
            ast.addImport("com.azure.core.credential.AzureKeyCredential");
            ast.addImport("com.azure.communication.common.implementation.CommunicationConnectionString");
        });

        clazz.addMethod("connectionString", Modifier.Keyword.PUBLIC)
            .setType(methodReturnType)
            .addParameter(String.class, "AzureKeyCredential")
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "CommunicationConnectionString connection = new CommunicationConnectionString(connectionString);",
                "this.credential(new AzureKeyCredential(connection.getAccessKey()));",
                "this.endpoint(connection.getEndpoint());",
                "return this;",
                "}")))
            .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set a connection string for authorization."))
                .addBlockTag("param", "connectionString", "valid connectionString as a string.")
                .addBlockTag("return", "the updated " + methodReturnType + " object."));
    }

    private void addHttpPipelineAuthPolicyMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.findCompilationUnit().ifPresent(ast ->
            ast.addImport("com.azure.communication.common.implementation.HmacAuthenticationPolicy"));

        clazz.addMethod("createHttpPipelineAuthPolicy", Modifier.Keyword.PRIVATE)
            .setType("HttpPipelinePolicy")
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "if (this.tokenCredential != null) {",
                "    return new BearerTokenAuthenticationPolicy(this.tokenCredential, \"https://communication.azure.com/.default\");",
                "} else if (this.keyCredential != null) {",
                "    return new HmacAuthenticationPolicy(new AzureKeyCredential(this.keyCredential.getKey()));",
                "} else {",
                "    throw LOGGER.logExceptionAsError(",
                "        new IllegalStateException(\"Missing credential information while building a client.\"));",
                "}",
                "}")));
    }

    private void updateHttpPipelineMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.getMethodsByName("createHttpPipeline").get(0).setBody(StaticJavaParser.parseBlock(String.join("\n",
            "{",
            "Configuration buildConfiguration = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;",
            "HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;",
            "ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;",
            "List<HttpPipelinePolicy> policies = new ArrayList<>();",
            "String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");",
            "String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");",
            "String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);",
            "policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));",
            "policies.add(new RequestIdPolicy());",
            "policies.add(new AddHeadersFromContextPolicy());",
            "HttpHeaders headers = new HttpHeaders();",
            "localClientOptions.getHeaders()",
            "    .forEach(header -> headers.set(HttpHeaderName.fromString(header.getName()), header.getValue()));",
            "if (headers.getSize() > 0) {",
            "    policies.add(new AddHeadersPolicy(headers));",
            "}",
            "this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)",
            "    .forEach(p -> policies.add(p));",
            "HttpPolicyProviders.addBeforeRetryPolicies(policies);",
            "policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));",
            "policies.add(new AddDatePolicy());",
            "policies.add(createHttpPipelineAuthPolicy());",
            "this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)",
            "    .forEach(p -> policies.add(p));",
            "HttpPolicyProviders.addAfterRetryPolicies(policies);",
            "policies.add(new HttpLoggingPolicy(localHttpLogOptions));",
            "HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))",
            "    .httpClient(httpClient).clientOptions(localClientOptions).build();",
            "return httpPipeline;",
            "}")));
    }

    private static void customizeRouterWorkerSelector(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(Duration.class);
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");
            ast.addImport("com.azure.communication.jobrouter.implementation.utils.CustomizationHelper");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            addConstructor(clazz, "String", "key", "LabelOperator", "labelOperator", "RouterValue", "value");

            changeFieldGetterAndSetterType(clazz, "value", "RouterValue", null);
            updateToJsonAndFromJson(clazz, "jsonWriter.writeUntypedField(\"value\", this.value);",
                "jsonWriter.writeJsonField(\"value\", this.value);",
                "deserializedRouterWorkerSelector.value = reader.readUntyped();",
                "deserializedRouterWorkerSelector.value = RouterValue.fromJson(reader);");

            changeFieldGetterAndSetterType(clazz, "expiresAfterSeconds", "Duration", "ExpiresAfter");
            updateToJsonAndFromJson(clazz, "jsonWriter.writeNumberField(\"expiresAfterSeconds\", this.expiresAfterSeconds);",
                "CustomizationHelper.serializeDurationToSeconds(jsonWriter, \"expiresAfterSeconds\", this.expiresAfterSeconds);",
                "= reader.getNullable(JsonReader::getDouble);",
                "= reader.getNullable(CustomizationHelper::deserializeDurationFromSeconds);");
        });
    }

    private static void customizeReclassifyExceptionAction(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            changeFieldGetterAndSetterType(clazz, "labelsToUpsert", "Map<String, RouterValue>", null);
            updateToJsonAndFromJson(clazz, "(writer, element) -> writer.writeUntyped(element));",
                "(writer, element) -> writer.writeJson(element));",
                "Map<String, Object> labelsToUpsert = reader.readMap(reader1 -> reader1.readUntyped());",
                "Map<String, RouterValue> labelsToUpsert = reader.readMap(reader1 -> RouterValue.fromJson(reader1));");
        });
    }

    private static void customizeWaitTimeExceptionTrigger(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(Duration.class);
            ast.addImport("com.azure.communication.jobrouter.implementation.utils.CustomizationHelper");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            addConstructor(clazz, "Duration", "thresholdSeconds");

            changeFieldGetterAndSetterType(clazz, "thresholdSeconds", "Duration", "Threshold");
            updateToJsonAndFromJson(clazz, "jsonWriter.writeDoubleField(\"thresholdSeconds\", this.thresholdSeconds);",
                "CustomizationHelper.serializeDurationToSeconds(jsonWriter, \"thresholdSeconds\", this.thresholdSeconds);",
                "deserializedWaitTimeExceptionTrigger.thresholdSeconds = reader.getDouble();",
                "deserializedWaitTimeExceptionTrigger.thresholdSeconds = CustomizationHelper.deserializeDurationFromSeconds(reader);");
        });
    }

    private static void customizeStaticRouterRule(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            changeFieldGetterAndSetterType(clazz, "value", "RouterValue", null);
            updateToJsonAndFromJson(clazz, "jsonWriter.writeUntypedField(\"value\", this.value);",
                "jsonWriter.writeJsonField(\"value\", this.value);",
                "deserializedStaticRouterRule.value = reader.readUntyped();",
                "deserializedStaticRouterRule.value = RouterValue.fromJson(reader);");
        });
    }

    private static void customizeRouterWorker(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            changeFieldGetterAndSetterType(clazz, "labels", "Map<String, RouterValue>", null);
            updateToJsonAndFromJson(clazz,
                "jsonWriter.writeMapField(\"labels\", this.labels, (writer, element) -> writer.writeUntyped(element));",
                "jsonWriter.writeMapField(\"labels\", this.labels, (writer, element) -> writer.writeJson(element));",
                "Map<String, Object> labels = reader.readMap(reader1 -> reader1.readUntyped());",
                "Map<String, RouterValue> labels = reader.readMap(reader1 -> RouterValue.fromJson(reader1));");

            changeFieldGetterAndSetterType(clazz, "tags", "Map<String, RouterValue>", null);
            updateToJsonAndFromJson(clazz,
                "jsonWriter.writeMapField(\"tags\", this.tags, (writer, element) -> writer.writeUntyped(element));",
                "jsonWriter.writeMapField(\"tags\", this.tags, (writer, element) -> writer.writeJson(element));",
                "Map<String, Object> tags = reader.readMap(reader1 -> reader1.readUntyped());",
                "Map<String, RouterValue> tags = reader.readMap(reader1 -> RouterValue.fromJson(reader1));");
        });
    }

    private static void customizeRouterQueueSelector(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            addConstructor(clazz, "String", "key", "LabelOperator", "labelOperator", "RouterValue", "value");

            changeFieldGetterAndSetterType(clazz, "value", "RouterValue", null);
            updateToJsonAndFromJson(clazz, "jsonWriter.writeUntypedField(\"value\", this.value);",
                "jsonWriter.writeJsonField(\"value\", this.value);",
                "deserializedRouterQueueSelector.value = reader.readUntyped();",
                "deserializedRouterQueueSelector.value = RouterValue.fromJson(reader);");
        });
    }

    private static void customizePassThroughWorkerSelectorAttachment(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(Duration.class);
            ast.addImport("com.azure.communication.jobrouter.implementation.utils.CustomizationHelper");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            addConstructor(clazz, "String", "key", "LabelOperator", "labelOperator");

            changeFieldGetterAndSetterType(clazz, "expiresAfterSeconds", "Duration", "ExpiresAfter");
            updateToJsonAndFromJson(clazz, "jsonWriter.writeNumberField(\"expiresAfterSeconds\", this.expiresAfterSeconds);",
                "CustomizationHelper.serializeDurationToSeconds(jsonWriter, \"expiresAfterSeconds\", this.expiresAfterSeconds);",
                "= reader.getNullable(JsonReader::getDouble);",
                "= reader.getNullable(CustomizationHelper::deserializeDurationFromSeconds);");
        });
    }

    private static void customizeDistributionPolicy(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(Duration.class);
            ast.addImport("com.azure.communication.jobrouter.implementation.utils.CustomizationHelper");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMembers().stream()
                .filter(BodyDeclaration::isInitializerDeclaration)
                .map(BodyDeclaration::asInitializerDeclaration)
                .filter(InitializerDeclaration::isStatic)
                .findFirst()
                .ifPresent(staticInitializer -> {
                    ExpressionStmt setAccessor = (ExpressionStmt) staticInitializer.getBody().getStatement(0);
                    MethodCallExpr setAccessorCall = (MethodCallExpr) setAccessor.getExpression();
                    ObjectCreationExpr anonymousAccessorCreation = (ObjectCreationExpr) setAccessorCall.getArgument(0);
                    anonymousAccessorCreation.addAnonymousClassBody(StaticJavaParser.parseMethodDeclaration(String.join("\n",
                        "@Override",
                        "public void setId(DistributionPolicy policy, String id) {",
                        "    policy.id = id;",
                        "}")));
                });

            changeFieldGetterAndSetterType(clazz, "offerExpiresAfterSeconds", "Duration", "OfferExpiresAfter");
            updateToJsonAndFromJson(clazz, "jsonWriter.writeNumberField(\"offerExpiresAfterSeconds\", this.offerExpiresAfterSeconds);",
                "CustomizationHelper.serializeDurationToSeconds(jsonWriter, \"offerExpiresAfterSeconds\", this.offerExpiresAfterSeconds);",
                "= reader.getNullable(JsonReader::getDouble);",
                "= reader.getNullable(CustomizationHelper::deserializeDurationFromSeconds);");
        });
    }

    private static void customizeRouterJob(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            changeFieldGetterAndSetterType(clazz, "labels", "Map<String, RouterValue>", null);
            updateToJsonAndFromJson(clazz,
                "jsonWriter.writeMapField(\"labels\", this.labels, (writer, element) -> writer.writeUntyped(element));",
                "jsonWriter.writeMapField(\"labels\", this.labels, (writer, element) -> writer.writeJson(element));",
                "Map<String, Object> labels = reader.readMap(reader1 -> reader1.readUntyped());",
                "Map<String, RouterValue> labels = reader.readMap(reader1 -> RouterValue.fromJson(reader1));");

            changeFieldGetterAndSetterType(clazz, "tags", "Map<String, RouterValue>", null);
            updateToJsonAndFromJson(clazz,
                "jsonWriter.writeMapField(\"tags\", this.tags, (writer, element) -> writer.writeUntyped(element));",
                "jsonWriter.writeMapField(\"tags\", this.tags, (writer, element) -> writer.writeJson(element));",
                "Map<String, Object> tags = reader.readMap(reader1 -> reader1.readUntyped());",
                "Map<String, RouterValue> tags = reader.readMap(reader1 -> RouterValue.fromJson(reader1));");
        });
    }

    private static void customizeRouterQueue(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.communication.jobrouter.models.RouterValue");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            changeFieldGetterAndSetterType(clazz, "labels", "Map<String, RouterValue>", null);
            updateToJsonAndFromJson(clazz,
                "jsonWriter.writeMapField(\"labels\", this.labels, (writer, element) -> writer.writeUntyped(element));",
                "jsonWriter.writeMapField(\"labels\", this.labels, (writer, element) -> writer.writeJson(element));",
                "Map<String, Object> labels = reader.readMap(reader1 -> reader1.readUntyped());",
                "Map<String, RouterValue> labels = reader.readMap(reader1 -> RouterValue.fromJson(reader1));");
        });
    }

    private static void customizeRouterQueueStatistics(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(Duration.class);
            ast.addImport("com.azure.communication.jobrouter.implementation.utils.CustomizationHelper");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getFieldByName("estimatedWaitTimeMinutes").get().getVariable(0).setType("Map<Integer, Duration>");
            clazz.getMethodsByName("getEstimatedWaitTimeMinutes").get(0)
                .setType("Map<Integer, Duration>")
                .setName("getEstimatedWaitTime");

            MethodDeclaration toJson = clazz.getMethodsByName("toJson").get(0);
            String body = toJson.getBody().get().toString()
                .replace("jsonWriter.writeMapField(\"estimatedWaitTimeMinutes\", this.estimatedWaitTimeMinutes,",
                    "CustomizationHelper.serializeDurationToMinutesMap(jsonWriter, \"estimatedWaitTimeMinutes\", this.estimatedWaitTimeMinutes);")
                .replace("(writer, element) -> writer.writeDouble(element));", "");
            toJson.setBody(StaticJavaParser.parseBlock(body));

            MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);
            body = fromJson.getBody().get().toString()
                .replace("Map<String, Double> estimatedWaitTimeMinutes = null;",
                    "Map<Integer, Duration> estimatedWaitTimeMinutes = null;")
                .replace("estimatedWaitTimeMinutes = reader.readMap(reader1 -> reader1.getDouble());",
                    "estimatedWaitTimeMinutes = CustomizationHelper.deserializeDurationFromMinutesMap(reader);");
            fromJson.setBody(StaticJavaParser.parseBlock(body));
        });
    }

    private static void changeFieldGetterAndSetterType(ClassOrInterfaceDeclaration clazz, String fieldName, String type,
        String getterAndSetterRename) {
        // Replace the field type.
        VariableDeclarator field = clazz.getFieldByName(fieldName).get().getVariable(0);
        field.setType(type);

        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        MethodDeclaration getter = clazz.getMethodsByName("get" + fieldName).get(0);
        // Replace the getter type.
        getter.setType(type);

        MethodDeclaration setter = clazz.getMethodsByName("set" + fieldName).get(0);
        // Replace the setter type.
        setter.getParameter(0).setType(type);

        if (getterAndSetterRename != null) {
            // Rename the getter and setter.
            getter.setName("get" + getterAndSetterRename);
            setter.setName("set" + getterAndSetterRename);
        }
    }

    private static void updateToJsonAndFromJson(ClassOrInterfaceDeclaration clazz, String toJsonFind,
        String toJsonReplace, String fromJsonFind, String fromJsonReplace) {
        MethodDeclaration toJson = clazz.getMethodsByName("toJson").get(0);
        String body = toJson.getBody().get().toString().replace(toJsonFind, toJsonReplace);
        toJson.setBody(StaticJavaParser.parseBlock(body));

        MethodDeclaration toJsonMergePatch = clazz.getMethodsByName("toJsonMergePatch").get(0);
        body = toJsonMergePatch.getBody().get().toString().replace(toJsonFind, toJsonReplace);
        toJsonMergePatch.setBody(StaticJavaParser.parseBlock(body));

        MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);
        body = fromJson.getBody().get().toString().replace(fromJsonFind, fromJsonReplace);
        fromJson.setBody(StaticJavaParser.parseBlock(body));
    }

    private static void addConstructor(ClassCustomization classCustomization, String... typeAndParamNames) {
        if (typeAndParamNames.length % 2 != 0) {
            throw new IllegalStateException("The number of type and param names must be even.");
        }

        classCustomization.customizeAst(ast ->
            addConstructor(ast.getClassByName(classCustomization.getClassName()).get(), typeAndParamNames));
    }

    private static void addConstructor(ClassOrInterfaceDeclaration clazz, String... typeAndParamNames) {
        if (typeAndParamNames.length % 2 != 0) {
            throw new IllegalStateException("The number of type and param names must be even.");
        }

        ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(
            new NodeList<>(com.github.javaparser.ast.Modifier.publicModifier()), clazz.getNameAsString());

        NodeList<Statement> constructorBody = new NodeList<>();
        Javadoc javadoc = new Javadoc(
            JavadocDescription.parseText("Creates an instance of " + clazz.getNameAsString() + " class."));
        for (int i = 0; i < typeAndParamNames.length; i += 2) {
            String paramType = typeAndParamNames[i];
            String paramName = typeAndParamNames[i + 1];

            constructorDeclaration.addParameter(paramType, paramName);

            constructorBody.add(
                StaticJavaParser.parseStatement("this." + paramName + " = " + paramName + ";"));
            constructorBody.add(
                StaticJavaParser.parseStatement("this.updatedProperties.add(\"" + paramName + "\");"));

            javadoc.addBlockTag(JavadocBlockTag.createParamBlockTag(paramName, "the " + paramName + " value to set."));
        }

        constructorDeclaration.setBody(new BlockStmt(constructorBody)).setJavadocComment(javadoc);

        clazz.getMembers().add(addConstructorPosition(clazz), constructorDeclaration);
    }

    private static int addConstructorPosition(ClassOrInterfaceDeclaration clazz) {
        int constructorIndex = 0;
        NodeList<BodyDeclaration<?>> members = clazz.getMembers();
        for (int i = 0; i < members.size(); i++) {
            BodyDeclaration<?> member = members.get(i);
            if (member.isFieldDeclaration()) {
                constructorIndex = i;
            } else if (member.isConstructorDeclaration()) {
                return i + 1;
            } else if (member.isMethodDeclaration()) {
                return constructorIndex;
            }
        }

        return constructorIndex;
    }
}
