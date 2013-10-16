/**
 * Copyright Microsoft Corporation
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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The Class ChannelSettings.
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class OriginSettings {

    /** The preview. */
    private PreviewEndPointSettings preview;

    /** The ingest. */
    private IngestEndpointSettings ingest;

    /**
     * Gets the preview.
     * 
     * @return the preview
     */
    @JsonProperty("Preview")
    public PreviewEndPointSettings getPreview() {
        return this.preview;
    }

    /**
     * Sets the preview.
     * 
     * @param preview
     *            the preview
     * @return the channel settings
     */
    @JsonProperty("Preview")
    public OriginSettings setPreview(PreviewEndPointSettings preview) {
        this.preview = preview;
        return this;
    }

    /**
     * Gets the ingest.
     * 
     * @return the ingest
     */
    @JsonProperty("Ingest")
    public IngestEndpointSettings getIngest() {
        return this.ingest;
    }

    /**
     * Sets the ingest.
     * 
     * @param ingest
     *            the ingest
     * @return the channel settings
     */
    @JsonProperty("Ingest")
    public OriginSettings setIngest(IngestEndpointSettings ingest) {
        this.ingest = ingest;
        return this;
    }

}
