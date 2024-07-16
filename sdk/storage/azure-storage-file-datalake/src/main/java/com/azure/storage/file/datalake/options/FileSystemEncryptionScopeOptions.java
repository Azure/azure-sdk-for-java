// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Encryption scope options to be used when creating a file system.
 */
public final class FileSystemEncryptionScopeOptions implements JsonSerializable<FileSystemEncryptionScopeOptions> {

    /*
     * Optional.  Version 2021-06-08 and later. Specifies the default
     * encryption scope to set on the container and use for all future writes.
     */
    private String defaultEncryptionScope;

    /*
     * Optional.  Version 2021-06-08 and newer. If true, prevents any request
     * from specifying a different encryption scope than the scope set on the
     * container.
     */
    private Boolean encryptionScopeOverridePrevented;

    /**
     * Get the defaultEncryptionScope property: Optional. Version 2021-06-08 and later. Specifies the default encryption
     * scope to set on the file system and use for all future writes.
     *
     * @return the defaultEncryptionScope value.
     */
    public String getDefaultEncryptionScope() {
        return defaultEncryptionScope;
    }

    /**
     * Set the defaultEncryptionScope property: Optional. Version 2021-06-08 and later. Specifies the default encryption
     * scope to set on the file system and use for all future writes.
     *
     * @param encryptionScope the defaultEncryptionScope value to set.
     * @return the updated {@link FileSystemEncryptionScopeOptions}.
     */
    public FileSystemEncryptionScopeOptions setDefaultEncryptionScope(String encryptionScope) {
        this.defaultEncryptionScope = encryptionScope;
        return this;
    }

    /**
     * Get the encryptionScopeOverridePrevented property: Optional. Version 2021-06-08 and newer. If true, prevents any
     * request from specifying a different encryption scope than the scope set on the container.
     *
     * @return the encryptionScopeOverridePrevented value.
     */
    public Boolean isEncryptionScopeOverridePrevented() {
        return encryptionScopeOverridePrevented;
    }

    /**
     * Set the encryptionScopeOverridePrevented property: Optional. Version 2021-06-08 and newer. If true, prevents any
     * request from specifying a different encryption scope than the scope set on the container.
     *
     * @param encryptionScopeOverridePrevented the encryptionScopeOverridePrevented value to set.
     * @return the updated {@link FileSystemEncryptionScopeOptions}.
     */
    public FileSystemEncryptionScopeOptions setEncryptionScopeOverridePrevented(Boolean encryptionScopeOverridePrevented) {
        this.encryptionScopeOverridePrevented = encryptionScopeOverridePrevented;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("DefaultEncryptionScope", defaultEncryptionScope);
        jsonWriter.writeBooleanField("EncryptionScopeOverridePrevented", encryptionScopeOverridePrevented);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    /**
     * Reads a JSON stream into a {@link FileSystemEncryptionScopeOptions}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link FileSystemEncryptionScopeOptions} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If an I/O error occurs.
     */
    public static FileSystemEncryptionScopeOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FileSystemEncryptionScopeOptions fileSystemEncryptionScopeOptions = new FileSystemEncryptionScopeOptions();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName(); // Get the name of the field.
                reader.nextToken(); // Progress to the value.

                if ("DefaultEncryptionScope".equals(fieldName)) {
                    fileSystemEncryptionScopeOptions.defaultEncryptionScope = reader.getString();
                } else if ("EncryptionScopeOverridePrevented".equals(fieldName)) {
                    fileSystemEncryptionScopeOptions.encryptionScopeOverridePrevented = reader.getNullable(JsonReader::getBoolean);
                } else {
                    // Skip unknown values.
                    // If the type supported additional properties, this is where they would be handled.
                    reader.skipChildren();
                }
            }

            return fileSystemEncryptionScopeOptions;
        });
    };
}
