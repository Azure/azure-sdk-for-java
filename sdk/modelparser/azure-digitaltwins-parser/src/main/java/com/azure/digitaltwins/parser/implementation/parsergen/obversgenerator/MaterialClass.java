// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.Novelty;
import com.azure.digitaltwins.parser.implementation.codegen.Multiplicity;
import com.azure.digitaltwins.parser.implementation.codegen.Mutability;
import com.azure.digitaltwins.parser.implementation.parsergen.TypeGenerator;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialClassDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.StringRestriction;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents a class that is materialized in the parser object model.
 */
public class MaterialClass implements TypeGenerator {
    private final ClientLogger logger = new ClientLogger(MaterialClass.class);

    private String typeName;
    private String baseTypeName;
    private String kindEnum;
    private String kindProperty;
    private String className;
    private String baseClassName;
    private String kindValue;
    private Map<Integer, List<ExtensibleMaterialClass>> extensibleMaterialClasses;
    private List<DescendantControl> descendantControls;

    private MaterialClassDigest materialClassDigest;
    private boolean isAbstract;
    private boolean isOvert;
    private boolean isAugmentable;
    private boolean isPartition;
    private String parentClass;
    private List<String> typeIds;
    private Map<Integer, List<String>> extensibleMaterialSubtypes;
    private List<String> standardElementIds;

    private Map<Integer, List<ConcreteSubclass>> concreteSubclasses;
    private Map<Integer, List<ConcreteSubclass>> elementalSubclasses;

    private List<MaterialProperty> properties;

    /**
     * Initializes a new instance of the {@link MaterialClass} class.
     *
     * @param typeName The name of the class.
     * @param parentTypeName The name of the parent type.
     * @param baseTypeName The name of the base type of all entities.
     * @param materialClassDigest A {@link MaterialClassDigest} object containing digested information about the material class.
     * @param contexts A map that maps from a context Id to a map of term definitions.
     * @param classIdentifierDefinitionRestrictions A map that maps from class name to a map that maps from DTDL version to a {@link StringRestriction} object that restricts the identifiers for the class.
     * @param extensibleMaterialClasses A map from DTDL version to a list of {@link ExtensibleMaterialClass} objects.
     * @param descendantControls A list of objects that implement the {@link DescendantControl} interface.
     */
    public MaterialClass(
        String typeName,
        String parentTypeName,
        String baseTypeName,
        MaterialClassDigest materialClassDigest,
        Map<String, Map<String, String>> contexts,
        Map<String, Map<Integer, StringRestriction>> classIdentifierDefinitionRestrictions,
        Map<Integer, List<ExtensibleMaterialClass>> extensibleMaterialClasses,
        List<DescendantControl> descendantControls) throws Exception {

        this.typeName = typeName;
        this.baseTypeName = baseTypeName;
        this.kindEnum = NameFormatter.formatNameAsEnum(baseTypeName);
        this.kindProperty = NameFormatter.formatNameAsEnumProperty(baseTypeName);
        this.className = NameFormatter.formatNameAsClass(typeName);
        this.baseClassName = NameFormatter.formatNameAsClass(baseTypeName);
        this.kindValue = this.kindEnum.concat(".").concat(NameFormatter.formatNameAsEnumValue(typeName));
        this.extensibleMaterialClasses = extensibleMaterialClasses;
        this.descendantControls = descendantControls;

        this.materialClassDigest = materialClassDigest;
        this.isAbstract = materialClassDigest.isAbstract();
        this.isOvert = materialClassDigest.isOvert();
        this.isAugmentable = !materialClassDigest.isAbstract();
        this.isPartition = materialClassDigest.isPartition();
        this.parentClass = parentTypeName != null ? NameFormatter.formatNameAsClass(parentTypeName) : null;
        this.typeIds = materialClassDigest.getTypeIds();
        this.extensibleMaterialSubtypes = createExtensibleMaterialSubtypes(this.materialClassDigest);
        this.standardElementIds = materialClassDigest.getStandardElementIds().getOrDefault(2, null);

        this.concreteSubclasses = new HashMap<>();
        this.elementalSubclasses = new HashMap<>();

        for (int dtdlVersion : this.materialClassDigest.getDtdlVersions()) {
            List<ConcreteSubclass> concreteSubclasses = new ArrayList<>();
            List<ConcreteSubclass> elementalSubclasses = new ArrayList<>();

            if (materialClassDigest.getConcreteSubclasses().containsKey(dtdlVersion)) {
                List<String> elementalSubclassNames = materialClassDigest.getElementalSubclasses().get(dtdlVersion);
                for (String subclassName : materialClassDigest.getConcreteSubclasses().get(dtdlVersion)) {
                    ConcreteSubclass concreteSubclass = new ConcreteSubclass(dtdlVersion, subclassName, this.kindEnum, contexts, classIdentifierDefinitionRestrictions);
                    concreteSubclasses.add(concreteSubclass);
                    if (elementalSubclassNames.contains(subclassName)) {
                        elementalSubclasses.add(concreteSubclass);
                    }
                }
            }

            this.concreteSubclasses.put(dtdlVersion, concreteSubclasses);
            this.elementalSubclasses.put(dtdlVersion, elementalSubclasses);
        }

        MaterialPropertyFactory materialPropertyFactory = new MaterialPropertyFactory(this.materialClassDigest.getDtdlVersions(), contexts, this.baseClassName);
        this.properties = new ArrayList<>();
        for (Map.Entry<String, MaterialPropertyDigest> kvp : materialClassDigest.getProperties().entrySet()) {
            this.properties.add(materialPropertyFactory.create(kvp.getKey(), kvp.getValue()));
        }

        // TODO: uncomment once the material factory creates actual values.
        this.properties.sort(Comparator.comparing(MaterialProperty::getPropertyName));

        if (this.parentClass == null) {
            this.properties.add(new InternalProperty(
                ParserGeneratorStringValues.IDENTIFIER_TYPE,
                ParserGeneratorStringValues.IDENTIFIER_NAME,
                Access.PUBLIC,
                NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.IDENTIFIER_NAME),
                "Gets the value of the '@id' property of the DTDL element that corresponds to this object.",
                true));

            this.properties.add(new InternalProperty(
                ParserGeneratorStringValues.IDENTIFIER_TYPE,
                ParserGeneratorStringValues.DEFINING_PARENT_NAME,
                Access.PUBLIC,
                NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.DEFINING_PARENT_NAME),
                "Gets the value of the '@id' property of the parent DTDL element in which this element is defined.",
                false));

            this.properties.add(new InternalProperty(
                ParserGeneratorStringValues.IDENTIFIER_TYPE,
                ParserGeneratorStringValues.DEFINING_PARTITION_NAME,
                Access.PUBLIC,
                NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.DEFINING_PARTITION_NAME),
                "Gets the value of the '@id' property of the partition DTDL element in which this element is defined.",
                false));

            this.properties.add(new InternalProperty(
                NameFormatter.formatNameAsEnum(this.baseTypeName),
                NameFormatter.formatNameAsEnumProperty(this.baseTypeName),
                Access.PUBLIC,
                NameFormatter.formatNameAsEnumProperty(this.baseTypeName),
                "Gets the kind of ".concat(this.baseTypeName).concat("."),
                true));

            this.properties.add(new InternalProperty(
                ParserGeneratorStringValues.OBVERSE_TYPE_INTEGER,
                ParserGeneratorStringValues.DTDL_VERSION_PROPERTY_NAME,
                Access.PACKAGE_PRIVATE,
                NameFormatter.formatNameAsParameter(ParserGeneratorStringValues.DTDL_VERSION_PROPERTY_NAME),
                "Gets the DTDL version in which this element is defined.",
                false));
        }

        // TODO: remove once class is fully implemented.
        logger.info(String.format("%s", this.kindProperty));
        logger.info(String.format("%s", this.kindValue));
        logger.info(String.format("%s", this.extensibleMaterialClasses));
        logger.info(String.format("%s", this.descendantControls));
        logger.info(String.format("%s", this.isOvert));
        logger.info(String.format("%s", this.isAugmentable));
        logger.info(String.format("%s", this.isPartition));
        logger.info(String.format("%s", this.typeIds));
        logger.info(String.format("%s", this.extensibleMaterialSubtypes));
        logger.info(String.format("%s", this.standardElementIds));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCode(JavaLibrary parserLibrary) {
        //boolean anyObjectProperties = this.properties.stream().anyMatch(p -> p.getPropertyKind() == PropertyKind.OBJECT);

        JavaClass obverseClass = parserLibrary.jClass(
            Access.PUBLIC,
            this.isAbstract ? Novelty.ABSTRACT : Novelty.NORMAL,
            this.className,
            Multiplicity.INSTANCE,
            this.parentClass,
            getImplementingInterfaces());

        obverseClass.addSummary("Class {@link ".concat(this.className).concat("} corresponds to an element of type ").concat(this.typeName).concat(" in a DTDL model."));

        if (this.parentClass == null) {
            obverseClass.addRemarks("This is a base class for all classes that correspond to elements in DTDL models.");
        }

        JavaScope staticDeclaration = obverseClass.staticDeclaration();
        generateVersionlessTypes(obverseClass, staticDeclaration);
        generateConcreteKinds(obverseClass, staticDeclaration);
        generateBadTypeFormatStrings(obverseClass, staticDeclaration);

        // TODO: azabbasi: MaterialClassEqualizer needs to be implemented.
        // MaterialClassEqualizer.addMembers(obverseClass, this.className, this.baseClassName, this.kindProperty, this.parentClass == null, this.isAugmentable, this.properties);

        if (!this.isAbstract) {
            this.generateConstructor(obverseClass, true);
        }

        this.generateConstructor(obverseClass, false);

        // TODO: Once concrete subclass has members implement
        for (Map.Entry<Integer, List<ConcreteSubclass>> kvp : this.concreteSubclasses.entrySet()) {
            for (ConcreteSubclass concreteSubclass : kvp.getValue()) {
                concreteSubclass.addMembers(obverseClass, this.typeName);
            }
        }

        for (MaterialProperty materialProperty : this.properties) {
            materialProperty.addMembers(this.materialClassDigest.getDtdlVersions(), obverseClass, this.isAugmentable);
        }

        // TODO: implement the rest.
    }

    private void generateConstructor(JavaClass obverseClass, boolean isConcrete) {
        // TODO: azabbasi: implement the constructor.
    }

    private void generateBadTypeFormatStrings(JavaClass obverseClass, JavaScope staticDeclaration) {
        obverseClass.field(
            Access.PRIVATE,
            "Map<Integer, String>",
            "BAD_TYPE_ACTION_FORMAT",
            "new HashMap<>()",
            Multiplicity.STATIC,
            Mutability.FINAL,
            null);

        obverseClass.field(
            Access.PRIVATE,
            "Map<Integer, String>",
            "BAD_TYPE_CAUSE_FORMAT",
            "new HashMap<>()",
            Multiplicity.STATIC,
            Mutability.FINAL,
            null);

        for (Map.Entry<Integer, String> kvp : this.materialClassDigest.getBadTypeActionFormat().entrySet()) {
            staticDeclaration.line("BAD_TYPE_ACTION_FORMAT.put("
                .concat(String.valueOf(kvp.getKey()))
                .concat(", \"")
                .concat(kvp.getValue())
                .concat("\");"));
        }

        staticDeclaration.jBreak();

        for (Map.Entry<Integer, String> kvp : this.materialClassDigest.getBadTypeCauseFormat().entrySet()) {
            staticDeclaration.line("BAD_TYPE_CAUSE_FORMAT.put("
                .concat(String.valueOf(kvp.getKey()))
                .concat(", \"")
                .concat(kvp.getValue())
                .concat("\");"));
        }
    }

    private void generateConcreteKinds(JavaClass obverseClass, JavaScope staticDeclaration) {
        obverseClass.field(
            Access.PRIVATE,
            "Map<Integer, Set<" + this.kindEnum + ">>",
            "CONCRETE_KINDS",
            "new HashMap<>()",
            Multiplicity.STATIC,
            Mutability.FINAL,
            null);

        for (int dtdlVersion : this.materialClassDigest.getDtdlVersions()) {
            staticDeclaration.line("CONCRETE_KINDS.put(" + dtdlVersion + ", new HashSet<>());");

            List<ConcreteSubclass> concreteSubclasses = this.concreteSubclasses.get(dtdlVersion);

            if (concreteSubclasses != null) {
                JavaSorted sorted = staticDeclaration.sorted();
                for (ConcreteSubclass concreteSubclass : concreteSubclasses) {
                    concreteSubclass.addEnumValue(sorted, "CONCRETE_KINDS.get(" + dtdlVersion + ")");
                }
            }

            staticDeclaration.jBreak();
        }
    }

    private void generateVersionlessTypes(JavaClass javaClass, JavaScope staticDeclaration) {
        javaClass.field(Access.PRIVATE, "HashSet<String>", "VERSION_LESS_TYPES", "new HashSet<>()", Multiplicity.STATIC, Mutability.FINAL, null);

        for (String typeId : this.typeIds) {
            staticDeclaration.line("VERSION_LESS_TYPES.add(\"" + typeId + "\");");
        }

        staticDeclaration.jBreak();
    }

    private String getImplementingInterfaces() {
        StringBuilder implementingBuilder = new StringBuilder();

        implementingBuilder.append("TypeChecker, ");

        if (this.isAugmentable && this.properties.stream().anyMatch(p -> p.getPropertyKind() == PropertyKind.OBJECT)) {
            implementingBuilder.append("PropertyValueConstrainer, PropertyInstanceBinder, ");
        }

        implementingBuilder.append("Equatable<").append(this.className).append(">");

        return implementingBuilder.toString();
    }

    private static Map<Integer, List<String>> createExtensibleMaterialSubtypes(MaterialClassDigest materialClassDigest) {
        Map<Integer, List<String>> result = new HashMap<>();

        materialClassDigest.getDtdlVersions()
            .stream()
            .forEach(s ->  result.put(
                s,
                materialClassDigest.getExtensibleMaterialSubclasses().getOrDefault(s, new ArrayList<>())));

        return result;
    }
}
