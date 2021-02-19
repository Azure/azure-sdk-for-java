// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation

import org.spockframework.lang.ISpecificationContext

class TestHelpers {
    static def logTestName(ISpecificationContext specificationContext) {
        String fullTestName = specificationContext.getCurrentFeature().getName().replace(' ', '').toLowerCase()
        if (specificationContext.getCurrentIteration().getEstimatedNumIterations() > 1) {
            fullTestName += "[" + specificationContext.getCurrentIteration().getIterationIndex() + "]"
        }
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }
}
