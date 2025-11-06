import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;


/**
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 */
public class AgentsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        renameImageGenSizeEnums(libraryCustomization, logger);
    }

    /**
     * Customization for enum values that are originally numbers and get transliterated by the emitter.
     * @param customization
     * @param logger
     */
    private void renameImageGenSizeEnums(LibraryCustomization customization, Logger logger) {
        logger.info("Renaming enum ImageGenToolSize variants");
        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
                .ifPresent(clazz -> clazz.getEntries().stream()
                        .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                        .forEach(entry -> entry.setName("SIZE_1024_X_1024"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
                .ifPresent(clazz -> clazz.getEntries().stream()
                        .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_FIVE_THREE_SIX".equals(entry.getName().getIdentifier()))
                        .forEach(entry -> entry.setName("SIZE_1024_X_1536"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
                .ifPresent(clazz -> clazz.getEntries().stream()
                        .filter(entry -> "ONE_FIVE_THREE_SIXX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                        .forEach(entry -> entry.setName("SIZE_1536_X_1024"))));
    }
}