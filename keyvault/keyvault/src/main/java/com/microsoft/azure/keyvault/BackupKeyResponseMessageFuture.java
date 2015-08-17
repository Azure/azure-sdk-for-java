/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault;

import java.io.IOException;
import java.util.concurrent.Future;

import org.codehaus.jackson.map.ObjectReader;

import com.microsoft.azure.keyvault.models.BackupKeyResponseMessage;

final class BackupKeyResponseMessageFuture extends FutureAdapter<KeyOpResponseMessageWithRawJsonContent, byte[]> {

    public BackupKeyResponseMessageFuture(Future<KeyOpResponseMessageWithRawJsonContent> inner) {
        super(inner);
    }

    @Override
    protected byte[] translate(KeyOpResponseMessageWithRawJsonContent a) throws IOException {
        ObjectReader reader = JsonSupport.getJsonReader(BackupKeyResponseMessage.class);
        BackupKeyResponseMessage message = reader.readValue(a.getKeyOpResponse());
        return message.getValue();
    }

}