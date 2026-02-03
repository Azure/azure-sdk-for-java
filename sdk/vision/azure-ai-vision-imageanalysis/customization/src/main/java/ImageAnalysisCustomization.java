import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the TypeSpec generated code for Azure AI Image Analysis.
 */
public class ImageAnalysisCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        updateClientJavadoc(customization.getClass("com.azure.ai.vision.imageanalysis", "ImageAnalysisAsyncClient"),
            "async-client-api-key-auth", "async-client-entra-id-auth");

        updateClientJavadoc(customization.getClass("com.azure.ai.vision.imageanalysis", "ImageAnalysisClient"),
            "sync-client-api-key-auth", "sync-client-entra-id-auth");
    }

    private void updateClientJavadoc(ClassCustomization clientClass, String apiKeyAuthTag, String entraIdAuthTag) {
        clientClass.customizeAst(ast -> ast.getClassByName(clientClass.getClassName()).ifPresent(clazz ->
            clazz.getJavadoc().ifPresent(javadoc -> {
                String codeSnippetTags = String.format("\n<!-- src_embed com.azure.ai.vision.imageanalysis.%1$s -->"
                    + "\n<!-- end com.azure.ai.vision.imageanalysis.%1$s -->"
                    + "\n<!-- src_embed com.azure.ai.vision.imageanalysis.%2$s -->"
                    + "\n<!-- end com.azure.ai.vision.imageanalysis.%2$s -->", apiKeyAuthTag, entraIdAuthTag);
                String javadocDescription = javadoc.getDescription().toText();
                javadocDescription += codeSnippetTags;
                javadoc.getDescription().getElements().clear();
                javadoc.getDescription().getElements()
                    .addAll(JavadocDescription.parseText(javadocDescription).getElements());
                clazz.setJavadocComment(javadoc);
            })));
    }


}
