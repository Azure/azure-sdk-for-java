// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.clientcore.linting.extensions.checkstyle.filters.SdkCheckFilter;

/**
 * Common support for determining whether SDK checks apply to a source file.
 */
public abstract class SdkCheck extends AbstractCheck {
    private boolean ignoredSource;

    /** Creates the common state used by an SDK check. */
    protected SdkCheck() {
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        ignoredSource = getFileContents() != null && SdkCheckFilter.isSampleOrTestFile(getFileContents().getFileName());
    }

    /**
     * Whether this file's SDK diagnostics would be discarded by {@code SdkCheckFilter}.
     *
     * @return whether validation can be skipped for this file.
     */
    protected final boolean isIgnoredSource() {
        return ignoredSource;
    }

}
