/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.models;

import java.io.IOException;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The attributes of a secret managed by the KeyVault service
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SecretAttributes {

    /**
     * NotBefore date as the number of seconds since the Unix Epoch (1/1/1970)
     */
    @JsonProperty("nbf")
    private Long notBeforeUnixTime;

    /**
     * Expiry date as the number of seconds since the Unix Epoch (1/1/1970)
     */
    @JsonProperty("exp")
    private Long expiresUnixTime;

    /**
     * Creation time as the number of seconds since the Unix Epoch (1/1/1970)
     */
    @JsonProperty("created")
    private Long createdUnixTime;

    /**
     * Last updated time as the number of seconds since the Unix Epoch
     * (1/1/1970)
     */
    @JsonProperty("updated")
    private Long updatedUnixTime;

    /**
     * Determines whether the secret is enabled
     */
    @JsonProperty("enabled")
    private Boolean enabled;

    /**
     * Optional.
     *
     * @return The Enabled value.
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Optional.
     *
     * @param enabledValue
     *            The Enabled value.
     */
    public void setEnabled(Boolean enabledValue) {
        enabled = enabledValue;
    }

    public Long getNotBeforeUnixTime() {
        return notBeforeUnixTime;
    }

    public void setNotBeforeUnixTime(Long notBeforeUnixTime) {
        this.notBeforeUnixTime = notBeforeUnixTime;
    }

    /**
     * Optional.
     *
     * @return the NotBefore value
     */
    public Date getNotBefore() {
        return fromUnixTime(notBeforeUnixTime);
    }

    /**
     * Optional.
     *
     * @param notBeforeValue
     *            The NotBefore value.
     */
    public void setNotBefore(Date notBeforeValue) {
        notBeforeUnixTime = toUnixTime(notBeforeValue);
    }

    public Long getExpiresUnixTime() {
        return expiresUnixTime;
    }

    public void setExpiresUnixTime(Long expiresUnixTime) {
        this.expiresUnixTime = expiresUnixTime;
    }

    /**
     * Optional.
     *
     * @return the Expires value
     */
    public Date getExpires() {
        return fromUnixTime(expiresUnixTime);
    }

    /**
     * Optional.
     *
     * @param expiresValue
     *            The Expires value.
     */
    public void setExpires(Date expiresValue) {
        expiresUnixTime = toUnixTime(expiresValue);
    }

    public Long getCreatedUnixTime() {
        return createdUnixTime;
    }

    public void setCreatedUnixTime(Long createdUnixTime) {
        this.createdUnixTime = createdUnixTime;
    }

    /**
     * Optional.
     *
     * @return the Created value
     */
    public Date getCreated() {
        return fromUnixTime(createdUnixTime);
    }

    public Long getUpdatedUnixTime() {
        return updatedUnixTime;
    }

    public void setUpdatedUnixTime(Long updatedUnixTime) {
        this.updatedUnixTime = updatedUnixTime;
    }

    /**
     * Optional.
     *
     * @return the Updated value
     */
    public Date getUpdated() {
        return fromUnixTime(updatedUnixTime);
    }

    /**
     * Default constructor The defaults for the properties are Enabled = null
     * NotBefore = null Expires = null Created = null Updated = null
     */
    public SecretAttributes() {
        enabled = null;
        notBeforeUnixTime = null;
        expiresUnixTime = null;
        createdUnixTime = null;
        updatedUnixTime = null;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            throw new IllegalStateException(e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Date fromUnixTime(Long unixTime) {
        if (unixTime == null) {
            return null;
        }
        return new Date(unixTime * 1000);
    }

    private static Long toUnixTime(Date value) {
        if (value == null) {
            return null;
        }
        return value.getTime() / 1000;
    }
}
