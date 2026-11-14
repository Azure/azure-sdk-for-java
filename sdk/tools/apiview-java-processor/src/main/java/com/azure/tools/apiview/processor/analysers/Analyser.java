package com.azure.tools.apiview.processor.analysers;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * We support multiple analysers, to serve different purposes.
 *
 * @see JavaASTAnalyser
 */
@FunctionalInterface
public interface Analyser {

    /**
     * Given the list of files, all of which came from a jar file (either a compiled class jar or a sources jar),
     * perform an analysis on it to populate the given APIListing model class.
     * This class will contain a list of tokens representing the entire public API surface area, as well as a navigation
     * hierarchy.
     *
     * @param allFiles A list of all files from the extracted jar file, some of which won't be relevant and can be
     *      ignored as necessary.
     */
    void analyse(List<Path> allFiles);
}
