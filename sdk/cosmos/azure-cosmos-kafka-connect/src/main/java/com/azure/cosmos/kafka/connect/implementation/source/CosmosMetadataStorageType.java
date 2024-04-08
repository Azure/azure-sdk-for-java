package com.azure.cosmos.kafka.connect.implementation.source;

public enum CosmosMetadataStorageType {
    KAFKA("Kafka"),
    COSMOS("Cosmos");

    private final String name;
    CosmosMetadataStorageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosMetadataStorageType fromName(String name) {
        for (CosmosMetadataStorageType type : CosmosMetadataStorageType.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
