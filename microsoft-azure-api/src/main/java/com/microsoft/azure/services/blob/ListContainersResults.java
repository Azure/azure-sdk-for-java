package com.microsoft.azure.services.blob;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "EnumerationResults")
public class ListContainersResults {
    private List<Container> containers;
    private String accountName;
    private String prefix;
    private String marker;
    private String nextMarker;
    private int maxResults;

    @XmlElementWrapper(name = "Containers")
    @XmlElement(name = "Container")
    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> value) {
        this.containers = value;
    }

    @XmlAttribute(name = "AccountName")
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @XmlElement(name = "Marker")
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    @XmlElement(name = "NextMarker")
    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    @XmlElement(name = "MaxResults")
    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public static class Container {
        private String name;
        private String url;
        private HashMap<String, String> metadata;
        private ContainerProperties properties;

        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement(name = "Url")
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @XmlElement(name = "Properties")
        public ContainerProperties getProperties() {
            return properties;
        }

        public void setProperties(ContainerProperties properties) {
            this.properties = properties;
        }

        @XmlElement(name = "Metadata")
        @XmlJavaTypeAdapter(MetadataAdapter.class)
        public HashMap<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(HashMap<String, String> metadata) {
            this.metadata = metadata;
        }

        public static class ContainerProperties {
            private String LastModified;
            private String Etag;

            @XmlElement(name = "Last-Modified")
            public String getLastModified() {
                return LastModified;
            }

            public void setLastModified(String lastModified) {
                LastModified = lastModified;
            }

            @XmlElement(name = "Etag")
            public String getEtag() {
                return Etag;
            }

            public void setEtag(String etag) {
                Etag = etag;
            }
        }
    }
}
