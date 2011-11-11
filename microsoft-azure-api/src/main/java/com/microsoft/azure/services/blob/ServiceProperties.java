package com.microsoft.azure.services.blob;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "StorageServiceProperties")
public class ServiceProperties {
    //TODO: What should the default value be (null or new Logging())?
    private Logging logging;
    private Metrics metrics;
    private String defaultServiceVersion;

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

    @XmlElement(name = "DefaultServiceVersion")
    public String getDefaultServiceVersion() {
        return defaultServiceVersion;
    }

    public void setDefaultServiceVersion(String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
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
