// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;


import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;
import com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator.DescendantControl;
import com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator.DescendantControlFactory;
import com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator.ExtensibleMaterialClass;
import com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator.MaterialClass;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for generating parser code base.
 */
public final class CodeGeneratorTask {
    private final ClientLogger logger = new ClientLogger(CodeGeneratorTask.class);

    private final String digestFilePath;
    private final String outputDirectory;

    /**
     * Initializes a new instance of the {@link CodeGeneratorTask} class.
     * @param digestFilePath The path to the digest file.
     * @param outputDirectory The path to the desired output directory.
     */
    public CodeGeneratorTask(String digestFilePath, String outputDirectory) {
        logger.info("Instantiating " + this.getClass().getSimpleName() + " using digest file path: " + digestFilePath + " and output directory: " + outputDirectory);

        if (digestFilePath == null || digestFilePath.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Digest file path cannot be null or empty."));
        }

        if (outputDirectory == null || outputDirectory.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Output directory cannot be null or empty."));
        }

        this.digestFilePath = digestFilePath;
        this.outputDirectory = outputDirectory;
    }

    public boolean run() throws Exception {
        logger.info("Reading metamodel digest file from directory: " + this.digestFilePath);

        String digestText;
        // Attempt to read the digest file.
        try {
            digestText = new String(Files.readAllBytes(Paths.get(this.digestFilePath)), Charset.defaultCharset());
        } catch (IOException ex) {
            logger.error("Failed to read the digest file from: " + this.digestFilePath);
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }

        MetamodelDigest metamodelDigest;

        try {
            metamodelDigest = new MetamodelDigest(digestText);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse the digest file: " + this.digestFilePath);
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }

        // TODO: azabbasi: figure out the generated code package name and the mechanics of building it into the project.
        JavaLibrary parserLibrary = new JavaLibrary(this.outputDirectory, "com.azure.digitaltwins.parser.autogen");
        parserLibrary.jImport("java.util.ArrayList");
        parserLibrary.jImport("java.util.HashMap");
        parserLibrary.jImport("java.util.List");
        parserLibrary.jImport("java.util.Iterator");
        parserLibrary.jImport("java.util.Map");
        parserLibrary.jImport("java.util.HashSet");
        parserLibrary.jImport("java.util.Set");
        parserLibrary.jImport("java.util.regex.Pattern");
        parserLibrary.jImport("com.fasterxml.jackson.databind.ObjectMapper");

        logger.info("Base class is " + metamodelDigest.getBaseClass());

        List<TypeGenerator> typeGenerators = new ArrayList<>();

        typeGenerators.add(new AggregateContextGenerator(metamodelDigest.getContexts(), metamodelDigest.getDtdlVersionsAllowingLocalTerms()));
        typeGenerators.add(new BaseKindEnumGenerator(metamodelDigest.getMaterialClasses(), metamodelDigest.getBaseClass()));
        typeGenerators.add(new MaterialTypeNameCollectionGenerator(metamodelDigest.getMaterialClasses().keySet(), metamodelDigest.getContexts().values()));

        typeGenerators.addAll(generateMaterialClasses(metamodelDigest));

        for (TypeGenerator typeGenerator : typeGenerators) {
            typeGenerator.generateCode(parserLibrary);
        }

        parserLibrary.generate();
        return true;
    }

    private List<TypeGenerator> generateMaterialClasses(MetamodelDigest metamodelDigest) throws Exception {
        String baseKindEnum = NameFormatter.formatNameAsEnum(metamodelDigest.getBaseClass());
        String baseKindProperty = NameFormatter.formatNameAsEnumProperty(metamodelDigest.getBaseClass());

        Map<Integer, List<ExtensibleMaterialClass>> extensibleMaterialClasses = new HashMap<>();

        for (Map.Entry<Integer, List<String>> kvp : metamodelDigest.getExtensibleMaterialClasses().entrySet()) {
            extensibleMaterialClasses.put(kvp.getKey(), new ArrayList<>());

            for (String extensibleMaterialClassName : kvp.getValue()) {
                extensibleMaterialClasses.get(kvp.getKey()).add(new ExtensibleMaterialClass(kvp.getKey(), extensibleMaterialClassName, baseKindEnum));
            }
        }

        DescendantControlFactory descendantControlFactory = new DescendantControlFactory(baseKindEnum, baseKindProperty);
        List<DescendantControl> descendantControls = new ArrayList<>();

        for (DescendantControlDigest descendantControlDigest : metamodelDigest.getDescendantControls()) {
            descendantControls.addAll(descendantControlFactory.create(descendantControlDigest));
        }

        List<TypeGenerator> typeGenerators = new ArrayList<>();
        for (Map.Entry<String, MaterialClassDigest> kvp: metamodelDigest.getMaterialClasses().entrySet()) {
            typeGenerators.add(new MaterialClass(
                kvp.getKey(),
                kvp.getValue().getParentClass(),
                metamodelDigest.getBaseClass(),
                kvp.getValue(),
                metamodelDigest.getContexts(),
                metamodelDigest.getClassIdentifierDefinitionRestrictions(),
                extensibleMaterialClasses,
                descendantControls));
        }

        typeGenerators.add(new MaterialClass(
            ParserGeneratorStringValues.REFERENCE_OBVERSE_NAME,
            metamodelDigest.getBaseClass(),
            metamodelDigest.getBaseClass(),
            new MaterialClassDigest(),
            metamodelDigest.getContexts(),
            metamodelDigest.getClassIdentifierDefinitionRestrictions(),
            extensibleMaterialClasses,
            descendantControls));

        return typeGenerators;
    }
}
