// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

public class StepVerifierCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(StepVerifierCheck.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/StepVerifierCheck";
    }

    @Test
    public void noStepVerifierSetDefaultTimeout() throws Exception {
        File file = TestUtils.createCheckFile("publicClassImplementsPublicApi", Arrays.asList(
            "package com.azure;",
            "public class MyTestClass {",
            "}"
        ));

        verify(checker, new File[]{file}, file.getAbsolutePath());
    }

    @Test
    public void stepVerifierSetDefaultTimeout() throws Exception {
        File file = TestUtils.createCheckFile("publicClassImplementsPublicApi", Arrays.asList(
            "package com.azure;",
            "public class MyTestClass {",
            "    public void test() {",
            "        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));", // line 4, column 9
            "    }",
            "}"
        ));

        String[] expected = new String[] {
            String.format("%d:%d: %s", 4, 9, StepVerifierCheck.ERROR_MESSAGE)
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }

    @Test
    public void stepVerifierStaticImportSetDefaultTimeout() throws Exception {
        File file = TestUtils.createCheckFile("publicClassImplementsPublicApi", Arrays.asList(
            "package com.azure;",
            "import static reactor.test.StepVerifier.setDefaultTimeout;",
            "public class MyTestClass {",
            "    public void test() {",
            "        setDefaultTimeout(Duration.ofSeconds(10));", // line 5, column 9
            "    }",
            "}"
        ));

        String[] expected = new String[] {
            String.format("%d:%d: %s", 5, 9, StepVerifierCheck.ERROR_MESSAGE)
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }

    @Test
    public void stepVerifierFullyQualifierSetDefaultTimeout() throws Exception {
        File file = TestUtils.createCheckFile("publicClassImplementsPublicApi", Arrays.asList(
            "package com.azure;",
            "public class MyTestClass {",
            "    public void test() {",
            "        reactor.test.StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));", // line 4, column 9
            "    }",
            "}"
        ));

        String[] expected = new String[] {
            String.format("%d:%d: %s", 4, 9, StepVerifierCheck.ERROR_MESSAGE)
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }
}
