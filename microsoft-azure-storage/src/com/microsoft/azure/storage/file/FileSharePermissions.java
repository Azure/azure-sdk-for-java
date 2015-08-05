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
package com.microsoft.azure.storage.file;

import com.microsoft.azure.storage.Permissions;

/**
 * Represents the permissions for a share.
 * <p>
 * The share's permissions encompass its access policies, represented by the {@link #getSharedAccessPolicies} method.
 * This setting references a collection of shared access policies for the share. A shared access policy may
 * be used to control the start time, expiry time, and permissions for one or more shared access signatures.
 * A shared access signature provides delegated access to the share's resources.
 * For more information on managing share permissions, see
 * <a href='http://go.microsoft.com/fwlink/?LinkID=224643'>Managing Access to Shares and Files</a>.
 */
public final class FileSharePermissions extends Permissions<SharedAccessFilePolicy> {
}