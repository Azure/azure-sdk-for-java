/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob;

import com.microsoft.windowsazure.services.core.Configuration;

public class BlobService {
    private BlobService() {
    }

    public static BlobContract create() {
        return create(null, Configuration.getInstance());
    }

    public static BlobContract create(Configuration config) {
        return create(null, config);
    }

    public static BlobContract create(String profile) {
        return create(profile, Configuration.getInstance());
    }

    public static BlobContract create(String profile, Configuration config) {
        return config.create(profile, BlobContract.class);
    }
}
