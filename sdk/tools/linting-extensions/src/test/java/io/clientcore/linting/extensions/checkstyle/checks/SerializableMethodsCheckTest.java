// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.clientcore.linting.extensions.checkstyle.checks.SerializableMethodsCheck.ERR_NO_FROM_JSON;
import static io.clientcore.linting.extensions.checkstyle.checks.SerializableMethodsCheck.ERR_NO_FROM_XML;

/**
 * Tests {@link SerializableMethodsCheck}.
 */
public class SerializableMethodsCheckTest extends AbstractModuleTestSupport {
    private Checker lintingChecker;

    @BeforeEach
    public void setupChecker() throws Exception {
        lintingChecker = createChecker(createModuleConfig(SerializableMethodsCheck.class));
    }

    @AfterEach
    public void teardownChecker() {
        lintingChecker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "io/clientcore/linting/extensions/checkstyle/checks/SerializableMethodsCheck";
    }

    @Test
    public void jsonSerializableWithBothMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("jsonComplete", "package com.azure;",
            "public class JsonComplete implements JsonSerializable {", "    public void toJson() {}",
            "    public static JsonComplete fromJson() { return null; }", "}");

        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void jsonSerializableMissingFromJson() throws Exception {
        File testFile = TestUtils.createCheckFile("jsonMissingFrom", "package com.azure;",
            "public class JsonMissingFrom implements JsonSerializable {", "    public void toJson() {}", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_JSON };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void jsonSerializableWithNonStaticFromJson() throws Exception {
        File testFile = TestUtils.createCheckFile("jsonNonStaticFrom", "package com.azure;",
            "public class JsonNonStaticFrom implements JsonSerializable {", "    public void toJson() {}",
            "    public JsonNonStaticFrom fromJson() { return null; }", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_JSON };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void xmlSerializableWithBothMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("xmlComplete", "package com.azure;",
            "public class XmlComplete implements XmlSerializable {", "    public void toXml() {}",
            "    public static XmlComplete fromXml() { return null; }", "}");

        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void xmlSerializableMissingFromXml() throws Exception {
        File testFile = TestUtils.createCheckFile("xmlMissingFrom", "package com.azure;",
            "public class XmlMissingFrom implements XmlSerializable {", "    public void toXml() {}", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_XML };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void xmlSerializableWithNonStaticFromXml() throws Exception {
        File testFile = TestUtils.createCheckFile("xmlNonStaticFrom", "package com.azure;",
            "public class XmlNonStaticFrom implements XmlSerializable {", "    public void toXml() {}",
            "    public XmlNonStaticFrom fromXml() { return null; }", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_XML };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void bothInterfacesWithAllMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("bothComplete", "package com.azure;",
            "public class BothComplete implements JsonSerializable, XmlSerializable {", "    public void toJson() {}",
            "    public static BothComplete fromJson() { return null; }", "    public void toXml() {}",
            "    public static BothComplete fromXml() { return null; }", "}");

        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void bothInterfacesMissingAllMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("bothMissing", "package com.azure;",
            "public class BothMissing implements JsonSerializable, XmlSerializable {", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_JSON, "2:1: " + ERR_NO_FROM_XML };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void bothInterfacesMissingJsonMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("bothMissingJson", "package com.azure;",
            "public class BothMissingJson implements JsonSerializable, XmlSerializable {", "    public void toXml() {}",
            "    public static BothMissingJson fromXml() { return null; }", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_JSON };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void bothInterfacesMissingXmlMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("bothMissingXml", "package com.azure;",
            "public class BothMissingXml implements JsonSerializable, XmlSerializable {", "    public void toJson() {}",
            "    public static BothMissingXml fromJson() { return null; }", "}");

        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_XML };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void classNotImplementingInterface() throws Exception {
        File testFile = TestUtils.createCheckFile("noInterface", "package com.azure;", "public class NoInterface {",
            "    public void someMethod() {}", "}");

        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void nestedClassWithJsonSerializable() throws Exception {
        File testFile = TestUtils.createCheckFile("nestedJson", "package com.azure;", "public class OuterClass {",
            "    public static class InnerClass implements JsonSerializable {", "        public void toJson() {}",
            "        public static InnerClass fromJson() { return null; }", "    }", "}");

        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void nestedClassMissingMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("nestedMissing", "package com.azure;", "public class OuterClass {",
            "    public static class InnerClass implements JsonSerializable {", "    }", "}");

        String[] expectedErrors = { "3:5: " + ERR_NO_FROM_JSON };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void classWithExtraMethodsAndCorrectSerializationMethods() throws Exception {
        File testFile = TestUtils.createCheckFile("extraMethods", "package com.azure;",
            "public class ExtraMethods implements JsonSerializable {", "    public void toJson() {}",
            "    public static ExtraMethods fromJson() { return null; }", "    public void otherMethod() {}",
            "    public String getData() { return null; }", "}");

        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void abstractClassImplementingJsonSerializable() throws Exception {
        File testFile = TestUtils.createCheckFile("abstractJson", "package com.azure;",
            "public abstract class AbstractJson implements JsonSerializable {", "    public void toJson() {}", "}");

        // Abstract classes should not require fromJson method
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void abstractClassImplementingXmlSerializable() throws Exception {
        File testFile = TestUtils.createCheckFile("abstractXml", "package com.azure;",
            "public abstract class AbstractXml implements XmlSerializable {", "    public void toXml() {}", "}");

        // Abstract classes should not require fromXml method
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void abstractClassImplementingBothInterfaces() throws Exception {
        File testFile = TestUtils.createCheckFile("abstractBoth", "package com.azure;",
            "public abstract class AbstractBoth implements JsonSerializable, XmlSerializable {",
            "    public void toJson() {}", "    public void toXml() {}", "}");

        // Abstract classes should not require fromJson or fromXml methods
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void classExtendsJsonSerializableType() throws Exception {
        File testFile = TestUtils.createCheckFile("extendsJson", "package com.azure;",
            "public interface ExtendsJson extends JsonSerializable {", "    public void toJson() {}", "}");

        // Classes extending JsonSerializable should not require fromJson method
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void classExtendsXmlSerializableType() throws Exception {
        File testFile = TestUtils.createCheckFile("extendsXml", "package com.azure;",
            "public interface ExtendsXml extends XmlSerializable {", "    public void toXml() {}", "}");

        // Classes extending XmlSerializable should not require fromXml method
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void classExtendsJsonSerializableAndImplementsInterface() throws Exception {
        File testFile = TestUtils.createCheckFile("extendsJsonImpl", "package com.azure;",
            "public class ExtendsJsonImpl extends BaseJson implements JsonSerializable {",
            "    public void toJson() {}", "}");

        // Classes extending a non-serializable type but implementing JsonSerializable should require fromJson
        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_JSON };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void concreteClassExtendingAbstractJsonSerializable() throws Exception {
        File testFile = TestUtils.createCheckFile("concreteExtendsAbstract", "package com.azure;",
            "public class ConcreteClass extends AbstractBase implements JsonSerializable {",
            "    public void toJson() {}", "    public static ConcreteClass fromJson() { return null; }", "}");

        // Concrete class implementing JsonSerializable with fromJson should pass
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }

    @Test
    public void concreteClassImplementingJsonSerializableMissingFromJson() throws Exception {
        File testFile = TestUtils.createCheckFile("concreteMissing", "package com.azure;",
            "public class ConcreteMissing extends AbstractBase implements JsonSerializable {",
            "    public void toJson() {}", "}");

        // Concrete class implementing JsonSerializable without fromJson should fail
        // unless it extends from JsonSerializable type
        String[] expectedErrors = { "2:1: " + ERR_NO_FROM_JSON };
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath(), expectedErrors);
    }

    @Test
    public void nestedAbstractClassImplementingJsonSerializable() throws Exception {
        File testFile = TestUtils.createCheckFile("nestedAbstract", "package com.azure;", "public class OuterClass {",
            "    public abstract static class InnerAbstract implements JsonSerializable {",
            "        public void toJson() {}", "    }", "}");

        // Nested abstract classes should not require fromJson method
        verify(lintingChecker, new File[] { testFile }, testFile.getAbsolutePath());
    }
}
