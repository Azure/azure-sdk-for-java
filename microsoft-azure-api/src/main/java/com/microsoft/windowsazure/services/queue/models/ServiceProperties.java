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
package com.microsoft.windowsazure.services.queue.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.microsoft.windowsazure.services.queue.QueueContract;

/**
 * A wrapper class for the Queue service properties set or retrieved with Queue Service REST API operations. This
 * is returned by calls to implementations of {@link QueueContract#getServiceProperties()} and
 * {@link QueueContract#getServiceProperties(QueueServiceOptions)} and passed to the server with calls to
 * {@link QueueContract#setServiceProperties(ServiceProperties)} and
 * {@link QueueContract#setServiceProperties(ServiceProperties, QueueServiceOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/hh452243.aspx">Get Queue Service Properties</a>
 * and <a href="http://msdn.microsoft.com/en-us/library/windowsazure/hh452232.aspx">Set Queue Service Properties</a>
 * documentation on MSDN for details of the underlying Queue Service REST API operations. See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/hh343268.aspx">Storage Analytics Overview</a>
 * documentation on MSDN for more information about logging and metrics.
 */
@XmlRootElement(name = "StorageServiceProperties")
public class ServiceProperties {
    private Logging logging = new Logging();
    private Metrics metrics = new Metrics();

    /**
     * Gets a reference to the {@link Logging} instance in this {@link ServiceProperties} instance.
     * <p>
     * This {@link ServiceProperties} instance holds a local copy of the Queue service properties when returned by a
     * call to {@link QueueContract}<em>.getServiceProperties</em>.
     * <p>
     * Note that changes to this value are not reflected in the Queue service properties until they have been set on the
     * storage account with a call to {@link QueueContract}<em>.setServiceProperties</em>.
     * 
     * @return
     *         A reference to the {@link Logging} instance in this {@link ServiceProperties} instance.
     */
    @XmlElement(name = "Logging")
    public Logging getLogging() {
        return logging;
    }

    /**
     * Sets the {@link Logging} instance in this {@link ServiceProperties} instance.
     * <p>
     * Note that changes to this value are not reflected in the Queue service properties until they have been set on the
     * storage account with a call to {@link QueueContract}<em>.setServiceProperties</em>.
     * 
     * @param logging
     *            The {@link Logging} instance to set in this {@link ServiceProperties} instance.
     */
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    /**
     * Gets a reference to the {@link Metrics} instance in this {@link ServiceProperties} instance.
     * <p>
     * This {@link ServiceProperties} instance holds a local copy of the Queue service properties when returned by a
     * call to {@link QueueContract}<em>.getServiceProperties</em>.
     * <p>
     * Note that changes to this value are not reflected in the Queue service properties until they have been set on the
     * storage account with a call to {@link QueueContract}<em>.setServiceProperties</em>.
     * 
     * @return
     *         A reference to the {@link Metrics} instance in this {@link ServiceProperties} instance.
     */
    @XmlElement(name = "Metrics")
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Sets the {@link Metrics} instance in this {@link ServiceProperties} instance.
     * <p>
     * Note that changes to this value are not reflected in the Queue service properties until they have been set on the
     * storage account with a call to {@link QueueContract}<em>.setServiceProperties</em>.
     * 
     * @param metrics
     *            The {@link Metrics} instance to set in this {@link ServiceProperties} instance.
     */
    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * This inner class represents the settings for logging on the Queue service of the storage account. These settings
     * include the Storage Analytics version, whether to log delete requests, read requests, or write requests, and a
     * {@link RetentionPolicy} instance for retention policy settings.
     */
    public static class Logging {
        private String version;
        private Boolean delete;
        private Boolean read;
        private Boolean write;
        private RetentionPolicy retentionPolicy;

        /**
         * Gets a reference to the {@link RetentionPolicy} instance in this {@link Logging} instance.
         * 
         * @return
         *         A reference to the {@link RetentionPolicy} instance in this {@link Logging} instance.
         */
        @XmlElement(name = "RetentionPolicy")
        public RetentionPolicy getRetentionPolicy() {
            return retentionPolicy;
        }

        /**
         * Sets the {@link RetentionPolicy} instance in this {@link Logging} instance.
         * 
         * @param retentionPolicy
         *            The {@link RetentionPolicy} instance to set in this {@link Logging} instance.
         */
        public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
        }

        /**
         * Gets a flag indicating whether queue write operations are logged. If this value is {@link true} then all
         * requests that write to the Queue service will be logged. These requests include adding a message, updating a
         * message, setting queue metadata, and creating a queue.
         * 
         * @return
         *         {@link true} if queue write operations are logged, otherwise {@link false}.
         */
        @XmlElement(name = "Write")
        public boolean isWrite() {
            return write;
        }

        /**
         * Sets a flag indicating whether queue write operations are logged. If this value is {@link true} then all
         * requests that write to the Queue service will be logged. These requests include adding a message, updating a
         * message, setting queue metadata, and creating a queue.
         * 
         * @param write
         *            {@link true} to enable logging of queue write operations, otherwise {@link false}.
         */
        public void setWrite(boolean write) {
            this.write = write;
        }

        /**
         * Gets a flag indicating whether queue read operations are logged. If this value is {@link true} then all
         * requests that read from the Queue service will be logged. These requests include listing queues, getting
         * queue metadata, listing messages, and peeking messages.
         * 
         * @return
         *         {@link true} if queue read operations are logged, otherwise {@link false}.
         */
        @XmlElement(name = "Read")
        public boolean isRead() {
            return read;
        }

        /**
         * Sets a flag indicating whether queue read operations are logged. If this value is {@link true} then all
         * requests that read from the Queue service will be logged. These requests include listing queues, getting
         * queue metadata, listing messages, and peeking messages.
         * 
         * @param read
         *            {@link true} to enable logging of queue read operations, otherwise {@link false}.
         */
        public void setRead(boolean read) {
            this.read = read;
        }

        /**
         * Gets a flag indicating whether queue delete operations are logged. If this value is {@link true} then all
         * requests that delete from the Queue service will be logged. These requests include deleting queues, deleting
         * messages, and clearing messages.
         * 
         * @return
         *         {@link true} if queue delete operations are logged, otherwise {@link false}.
         */
        @XmlElement(name = "Delete")
        public boolean isDelete() {
            return delete;
        }

        /**
         * Sets a flag indicating whether queue delete operations are logged. If this value is {@link true} then all
         * requests that delete from the Queue service will be logged. These requests include deleting queues, deleting
         * messages, and clearing messages.
         * 
         * @param delete
         *            {@link true} to enable logging of queue delete operations, otherwise {@link false}.
         */
        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        /**
         * Gets the Storage Analytics version number associated with this {@link Logging} instance.
         * 
         * @return
         *         A {@link String} containing the Storage Analytics version number.
         */
        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        /**
         * Sets the Storage Analytics version number to associate with this {@link Logging} instance. The current
         * supported
         * version number is "1.0".
         * <p>
         * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/hh343268.aspx">Storage Analytics
         * Overview</a> documentation on MSDN for more information.
         * 
         * @param version
         *            A {@link String} containing the Storage Analytics version number to set.
         */
        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * This inner class represents the settings for metrics on the Queue service of the storage account. These settings
     * include the Storage Analytics version, whether metrics are enabled, whether to include API operation summary
     * statistics, and a {@link RetentionPolicy} instance for retention policy settings.
     */
    public static class Metrics {
        private String version;
        private boolean enabled;
        private Boolean includeAPIs;
        private RetentionPolicy retentionPolicy;

        /**
         * Gets a reference to the {@link RetentionPolicy} instance in this {@link Metrics} instance.
         * 
         * @return
         *         A reference to the {@link RetentionPolicy} instance in this {@link Metrics} instance.
         */
        @XmlElement(name = "RetentionPolicy")
        public RetentionPolicy getRetentionPolicy() {
            return retentionPolicy;
        }

        /**
         * Sets the {@link RetentionPolicy} instance in this {@link Metrics} instance.
         * 
         * @param retentionPolicy
         *            The {@link RetentionPolicy} instance to set in this {@link Metrics} instance.
         */
        public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
        }

        /**
         * Gets a flag indicating whether metrics should generate summary statistics for called API operations. If this
         * value is {@link true} then all Queue service REST API operations will be included in the metrics.
         * 
         * @return
         *         {@link true} if Queue service REST API operations are included in metrics, otherwise {@link false}.
         */
        @XmlElement(name = "IncludeAPIs")
        public Boolean isIncludeAPIs() {
            return includeAPIs;
        }

        /**
         * Sets a flag indicating whether metrics should generate summary statistics for called API operations. If this
         * value is {@link true} then all Queue service REST API operations will be included in the metrics.
         * 
         * @param includeAPIs
         *            {@link true} to include Queue service REST API operations in metrics, otherwise {@link false}.
         */
        public void setIncludeAPIs(Boolean includeAPIs) {
            this.includeAPIs = includeAPIs;
        }

        /**
         * Gets a flag indicating whether metrics is enabled for the Queue storage service.
         * 
         * @return
         *         A flag indicating whether metrics is enabled for the Queue storage service.
         */
        @XmlElement(name = "Enabled")
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets a flag indicating whether to enable metrics for the Queue storage service.
         * 
         * @param enabled
         *            {@link true} to enable metrics for the Queue storage service, otherwise {@link false}.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Gets the Storage Analytics version number associated with this {@link Metrics} instance.
         * 
         * @return
         *         A {@link String} containing the Storage Analytics version number.
         */
        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        /**
         * Sets the Storage Analytics version number to associate with this {@link Metrics} instance. The current
         * supported
         * version number is "1.0".
         * <p>
         * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/hh343268.aspx">Storage Analytics
         * Overview</a> documentation on MSDN for more information.
         * 
         * @param version
         *            A {@link String} containing the Storage Analytics version number to set.
         */
        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * This inner class represents the retention policy settings for logging or metrics on the Queue service of the
     * storage account. These settings include whether a retention policy is enabled for the data, and the number of
     * days that metrics or logging data should be retained.
     */
    public static class RetentionPolicy {
        private boolean enabled;
        private Integer days; // nullable, because optional if "enabled" is false

        /**
         * Gets the number of days that metrics or logging data should be retained. All data older than this value will
         * be deleted. The value may be null if a retention policy is not enabled.
         * 
         * @return
         */
        @XmlElement(name = "Days")
        public Integer getDays() {
            return days;
        }

        /**
         * Sets the number of days that metrics or logging data should be retained. All data older than this value will
         * be deleted. The value must be in the range from 1 to 365. This value must be set if a retention policy is
         * enabled, but is not required if a retention policy is not enabled.
         * 
         * @param days
         *            The number of days that metrics or logging data should be retained.
         */
        public void setDays(Integer days) {
            this.days = days;
        }

        /**
         * Gets a flag indicating whether a retention policy is enabled for the storage service.
         * 
         * @return
         *         {@link true} if data retention is enabled, otherwise {@link false}.
         */
        @XmlElement(name = "Enabled")
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets a flag indicating whether a retention policy is enabled for the storage service.
         * 
         * @param enabled
         *            Set {@link true} to enable data retention.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
