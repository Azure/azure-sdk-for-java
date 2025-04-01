import com.azure.autorest.customization.*;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;

import java.util.List;

/**
 * This class contains the customization code to customize the AutoRest generated code for Azure AI Inference.
 */
public class AIProjectsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // remove unused class (no reference to them, after partial-update)

        ClassCustomization connectionsImpl = customization.getClass(
            "com.azure.ai.projects.implementation",
            "ConnectionsImpl");

        List<Range> toReplace = connectionsImpl.getEditor().searchText(
            connectionsImpl.getFileName(),
            "{endpoint}/agents/v1.0");

        for (Range r : toReplace ) {
            logger.info("customizing");
            connectionsImpl.getEditor().replace(
                connectionsImpl.getFileName(),
                r.getStart(),
                r.getEnd(),
                "{endpoint}"
            );
        }
    }
}
