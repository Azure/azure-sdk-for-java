import com.azure.autorest.customization.*;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the customization code to customize the AutoRest generated code for App Configuration.
 */
public class JobRouterSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.communication.jobrouter");
        ClassCustomization classCustomizationForJobRouterAdministrationClientBuilder = packageCustomization.getClass("JobRouterAdministrationClientBuilder");

        addAuthTraits(classCustomizationForJobRouterAdministrationClientBuilder);
        addConnectionStringClientMethod(classCustomizationForJobRouterAdministrationClientBuilder, "JobRouterAdministrationClientBuilder");
        addHttpPipelineAuthPolicyMethod(classCustomizationForJobRouterAdministrationClientBuilder);
        updateHttpPipelineMethod(classCustomizationForJobRouterAdministrationClientBuilder);

        logger.info("Customizing the JobRouterClientBuilder class");
        ClassCustomization classCustomizationForJobRouterClientBuilder = packageCustomization.getClass("JobRouterClientBuilder");

        addAuthTraits(classCustomizationForJobRouterClientBuilder);
        addConnectionStringClientMethod(classCustomizationForJobRouterClientBuilder, "JobRouterClientBuilder");
        addHttpPipelineAuthPolicyMethod(classCustomizationForJobRouterClientBuilder);
        updateHttpPipelineMethod(classCustomizationForJobRouterClientBuilder);

        logger.info("Customizing the ScoringRuleOptions class");
        PackageCustomization modelsPackageCustomization = customization.getPackage("com.azure.communication.jobrouter.models");
        ClassCustomization classCustomizationForScoringRuleOptions = modelsPackageCustomization.getClass("ScoringRuleOptions");
        classCustomizationForScoringRuleOptions
            .getMethod("setIsBatchScoringEnabled")
            .setModifier(Modifier.PRIVATE);
    }

    private void addAuthTraits(ClassCustomization classCustomization) {
        classCustomization.addImports("com.azure.core.client.traits.TokenCredentialTrait");
        classCustomization.addImports("com.azure.core.client.traits.KeyCredentialTrait");
        classCustomization.addImports("com.azure.core.client.traits.ConnectionStringTrait");
        classCustomization.customizeAst(compilationUnit -> {
            compilationUnit.getClassByName(classCustomization.getClassName()).ifPresent(builderClass -> {
                ClassOrInterfaceDeclaration clientBuilderClass = builderClass.asClassOrInterfaceDeclaration();
                NodeList<ClassOrInterfaceType> implementedTypes = clientBuilderClass.getImplementedTypes();
                boolean hasTokenCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"));
                if (!hasTokenCredentialTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("TokenCredentialTrait<%s>", classCustomization.getClassName()));
                }

                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"));
                if (!hasKeyCredentialTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("KeyCredentialTrait<%s>", classCustomization.getClassName()));
                }

                boolean hasConnectionStringTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait"));
                if (!hasConnectionStringTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("ConnectionStringTrait<%s>", classCustomization.getClassName()));
                }
            });
        });
    }

    private void addConnectionStringClientMethod(ClassCustomization classCustomization, String methodReturnType) {
        classCustomization.addImports("com.azure.core.credential.AzureKeyCredential");
        classCustomization.addMethod(
            "public "+methodReturnType+" connectionString(String connectionString) {" +
                "CommunicationConnectionString connection = new CommunicationConnectionString(connectionString);"+
                "this.credential(new AzureKeyCredential(connection.getAccessKey()));"+
                "this.endpoint(connection.getEndpoint());"+
                "return this;"+
                "}",
            List.of(
                "com.azure.communication.common.implementation.CommunicationConnectionString"
            ));

        classCustomization
            .getMethod("connectionString")
            .getJavadoc()
            .setDescription("Set a connection string for authorization.\n" +
                "@param connectionString valid connectionString as a string.\n" +
                "@return the updated " + methodReturnType + " object.");
    }

    private void addHttpPipelineAuthPolicyMethod(ClassCustomization classCustomization) {
        classCustomization.addImports("com.azure.communication.common.implementation.HmacAuthenticationPolicy");
        classCustomization.addMethod(
            "private HttpPipelinePolicy createHttpPipelineAuthPolicy() {" +
                "        if (this.tokenCredential != null) {" +
                "            return new BearerTokenAuthenticationPolicy(this.tokenCredential, \"https://communication.azure.com/.default\");" +
                "        } else if (this.keyCredential != null) {" +
                "            return new HmacAuthenticationPolicy(new AzureKeyCredential(this.keyCredential.getKey()));" +
                "        } else {" +
                "            throw LOGGER.logExceptionAsError(" +
                "                new IllegalStateException(\"Missing credential information while building a client.\"));" +
                "        }" +
                "    }");
    }

    private void updateHttpPipelineMethod(ClassCustomization classCustomization) {
        MethodCustomization methodCustomization = classCustomization.getMethod("createHttpPipeline");
        methodCustomization.replaceBody("Configuration buildConfiguration" +
            "            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;" +
            "        HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;" +
            "        ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;" +
            "        List<HttpPipelinePolicy> policies = new ArrayList<>();" +
            "        String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");" +
            "        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");" +
            "        String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);" +
            "        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));" +
            "        policies.add(new RequestIdPolicy());" +
            "        policies.add(new AddHeadersFromContextPolicy());" +
            "        HttpHeaders headers = new HttpHeaders();" +
            "        localClientOptions.getHeaders()" +
            "            .forEach(header -> headers.set(HttpHeaderName.fromString(header.getName()), header.getValue()));" +
            "        if (headers.getSize() > 0) {" +
            "            policies.add(new AddHeadersPolicy(headers));" +
            "        }" +
            "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)" +
            "            .forEach(p -> policies.add(p));" +
            "        HttpPolicyProviders.addBeforeRetryPolicies(policies);" +
            "        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));" +
            "        policies.add(new AddDatePolicy());" +
            "        policies.add(createHttpPipelineAuthPolicy());" +
            "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)" +
            "            .forEach(p -> policies.add(p));" +
            "        HttpPolicyProviders.addAfterRetryPolicies(policies);" +
            "        policies.add(new HttpLoggingPolicy(localHttpLogOptions));" +
            "        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))" +
            "            .httpClient(httpClient).clientOptions(localClientOptions).build();" +
            "        return httpPipeline;");
    }
}
