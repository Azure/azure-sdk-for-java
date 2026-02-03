import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.InitializerDeclaration;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains the customization code to customize the AutoRest generated code for Azure AI Inference.
 */
public class InferenceCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // remove unused class (no reference to them, after partial-update)

        // There used to be a rename of EmbedRequest1 to ImageEmbedRequest but the model is in implementation, so the
        // name really doesn't matter. This was removed to complete migration to JavaParser customizations.
//        PackageCustomization implModels = customization.getPackage("com.azure.ai.inference.implementation.models");
//        ClassCustomization embedRequest1 = implModels.getClass("EmbedRequest1");
//        embedRequest1.rename("ImageEmbedRequest");

        customization.getClass("com.azure.ai.inference.models", "ChatCompletionsOptions").customizeAst(ast -> {
            ast.addImport("com.azure.ai.inference.implementation.accesshelpers.ChatCompletionsOptionsAccessHelper");

            ast.getClassByName("ChatCompletionsOptions").ifPresent(clazz -> clazz.getMembers()
                .add(0, new InitializerDeclaration(true, StaticJavaParser.parseBlock("{"
                    + "ChatCompletionsOptionsAccessHelper.setAccessor(new ChatCompletionsOptionsAccessHelper.ChatCompletionsOptionsAccessor() {"
                    + "    @Override"
                    + "    public void setStream(ChatCompletionsOptions options, boolean stream) {"
                    + "        options.setStream(stream);"
                    + "    }"
                    + "}); }"))));
        });

        customization.getClass("com.azure.ai.inference", "ModelServiceVersion").customizeAst(ast ->
            ast.getEnumByName("ModelServiceVersion").ifPresent(clazz -> clazz.getMethodsByName("getLatest")
                .forEach(method -> method.setBody(StaticJavaParser.parseBlock("{ return V2024_05_01_PREVIEW; }")))));

        customizeChatCompletionsBaseClasses(customization, logger);
    }

    private void customizeChatCompletionsBaseClasses(LibraryCustomization customization, Logger logger) {
        List<String> classList = Arrays.asList("ChatCompletionsNamedToolChoice", "ChatCompletionsToolCall", "ChatCompletionsToolDefinition");
        for (String className : classList) {
            logger.info("Customizing the {} class", className);
            customization.getClass("com.azure.ai.inference.models", className).customizeAst(ast ->
                ast.getClassByName(className).ifPresent(clazz -> clazz.setModifiers(Modifier.Keyword.PUBLIC)));
        }
    }
}
