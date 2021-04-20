// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.azure.digitaltwins.parser.implementation.codegen.JavaMethod;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;
import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.Novelty;
import com.azure.digitaltwins.parser.implementation.codegen.Multiplicity;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.Mutability;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.stream.Collectors;

/**
 * Code generator for class that stores DTDL context term definitions.
 */
public class AggregateContextGenerator implements TypeGenerator {
    private final Map<String, Map<String, String>> context;
    private final List<Integer> dtdlVersionsAllowingLocalTerms;

    /**
     * Initializes a new instance of the {@link AggregateContextGenerator} class.
     *
     * @param context A {@link Map} that maps from a context Id to a map of term definitions.
     * @param dtdlVersionsAllowingLocalTerms A {@link List} of DTDL versions that allow local term definitions in context blocks.
     */
    public AggregateContextGenerator(Map<String, Map<String, String>> context, List<Integer> dtdlVersionsAllowingLocalTerms) {
        this.context = context;
        this.dtdlVersionsAllowingLocalTerms = dtdlVersionsAllowingLocalTerms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(JavaLibrary parserLibrary) {
        JavaClass contextClass = parserLibrary.jClass(Access.PACKAGE_PRIVATE, Novelty.NORMAL, "AggregateContext", Multiplicity.INSTANCE, null, null);
        contextClass.addSummary("Class for parsing and storing information from JSON-LD context blocks.");
        generateFields(contextClass);
        generateStaticConstructor(contextClass);
    }

    private void generateFields(JavaClass contextClass) {
        contextClass.field(
            Access.PRIVATE,
            "HashSet<Integer>",
            "DTDL_VERSIONS_ALLOWING_LOCAL_TERMS",
            String.format(
                "new HashSet<>(Arrays.asList(%s))",
                this.dtdlVersionsAllowingLocalTerms
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "))),
            Multiplicity.STATIC,
            Mutability.FINAL,
            null);
    }

    private void generateStaticConstructor(JavaClass contextClass) {
        JavaConstructor constructor = contextClass.constructor(Access.PACKAGE_PRIVATE, Multiplicity.STATIC);
        constructor.getBody()
            .line("dtdlContextHistory = getDtdlContextHistory();")
            .jBreak()
            .line("affiliateContextHistories = new HashMap<String, ContextHistory>();")
            .jBreak();

        JavaMethod dtdlContextMethod = contextClass.method(Access.PRIVATE, Novelty.NORMAL, "ContextHistory", "getDtdlContextHistory", Multiplicity.STATIC);
        dtdlContextMethod
            .getBody()
            .line("List<VersionedContext> versionedContexts = new ArrayList<>();")
            .jBreak();

        int affiliateCount = 0;
        Map<String, Integer> affiliateIndices = new HashMap<>();
        Map<Integer, JavaMethod> affiliateContextMethods = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> contextPair : this.context.entrySet()) {
            String contextSpecifier = contextPair.getKey();

            if (contextSpecifier.startsWith(ParserGeneratorStringValues.DTDL_CONTEXT_PREFIX)) {
                addContextVersion(dtdlContextMethod.getBody(), contextSpecifier, contextPair.getValue());
            } else {
                String affiliateName = contextSpecifier.substring(0, contextSpecifier.indexOf(";"));
                Integer affiliateIndex = affiliateIndices.get(affiliateName);
                if (affiliateIndex == null) {
                    affiliateIndex = affiliateCount++;
                    affiliateIndices.put(affiliateName, affiliateIndex);

                    JavaMethod affiliateContextMethod = contextClass.method(
                        Access.PRIVATE,
                        Novelty.NORMAL,
                        "ContextHistory",
                        "getAffiliate" + affiliateIndex + "ContextHistory",
                        Multiplicity.STATIC);
                    affiliateContextMethod
                        .getBody()
                        .line("List<VersionedContext> versionedContexts = new ArrayList<>();")
                        .jBreak();

                    affiliateContextMethods.put(affiliateIndex, affiliateContextMethod);
                }

                addContextVersion(affiliateContextMethods.get(affiliateIndex).getBody(), contextSpecifier, contextPair.getValue());
            }
        }

        dtdlContextMethod.getBody().line("return new ContextHistory(versionedContexts);");

        for (Map.Entry<Integer, JavaMethod> affiliateContextMethod : affiliateContextMethods.entrySet()) {
            affiliateContextMethod.getValue().getBody().line("return new ContextHistory(versionedContexts);");
        }

        for (Map.Entry<String, Integer> affiliateIndex : affiliateIndices.entrySet()) {
            constructor.getBody().line("affiliateContextHistories.put(\"" + affiliateIndex.getKey() + "\", getAffiliate" + affiliateIndex.getValue() + "ContextHistory());");
        }
    }

    private void addContextVersion(JavaScope contextMethodBody, String contextSpecifier, Map<String, String> termDefinitions) {
        String versionString = contextSpecifier.substring(contextSpecifier.indexOf(';') + 1);
        int dotIx = versionString.indexOf('.');
        int majorVersion = Integer.parseInt(versionString);
        int minorVersion = dotIx < 0 ? 0 : Integer.parseInt(versionString.substring(dotIx + 1));

        String contextVar = "context".concat(String.valueOf(majorVersion)).concat("_").concat(String.valueOf(minorVersion));

        contextMethodBody.line(String.format("VersionedContext %s = new VersionedContext(%s, %s);", contextVar, majorVersion, minorVersion));

        for (Map.Entry<String, String> kvp : termDefinitions.entrySet()) {
            contextMethodBody
                .line(String.format(
                    "%s.addDefinition(\"%s\", new %s(\"%s\"));",
                    contextVar,
                    kvp.getKey(),
                    ParserGeneratorStringValues.IDENTIFIER_TYPE, kvp.getValue()));
        }

        contextMethodBody
            .line(String.format("versionedContexts.add(%s);", contextVar))
            .jBreak();
    }
}
