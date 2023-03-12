// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.spring.storage;

public enum DisplayMode {

    /**
     * Some files we can't display in the browser, so we let it be a download.
     * Because people will always want to download, this is the default, and we will always have download be an option.
     */
    DOWNLOAD,

    /**
     * Some files can be previewed in a modal popup window, and this is a nicer experience than opening in a new tab,
     * so we will support that.
     */
    MODAL_POPUP,

    /**
     * Some files are best handled in a new browser window
     */
    NEW_BROWSER_TAB;
}
