import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;


/**
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 * Reference: https://github.com/Azure/autorest.java/blob/main/customization-base/README.md
 */
public class AgentsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        renameImageGenToolSize(libraryCustomization, logger);
        modifyPollingStrategies(libraryCustomization, logger);
        addToolboxesV1PreviewEnumValue(libraryCustomization, logger);
    }

    private void renameImageGenToolSize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1024_X_1024"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_FIVE_THREE_SIX".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1024_X_1536"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_FIVE_THREE_SIXX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1536_X_1024"))));
    }

    private void modifyPollingStrategies(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.implementation", "OperationLocationPollingStrategy")
            .customizeAst(ast -> ast.getClassByName("OperationLocationPollingStrategy")
                .ifPresent(clazz -> {
                    clazz.getConstructors().get(1).getBody().getStatements()
                        .set(0, StaticJavaParser.parseStatement("super(PollingUtils.OPERATION_LOCATION_HEADER, AgentsServicePollUtils.withFoundryFeatures(pollingStrategyOptions));"));

                    clazz.addMember(StaticJavaParser.parseMethodDeclaration("@Override public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) { return super.poll(pollingContext, pollResponseType).map(AgentsServicePollUtils::remapStatus); }"));
                }));

        customization.getClass("com.azure.ai.agents.implementation", "SyncOperationLocationPollingStrategy")
            .customizeAst(ast -> ast.getClassByName("SyncOperationLocationPollingStrategy")
                .ifPresent(clazz -> {
                    clazz.getConstructors().get(1).getBody().getStatements()
                        .set(0, StaticJavaParser.parseStatement("super(PollingUtils.OPERATION_LOCATION_HEADER, AgentsServicePollUtils.withFoundryFeatures(pollingStrategyOptions));"));

                    clazz.addMember(StaticJavaParser.parseMethodDeclaration("@Override public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) { return AgentsServicePollUtils.remapStatus(super.poll(pollingContext, pollResponseType)); }"));
                }));
    }

    /**
     * Adds TOOLBOXES_V1_PREVIEW enum value to FoundryFeaturesOptInKeys.
     */
    private void addToolboxesV1PreviewEnumValue(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.models", "FoundryFeaturesOptInKeys")
            .customizeAst(ast -> ast.getEnumByName("FoundryFeaturesOptInKeys")
                .ifPresent(clazz -> {
                    // Check if already present
                    boolean exists = clazz.getEntries().stream()
                        .anyMatch(entry -> "TOOLBOXES_V1_PREVIEW".equals(entry.getName().getIdentifier()));
                    if (!exists) {
                        clazz.addEnumConstant("TOOLBOXES_V1_PREVIEW")
                            .addArgument(StaticJavaParser.parseExpression("\"Toolboxes=V1Preview\""))
                            .setJavadocComment("Enum value Toolboxes=V1Preview.");
                    }
                }));
    }
}
