package com.azure.sdk.build.tool;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing list of build tools that can be executed when the plugin is run.
 */
public class Tools {
    private static final List<Runnable> TOOLS = new ArrayList<>();
    static {
        TOOLS.add(new DependencyCheckerTool());
        TOOLS.add(new AnnotationProcessingTool());
    }

    /**
     * Returns the list of tools available to run.
     * @return The list of tools.
     */
    public static List<Runnable> getTools() {
        return TOOLS;
    }
}
