/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media.models;

import java.util.List;

/**
 * The Class ListMediaProcessorsResult.
 */
public class ListMediaProcessorsResult {

    /** The media processor infos. */
    private List<MediaProcessorInfo> mediaProcessorInfos;

    /**
     * Gets the media processor infos.
     * 
     * @return the media processor infos
     */
    public List<MediaProcessorInfo> getMediaProcessorInfos() {
        return mediaProcessorInfos;
    }

    /**
     * Sets the media processor infos.
     * 
     * @param mediaProcessorInfos
     *            the media processor infos
     * @return the list media processor result
     */
    public ListMediaProcessorsResult setMediaProcessorInfos(List<MediaProcessorInfo> mediaProcessorInfos) {
        this.mediaProcessorInfos = mediaProcessorInfos;
        return this;
    }

}
