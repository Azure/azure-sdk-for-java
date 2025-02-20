import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;
import com.github.javaparser.StaticJavaParser;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the customization code to customize the AutoRest generated code for Azure AI Inference.
 */
public class InferenceCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // remove unused class (no reference to them, after partial-update)
        PackageCustomization implModels = customization.getPackage("com.azure.ai.inference.implementation.models");
        ClassCustomization embedRequest1 = implModels.getClass("EmbedRequest1");
        embedRequest1.rename("ImageEmbedRequest");
        PackageCustomization inferenceModels = customization.getPackage("com.azure.ai.inference.models");
        inferenceModels.getClass("ChatCompletionsOptions").customizeAst(ast -> {
            ast.addImport("com.azure.ai.inference.implementation.accesshelpers.ChatCompletionsOptionsAccessHelper");

            ast.getClassByName("ChatCompletionsOptions").ifPresent(clazz -> {

                // Add Accessor to ChatCompletionsOptions
                clazz.setMembers(clazz.getMembers()
                    .addFirst(StaticJavaParser.parseBodyDeclaration(String.join("\n", "static {",
                        "    ChatCompletionsOptionsAccessHelper.setAccessor(new ChatCompletionsOptionsAccessHelper.ChatCompletionsOptionsAccessor() {",
                        "        @Override",
                        "        public void setStream(ChatCompletionsOptions options, boolean stream) {",
                        "            options.setStream(stream);",
                        "        }",
                        "    });",
                        "}"))));
            });
        });
        customizeChatCompletionsBaseClasses(customization, logger);
    }

    private void customizeChatCompletionsBaseClasses(LibraryCustomization customization, Logger logger) {
        List<String> classList = Arrays.asList("ChatCompletionsNamedToolChoice", "ChatCompletionsToolCall", "ChatCompletionsToolDefinition");
        for (String className : classList) {
            logger.info("Customizing the {} class", className);
            ClassCustomization namedToolSelectionClass = customization.getPackage("com.azure.ai.inference.models").getClass(className);
            namedToolSelectionClass.setModifier(Modifier.PUBLIC);
        }
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }

}
