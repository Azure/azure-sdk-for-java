import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

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
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/inference/implementation/models/CompleteOptions.java");
        PackageCustomization implModels = customization.getPackage("com.azure.ai.inference.implementation.models");
        ClassCustomization embedRequest1 = implModels.getClass("EmbedRequest1");
        embedRequest1.rename("ImageEmbedRequest");
        customizeChatCompletionsBaseClasses(customization, logger);
    }

    private void customizeChatCompletionsBaseClasses(LibraryCustomization customization, Logger logger) {
        List<String> classList = Arrays.asList("ChatCompletionsNamedToolSelection", "ChatCompletionsToolCall", "ChatCompletionsToolDefinition");
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
