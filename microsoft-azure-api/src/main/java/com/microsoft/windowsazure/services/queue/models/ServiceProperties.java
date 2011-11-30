/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.queue.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "StorageServiceProperties")
public class ServiceProperties {
    private Logging logging = new Logging();
    private Metrics metrics = new Metrics();

    @XmlElement(name = "Logging")
    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    @XmlElement(name = "Metrics")
    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public static class Logging {
        private String version;
        private Boolean delete;
        private Boolean read;
        private Boolean write;
        private RetentionPolicy retentionPolicy;

        @XmlElement(name = "RetentionPolicy")
        public RetentionPolicy getRetentionPolicy() {
            return retentionPolicy;
        }

        public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
        }

        @XmlElement(name = "Write")
        public boolean isWrite() {
            return write;
        }

        public void setWrite(boolean write) {
            this.write = write;
        }

        @XmlElement(name = "Read")
        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }

        @XmlElement(name = "Delete")
        public boolean isDelete() {
            return delete;
        }

        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class Metrics {
        private String version;
        private boolean enabled;
        private Boolean includeAPIs;
        private RetentionPolicy retentionPolicy;

        @XmlElement(name = "RetentionPolicy")
        public RetentionPolicy getRetentionPolicy() {
            return retentionPolicy;
        }

        public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
        }

        @XmlElement(name = "IncludeAPIs")
        public Boolean isIncludeAPIs() {
            return includeAPIs;
        }

        public void setIncludeAPIs(Boolean includeAPIs) {
            this.includeAPIs = includeAPIs;
        }

        @XmlElement(name = "Enabled")
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class RetentionPolicy {
        private boolean enabled;
        private Integer days; // nullable, because optional if "enabled" is false

        @XmlElement(name = "Days")
        public Integer getDays() {
            return days;
        }

        public void setDays(Integer days) {
            this.days = days;
        }

        @XmlElement(name = "Enabled")
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
