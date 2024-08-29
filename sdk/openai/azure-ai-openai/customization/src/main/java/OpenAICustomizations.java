import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.ConstructorCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.JavadocCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class OpenAICustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // remove unused class (no reference to them, after partial-update)
        removeMultipartFormDataFiles(customization, logger);
        customizeEmbeddingEncodingFormatClass(customization, logger);
        customizeEmbeddingsOptions(customization, logger);
    }

    private void removeMultipartFormDataFiles(LibraryCustomization customization, Logger logger) {
        logger.info("Removing FileDetails and MultipartFormDataHelper classes");
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/openai/models/FileDetails.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/openai/implementation/MultipartFormDataHelper.java");
    }

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

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
