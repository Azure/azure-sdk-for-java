package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Test;

public class JavadocThrowsChecksTests extends AbstractModuleTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/JavadocThrowsChecks";
    }

    @Test
    public void testSimpleInvalidThrow() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(JavadocThrowsChecks.class);
        verify(checkConfig, getPath("SimpleInvalidThrow.java"), null);
    }
}
