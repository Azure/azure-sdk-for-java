import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
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
        annotateBetaFields(libraryCustomization, logger);
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

    private void annotateBetaFields(LibraryCustomization customization, Logger logger) {
        for (BetaAnnotation entry : BetaAnnotationLoader.load()) {
            String member = entry instanceof BetaMember ? ((BetaMember) entry).getMember() : null;
            logger.info("Annotating {}{} with @Beta: {}", entry.getClassName(),
                member == null ? "" : "#" + member, entry.getDescription());

            int lastDot = entry.getClassName().lastIndexOf('.');
            String packageName = entry.getClassName().substring(0, lastDot);
            String simpleName = entry.getClassName().substring(lastDot + 1);

            customization.getClass(packageName, simpleName).customizeAst(ast -> {
                ast.addImport("com.azure.ai.agents.util.Beta");
                ast.getPrimaryType().ifPresent(type -> {
                    if (member == null) {
                        type.addAnnotation(StaticJavaParser.parseAnnotation("@Beta"));
                    } else {
                        annotateMember(type, member);
                    }
                });
            });
        }
    }

    /**
     * Annotates a field on the given type with {@code @Beta}, along with its associated getter and
     * setter methods. The customization API only exposes class/method handles, but {@code customizeAst}
     * gives full access to the JavaParser AST, so the field can be reached and annotated here.
     */
    private void annotateMember(TypeDeclaration<?> type, String member) {
        boolean found = false;

        for (FieldDeclaration field : type.getFields()) {
            if (field.getVariables().stream().anyMatch(v -> v.getNameAsString().equals(member))) {
                field.addAnnotation(StaticJavaParser.parseAnnotation("@Beta"));
                found = true;
            }
        }

        if (!found) {
            throw new IllegalStateException(
                "Could not find field '" + member + "' on type " + type.getNameAsString() + ".");
        }

        String capitalized = Character.toUpperCase(member.charAt(0)) + member.substring(1);
        for (String accessor : new String[] { "get" + capitalized, "is" + capitalized, "set" + capitalized }) {
            for (MethodDeclaration method : type.getMethodsByName(accessor)) {
                method.addAnnotation(StaticJavaParser.parseAnnotation("@Beta"));
            }
        }
    }
}
