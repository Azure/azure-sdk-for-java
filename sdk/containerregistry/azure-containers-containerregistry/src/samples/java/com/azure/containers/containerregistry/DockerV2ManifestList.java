package com.azure.containers.containerregistry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class DockerV2ManifestList {
    @JsonProperty("mediaType")
    private String mediaType;
    @JsonProperty("schemaVersion")
    private Integer schemaVersion;
    @JsonProperty("manifests")
    private List<DockerV2ManifestListAttributes> manifests;

    public DockerV2ManifestList() {
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public DockerV2ManifestList setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public List<DockerV2ManifestListAttributes> getManifests() {
        return this.manifests;
    }

    public DockerV2ManifestList setManifests(List<DockerV2ManifestListAttributes> manifests) {
        this.manifests = manifests;
        return this;
    }

    public DockerV2ManifestList setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    public Integer getSchemaVersion() {
        return this.schemaVersion;
    }

    public static final class DockerV2ManifestListAttributes {
        @JsonProperty("mediaType")
        private String mediaType;

        @JsonProperty("size")
        private Long size;

        @JsonProperty("digest")
        private String digest;

        @JsonProperty("platform")
        private Platform platform;

        public DockerV2ManifestListAttributes() {
        }

        public String getMediaType() {
            return this.mediaType;
        }

        public DockerV2ManifestListAttributes setMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Long getSize() {
            return this.size;
        }

        public DockerV2ManifestListAttributes setSize(Long size) {
            this.size = size;
            return this;
        }

        public String getDigest() {
            return this.digest;
        }

        public DockerV2ManifestListAttributes setDigest(String digest) {
            this.digest = digest;
            return this;
        }

        public Platform getPlatform() {
            return this.platform;
        }

        public DockerV2ManifestListAttributes setPlatform(Platform platform) {
            this.platform = platform;
            return this;
        }
    }

    public static  final class Platform {
        @JsonProperty("architecture")
        private String architecture;

        @JsonProperty("os")
        private String os;

        public Platform setOs(String os) {
            this.os = os;
            return this;
        }

        public String getOs() {
            return this.os;
        }

        public Platform setArchitecture(String os) {
            this.architecture = architecture;
            return this;
        }

        public String getArchitecture() {
            return this.architecture;
        }
    }
}
