import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.ConstructorCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.JavadocCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class ChatCompletionsToolCallCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeChatCompletionsToolCall(customization, logger);
        customizeEmbeddingEncodingFormatClass(customization, logger);
        customizeEmbeddingsOptions(customization, logger);
    }

    public void customizeChatCompletionsToolCall(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the ChatCompletionsToolCall class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.openai.models");
        ClassCustomization classCustomization = packageCustomization.getClass("ChatCompletionsToolCall");

        // Replace JsonTypeInfo annotation
        classCustomization.removeAnnotation("JsonTypeInfo");
        classCustomization.addAnnotation("JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = ChatCompletionsToolCall.class)");

        // Edit constructor
        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("ChatCompletionsToolCall")
            .replaceParameters("@JsonProperty(value = \"id\") String id, @JsonProperty(value = \"type\")String type")
            .replaceBody(joinWithNewline(
                    "this.id = id;",
                    "this.type = type;"));
        JavadocCustomization constructorJavadocCustomization = constructorCustomization.getJavadoc()
            .setParam("type", "the type value to set.");

        // remove unused class (no reference to them, after partial-update)
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/openai/models/FileDetails.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/openai/implementation/MultipartFormDataContentBuilder.java");
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
