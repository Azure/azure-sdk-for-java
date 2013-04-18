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
package com.microsoft.windowsazure.services.table.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the Storage service properties that can be set on a storage account, including Windows Azure Storage
 * Analytics. This class is used by the {@link TableContract#getServiceProperties()} method to return the service
 * property values set on the storage account, and by the {@link TableContract#setServiceProperties(ServiceProperties)}
 * and {@link TableContract#setServiceProperties(ServiceProperties, TableServiceOptions)} methods to set the values of
 * the service properties.
 */
@XmlRootElement(name = "StorageServiceProperties")
public class ServiceProperties {
    private Logging logging = new Logging();
    private Metrics metrics = new Metrics();

    /**
     * Gets the value of the logging options on the storage account.
     * 
     * @return
     *         A {@link Logging} instance containing the logging options.
     */
    @XmlElement(name = "Logging")
    public Logging getLogging() {
        return logging;
    }

    /**
     * Sets the value of the logging options on the storage account.
     * 
     * @param logging
     *            A {@link Logging} instance containing the logging options.
     */
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    /**
     * Gets the value of the metrics options on the storage account.
     * 
     * @return
     *         A {@link Metrics} instance containing the metrics options.
     */
    @XmlElement(name = "Metrics")
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Sets the value of the metrics options on the storage account.
     * 
     * @param metrics
     *            A {@link Metrics} instance containing the metrics options.
     */
    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Represents the logging options that can be set on a storage account.
     */
    public static class Logging {
        private String version;
        private boolean delete;
        private boolean read;
        private boolean write;
        private RetentionPolicy retentionPolicy;

        /**
         * Gets the retention policy for logging data set on the storage account.
         * 
         * @return
         *         The {@link RetentionPolicy} set on the storage account.
         */
        @XmlElement(name = "RetentionPolicy")
        public RetentionPolicy getRetentionPolicy() {
            return retentionPolicy;
        }

        /**
         * Sets the retention policy to use for logging data on the storage account.
         * 
         * @param retentionPolicy
         *            The {@link RetentionPolicy} to set on the storage account.
         */
        public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
        }

        /**
         * Gets a flag indicating whether all write requests are logged.
         * 
         * @return
         *         A flag value of <code>true</code> if all write operations are logged; otherwise, <code>false</code>.
         */
        @XmlElement(name = "Write")
        public boolean isWrite() {
            return write;
        }

        /**
         * Sets a flag indicating whether all write requests should be logged.
         * 
         * @param write
         *            Set a flag value of <code>true</code> to log all write operations; otherwise, <code>false</code>.
         */
        public void setWrite(boolean write) {
            this.write = write;
        }

        /**
         * Gets a flag indicating whether all read requests are logged.
         * 
         * @return
         *         A flag value of <code>true</code> if all read operations are logged; otherwise, <code>false</code>.
         */
        @XmlElement(name = "Read")
        public boolean isRead() {
            return read;
        }

        /**
         * Sets a flag indicating whether all read requests should be logged.
         * 
         * @param read
         *            Set a flag value of <code>true</code> to log all read operations; otherwise, <code>false</code>.
         */
        public void setRead(boolean read) {
            this.read = read;
        }

        /**
         * Gets a flag indicating whether all delete requests are logged.
         * 
         * @return
         *         A flag value of <code>true</code> if all delete operations are logged; otherwise, <code>false</code>.
         */
        @XmlElement(name = "Delete")
        public boolean isDelete() {
            return delete;
        }

        /**
         * Sets a flag indicating whether all delete requests should be logged.
         * 
         * @param delete
         *            Set a flag value of <code>true</code> to log all delete operations; otherwise, <code>false</code>.
         */
        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        /**
         * Gets the version of logging configured on the storage account.
         * 
         * @return
         *         A {@link String} containing the version of logging configured on the storage account.
         */
        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        /**
         * Sets the version of logging configured on the storage account.
         * 
         * @param version
         *            A {@link String} containing the version of logging configured on the storage account.
         */
        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * Represents the metrics options that can be set on a storage account.
     */
    public static class Metrics {
        private String version;
        private boolean enabled;
        private Boolean includeAPIs;
        private RetentionPolicy retentionPolicy;

        /**
         * Gets the retention policy for metrics data set on the storage account.
         * 
         * @return
         *         The {@link RetentionPolicy} set on the storage account.
         */
        @XmlElement(name = "RetentionPolicy")
        public RetentionPolicy getRetentionPolicy() {
            return retentionPolicy;
        }

        /**
         * Sets the retention policy to use for metrics data on the storage account.
         * 
         * @param retentionPolicy
         *            The {@link RetentionPolicy} to set on the storage account.
         */
        public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
        }

        /**
         * Gets a flag indicating whether metrics generates summary statistics for called API operations.
         * 
         * @return
         *         A flag value of <code>true</code> if metrics generates summary statistics for called API operations;
         *         otherwise, <code>false</code>.
         */
        @XmlElement(name = "IncludeAPIs")
        public Boolean isIncludeAPIs() {
            return includeAPIs;
        }

        /**
         * Sets a flag indicating whether metrics should generate summary statistics for called API operations. This
         * flag is optional if metrics is not enabled.
         * 
         * @param includeAPIs
         *            Set a flag value of <code>true</code> to generate summary statistics for called API operations;
         *            otherwise, <code>false</code>.
         */
        public void setIncludeAPIs(Boolean includeAPIs) {
            this.includeAPIs = includeAPIs;
        }

        /**
         * Gets a flag indicating whether metrics is enabled for the storage account.
         * 
         * @return
         *         A flag value of <code>true</code> if metrics is enabled for the storage account; otherwise,
         *         <code>false</code>.
         */
        @XmlElement(name = "Enabled")
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets a flag indicating whether to enable metrics for the storage account.
         * 
         * @param enabled
         *            Set a flag value of <code>true</code> to enable metrics for the storage account; otherwise,
         *            <code>false</code>.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Gets the version of Storage Analytics configured on the storage account.
         * 
         * @return
         *         A {@link String} containing the version of Storage Analytics configured on the storage account.
         */
        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        /**
         * Sets the version of Storage Analytics configured on the storage account.
         * 
         * @param version
         *            A {@link String} containing the version of Storage Analytics configured on the storage account.
         */
        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * Represents the optional retention policy that can be applied to logging or metrics on the storage account.
     */
    public static class RetentionPolicy {
        private boolean enabled;
        private Integer days; // nullable, because optional if "enabled" is false

        /**
         * Gets the number of days that metrics or logging data should be retained, if logging is enabled.
         * 
         * @return
         *         The number of days to retain logging or metrics data if logging is enabled, or <code>null</code>.
         */
        @XmlElement(name = "Days")
        public Integer getDays() {
            return days;
        }

        /**
         * Sets the number of days that metrics or logging data should be retained. The minimum value you can specify is
         * 1; the largest value is 365 (one year). This value must be specified even if the enabled flag is set
         * to <code>false</code>.
         * 
         * @param days
         *            The number of days to retain logging or metrics data.
         */
        public void setDays(Integer days) {
            this.days = days;
        }

        /**
         * Gets a flag indicating whether a retention policy is enabled.
         * 
         * @return
         *         A flag value of <code>true</code> if a retention policy is enabled; otherwise, <code>false</code>.
         */
        @XmlElement(name = "Enabled")
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets a flag indicating whether to enable a retention policy.
         * 
         * @param enabled
         *            Set a flag value of <code>true</code> to enable a retention policy; otherwise, <code>false</code>.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
