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
package com.microsoft.windowsazure.services.table.implementation;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;
import com.microsoft.windowsazure.services.table.EdmValueConverter;
import com.microsoft.windowsazure.services.table.models.EdmType;
import com.sun.jersey.core.util.Base64;

public class DefaultEdmValueConverter implements EdmValueConverter {

    private final ISO8601DateConverter iso8601DateConverter;

    @Inject
    public DefaultEdmValueConverter(ISO8601DateConverter iso8601DateConverter) {
        this.iso8601DateConverter = iso8601DateConverter;
    }

    @Override
    public String serialize(String edmType, Object value) {
        if (value == null)
            return null;

        String serializedValue;
        if (value instanceof Date) {
            serializedValue = iso8601DateConverter.format((Date) value);
        }
        else if (value instanceof byte[]) {
            serializedValue = new String(Base64.encode((byte[]) value));
        }
        else {
            serializedValue = value.toString();
        }

        return serializedValue;
    }

    @Override
    public Object deserialize(String edmType, String value) {
        if (edmType == null)
            return value;

        if (EdmType.DATETIME.equals(edmType)) {
            try {
                return iso8601DateConverter.parse(value);
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        else if (EdmType.BOOLEAN.equals(edmType)) {
            return Boolean.parseBoolean(value);
        }
        else if (EdmType.DOUBLE.equals(edmType)) {
            return Double.parseDouble(value);
        }
        else if (EdmType.INT32.equals(edmType)) {
            return Integer.parseInt(value);
        }
        else if (EdmType.INT64.equals(edmType)) {
            return Long.parseLong(value);
        }
        else if (EdmType.BINARY.equals(edmType)) {
            return Base64.decode(value);
        }
        else if (EdmType.GUID.equals(edmType)) {
            return UUID.fromString(value);
        }

        return value;
    }
}
