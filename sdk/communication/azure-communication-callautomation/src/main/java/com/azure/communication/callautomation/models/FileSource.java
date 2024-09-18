// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/** The FileSource model. */
@Fluent
public final class FileSource extends PlaySource {
    /*
     * Uri for the audio file to be played
     */
    private String url;

    /**
     * Get the uri property: Uri for the audio file to be played.
     *
     * @return the uri value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the uri property: Uri for the audio file to be played.
     *
     * @param url the uri value to set.
     * @return the FileSourceInternal object itself.
     */
    public FileSource setUrl(String url) {
        this.url = url;
        return this;
    }
}
