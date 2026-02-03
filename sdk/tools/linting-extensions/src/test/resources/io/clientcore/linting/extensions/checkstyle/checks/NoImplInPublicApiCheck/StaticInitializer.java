package io.clientcore.linting.extensions.checkstyle.checks.NoImplInPublicApiCheck;

import com.azure.implementation.TheClass;
import com.azure.implementation.TheOtherClass;

public class AClass {
    static {
        TheClass theClass = new TheClass() {
            @Override
            public TheOtherClass getTheOtherClass() {
                return null;
            }
        };
    }
}
