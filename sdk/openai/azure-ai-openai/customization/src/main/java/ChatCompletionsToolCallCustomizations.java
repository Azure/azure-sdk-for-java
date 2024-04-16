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

        // Remove type and getter in ChatCompletionsFunctionToolCall
        classCustomization = packageCustomization.getClass("ChatCompletionsFunctionToolCall");
        classCustomization.removeMethod("getType");
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName("ChatCompletionsFunctionToolCall").get();
            clazz.getMembers().removeIf(node -> {
                if (node.isFieldDeclaration()
                        && node.asFieldDeclaration().getVariables() != null && !node.asFieldDeclaration().getVariables().isEmpty()) {
                    return "type".equals(node.asFieldDeclaration().getVariables().get(0).getName().asString());
                }
                return false;
            });
        });

        // remove unused class (no reference to them, after partial-update)
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
