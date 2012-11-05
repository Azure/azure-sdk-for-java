/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.implementation;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.MediaEntityContract;
import com.microsoft.windowsazure.services.media.entities.EntityCreationOperation;

/**
 * 
 *
 */
public class MediaEntityRestProxy implements MediaEntityContract {

    /**
     * 
     */
    public MediaEntityRestProxy() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaEntityContract#create(com.microsoft.windowsazure.services.media.entities.EntityCreationOperation)
     */
    @Override
    public <T> T create(EntityCreationOperation<T> creator) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

}
