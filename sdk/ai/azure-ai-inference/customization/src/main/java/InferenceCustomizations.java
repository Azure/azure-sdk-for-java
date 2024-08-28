import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for Azure AI Inference.
 */
public class InferenceCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // remove unused class (no reference to them, after partial-update)
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/inference/implementation/models/CompleteOptions.java");
        //customizeEmbeddingEncodingFormatClass(customization, logger);
        //customizeEmbeddingsOptions(customization, logger);
    }

    /*
    private void customizeEmbeddingEncodingFormatClass(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the EmbeddingEncodingFormat class");
        ClassCustomization embeddingEncodingFormatClass = customization.getPackage("com.azure.ai.openai.models").getClass("EmbeddingEncodingFormat");
        embeddingEncodingFormatClass.getConstructor("EmbeddingEncodingFormat").setModifier(0);
        embeddingEncodingFormatClass.setModifier(0);
    }

    private void customizeEmbeddingsOptions(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the EmbeddingsOptions class");
        ClassCustomization embeddingsOptionsClass = customization.getPackage("com.azure.ai.openai.models").getClass("EmbeddingsOptions");
        embeddingsOptionsClass.getMethod("getEncodingFormat").setModifier(0);
        embeddingsOptionsClass.getMethod("setEncodingFormat").setModifier(0);
    }
     */

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }

}
