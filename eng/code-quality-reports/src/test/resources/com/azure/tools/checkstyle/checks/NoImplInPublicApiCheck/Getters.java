package com.azure;

import com.azure.AClass;
import com.azure.implementation.AnImplementationClass;

public final class MyClass {
    // Getters that return a public API are okay.
    public AClass getPublicAClass() { // ok
        return null;
    }

    protected AClass getProtectedAClass() { // ok
        return null;
    }

    AClass getPackagePriveAClass() { // ok
        return null;
    }

    private AClass getPrivateAClass() { // ok
        return null;
    }

    // Fully-qualified getters that return a public API are okay.
    public com.azure.AClass getFullyQualifiedPublicAClass() { // ok
        return null;
    }

    protected com.azure.AClass getFullyQualifiedProtectedAClass() { // ok
        return null;
    }

    com.azure.AClass getFullyQualifiedPackagePriveAClass() { // ok
        return null;
    }

    private com.azure.AClass getFullyQualifiedPrivateAClass() { // ok
        return null;
    }

    // Getters that return a non-public API are only okay if it isn't public API.
    public AnImplementationClass getPublicAnImplementationClass() { // line 40, column
        return null;
    }

    protected AnImplementationClass getProtectedAnImplementationClass() { // line 44, column
        return null;
    }

    AnImplementationClass getPackagePrivateAnImplementationClass() { // ok
        return null;
    }

    private AnImplementationClass getPrivateAnImplementationClass() { // ok
        return null;
    }

    // Fully-qualified getters that return a non-public API are only okay if it isn't public API.
    public com.azure.implementation.AnImplementationClass getFullyQualifiedPublicAnImplementationClass() { // line 57, column 36
        return null;
    }

    protected com.azure.implementation.AnImplementationClass getFullyQualifiedProtectedAnImplementationClass() { // line 61, column 39
        return null;
    }

    com.azure.implementation.AnImplementationClass getFullyQualifiedPackagePrivateAnImplementationClass() { // ok
        return null;
    }

    private com.azure.implementation.AnImplementationClass getFullyQualifiedPrivateAnImplementationClass() { // ok
        return null;
    }
}