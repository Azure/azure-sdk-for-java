import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

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
        customizeCompletionsOptions(customization, logger);
        customizeChatCompletionsOptions(customization, logger);
    }

    private void removeMultipartFormDataFiles(LibraryCustomization customization, Logger logger) {
        logger.info("Removing FileDetails and MultipartFormDataHelper classes");
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/openai/models/FileDetails.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/openai/implementation/MultipartFormDataHelper.java");
    }

    private void customizeEmbeddingEncodingFormatClass(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the EmbeddingEncodingFormat class");
        customization.getClass("com.azure.ai.openai.models", "EmbeddingEncodingFormat").customizeAst(ast ->
            ast.getClassByName("EmbeddingEncodingFormat").ifPresent(clazz -> {
                clazz.getDefaultConstructor().ifPresent(ConstructorDeclaration::setModifiers);
                clazz.setModifiers();
            }));
    }

    private void customizeEmbeddingsOptions(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the EmbeddingsOptions class");
        customization.getClass("com.azure.ai.openai.models", "EmbeddingsOptions").customizeAst(ast ->
            ast.getClassByName("EmbeddingsOptions").ifPresent(clazz -> {
                clazz.getMethodsByName("getEncodingFormat").forEach(MethodDeclaration::setModifiers);
                clazz.getMethodsByName("setEncodingFormat").forEach(MethodDeclaration::setModifiers);
            }));
    }

    private void customizeCompletionsOptions(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the CompletionOptions class");
        customization.getClass("com.azure.ai.openai.models", "CompletionsOptions").customizeAst(ast -> {
            ast.addImport("com.azure.ai.openai.implementation.accesshelpers.CompletionsOptionsAccessHelper");
            ast.getClassByName("CompletionsOptions").ifPresent(clazz -> clazz.getMembers()
                .add(0, new InitializerDeclaration(true, StaticJavaParser.parseBlock("{"
                    + "CompletionsOptionsAccessHelper.setAccessor(new CompletionsOptionsAccessHelper.CompletionsOptionsAccessor() {"
                    + "    @Override"
                    + "    public void setStream(CompletionsOptions options, boolean stream) {"
                    + "        options.setStream(stream);"
                    + "    }"
                    + "    @Override"
                    + "    public void setStreamOptions(CompletionsOptions options, ChatCompletionStreamOptions streamOptions) {"
                    + "        options.setStreamOptions(streamOptions);"
                    + "    }"
                    + "}); }"))));
        });
    }

    private void customizeChatCompletionsOptions(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the ChatCompletionsOptions class");
        customization.getClass("com.azure.ai.openai.models", "ChatCompletionsOptions").customizeAst(ast -> {
            ast.addImport("com.azure.ai.openai.implementation.accesshelpers.ChatCompletionsOptionsAccessHelper");
            ast.getClassByName("ChatCompletionsOptions").ifPresent(clazz -> clazz.getMembers()
                .add(0, new InitializerDeclaration(true, StaticJavaParser.parseBlock("{"
                    + "ChatCompletionsOptionsAccessHelper.setAccessor(new ChatCompletionsOptionsAccessHelper.ChatCompletionsOptionsAccessor() {"
                    + "    @Override"
                    + "    public void setStream(ChatCompletionsOptions options, boolean stream) {"
                    + "        options.setStream(stream);"
                    + "    }"
                    + "    @Override"
                    + "    public void setStreamOptions(ChatCompletionsOptions options, ChatCompletionStreamOptions streamOptions) {"
                    + "        options.setStreamOptions(streamOptions);"
                    + "    }"
                    + "}); }"))));
        });
    }
}
