import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.util.List;


/**
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class DocumentIntelligenceCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeAnalzyeResultOperation(customization, logger);
        customizePollingStrategy(customization, logger);
        customizePollingUtils(customization, logger);
    }

    private void customizeAnalzyeResultOperation(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the AnalyzeResultOperation class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.models");
        packageCustomization.getClass("AnalyzeResultOperation")
            .removeAnnotation("Immutable")
            .customizeAst(ast ->
                ast.getClassByName("AnalyzeResultOperation").ifPresent(clazz -> {
                    addOperationIdField(clazz);
                    addOperationIdGetter(clazz);
                    addOperationIdSetter(clazz);
                }));
    }

    private void addOperationIdSetter(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("setOperationId", Modifier.Keyword.PUBLIC)
            .setType("void")
            .addParameter("String", "operationId")
            .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Sets the operationId property: Operation ID."))))
                .addBlockTag("param", "operationId the operationId value to set.")
            )
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "this.operationId = operationId;",
                "}")));
    }

    private void addOperationIdGetter(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("getOperationId", Modifier.Keyword.PUBLIC)
            .setType("String")
            .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Gets the operationId property: Operation ID."))))
                .addBlockTag("return", "the operationId value.")
            )
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "return operationId;",
                "}")));
    }

    private void addOperationIdField(ClassOrInterfaceDeclaration clazz) {
        clazz.addField("String", "operationId", Modifier.Keyword.PRIVATE);
    }

    private void customizePollingUtils(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the PollingUtils class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("PollingUtils").customizeAst(ast ->
            ast.getClassByName("PollingUtils").ifPresent(clazz -> {
                ast.addImport("java.util.regex.Matcher");
                ast.addImport("java.util.regex.Pattern");
                addParseOperationIdMethod(clazz);
            }));
    }

    private void addParseOperationIdMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("parseOperationId", Modifier.Keyword.PRIVATE)
            .setType("String")
            .setModifiers(Modifier.Keyword.STATIC)
            .addParameter("String", "operationLocationHeader")
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "if (CoreUtils.isNullOrEmpty(operationLocationHeader)) {",
                "    return null;",
                "}",
                "Pattern pattern = Pattern.compile(\"[^:]+://[^/]+/documentintelligence/.+/([^?/]+)\");",
                "Matcher matcher = pattern.matcher(operationLocationHeader);",
                "if (matcher.find() && matcher.group(1) != null) {",
                "    return matcher.group(1);",
                "}",
                "return null;",
                "}")));
    }

    private void customizePollingStrategy(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the SyncOperationLocationPollingStrategy class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("SyncOperationLocationPollingStrategy").customizeAst(ast ->
            ast.getClassByName("SyncOperationLocationPollingStrategy").ifPresent(clazz -> {
                ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeResultOperation");
                ast.addImport("static com.azure.ai.documentintelligence.implementation.PollingUtils.parseOperationId");
                addSyncPollOverrideMethod(clazz);
            }));

        logger.info("Customizing the OperationLocationPollingStrategy class");
        packageCustomization.getClass("OperationLocationPollingStrategy").customizeAst(ast ->
            ast.getClassByName("OperationLocationPollingStrategy").ifPresent(clazz -> {
                ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeResultOperation");
                ast.addImport("static com.azure.ai.documentintelligence.implementation.PollingUtils.parseOperationId");
                addAsyncPollOverrideMethod(clazz);
            }));

    }

    private void addAsyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("Mono<PollResponse<T>>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "return super.poll(pollingContext, pollResponseType)",
                "    .map(pollResponse -> {",
                "        String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));",
                "        String operationId = null;",
                "        if (operationLocationHeader != null) {",
                "            operationId = parseOperationId(operationLocationHeader);",
                "        }",
                "        if (pollResponse.getValue() instanceof AnalyzeResultOperation) {",
                "            AnalyzeResultOperation operation = (AnalyzeResultOperation) pollResponse.getValue();",
                "            operation.setOperationId(operationId);",
                "        }",
                "        return pollResponse;",
                "    });",
                "}")));
    }

    private void addSyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.findCompilationUnit().ifPresent(ast ->
            ast.addImport("com.azure.communication.common.implementation.HmacAuthenticationPolicy"));

        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("PollResponse<T>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "PollResponse<T> pollResponse = super.poll(pollingContext, pollResponseType);",
                "String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));",
                "String operationId = null;",
                "if (operationLocationHeader != null) {",
                "    operationId = parseOperationId(operationLocationHeader);",
                "}",
                "if (pollResponse.getValue() instanceof AnalyzeResultOperation) {",
                "    AnalyzeResultOperation operation = (AnalyzeResultOperation) pollResponse.getValue();",
                "    operation.setOperationId(operationId);",
                "}",
                "return pollResponse;",
                "}")));
    }
}
