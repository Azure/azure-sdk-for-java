import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the TypeSpec generated code for Text Translation Client Builder.
 */
public class TextTranslationClientBuilderCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the TextTranslationClientBuilder class");
        customization.getClass("com.azure.ai.translation.text", "TextTranslationClientBuilder").customizeAst(ast -> {
            // add KeyCredentialTrait and TokenCredentialTrait imports
            ast.addImport("com.azure.core.client.traits.KeyCredentialTrait");
            ast.addImport("com.azure.core.client.traits.TokenCredentialTrait");

            ast.getClassByName("TextTranslationClientBuilder").ifPresent(clazz -> {
                NodeList<ClassOrInterfaceType> implementedTypes = clazz.getImplementedTypes();
                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .filter(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"))
                    .findFirst()
                    .isPresent();
                if (!hasKeyCredentialTrait) {
                    clazz.addImplementedType("KeyCredentialTrait<TextTranslationClientBuilder>");
                }

                boolean hasTokenCredentialTrait = implementedTypes.stream()
                    .filter(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"))
                    .findFirst()
                    .isPresent();
                if (!hasTokenCredentialTrait) {
                    clazz.addImplementedType("TokenCredentialTrait<TextTranslationClientBuilder>");
                }
            });
        });
    }
}
