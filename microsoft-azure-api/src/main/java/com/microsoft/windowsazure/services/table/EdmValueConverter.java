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
package com.microsoft.windowsazure.services.table;

import com.microsoft.windowsazure.services.table.models.EdmType;

/**
 * An interface for EDM type serialization to Windows Azure Storage service and deserialization to native Java
 * types.
 */
public interface EdmValueConverter {
    /**
     * Creates a serialized string in EDM type format from the native Java <em>value</em> parameter.
     * <p>
     * Supported <em>edmType</em> parameter values are defined as constants in the {@link EdmType} class.
     * 
     * @param edmType
     *            A {@link String} containing the EDM data type to serialize the <em>value</em> parameter as.
     * @param value
     *            An {@link Object} reference to the native Java value to serialize.
     * @return
     *         A {@link String} containing the serialized data to send to the Windows Azure Storage service.
     */
    String serialize(String edmType, Object value);

    /**
     * Creates an object of the correct native Java type from the serialized data in EDM type format.
     * <p>
     * Supported <em>edmType</em> parameter values are defined as constants in the {@link EdmType} class.
     * 
     * @param edmType
     *            A {@link String} containing the EDM data type of the <em>value</em> parameter to deserialize.
     * @param value
     *            A {@link String} containing the Windows Azure Storage service data to deserialize.
     * @return
     *         An {@link Object} reference to the deserialized native Java value. This is an object of the
     *         correct native Java type for the EDM data type, passed as an {@link Object}.
     */
    Object deserialize(String edmType, String value);
}
