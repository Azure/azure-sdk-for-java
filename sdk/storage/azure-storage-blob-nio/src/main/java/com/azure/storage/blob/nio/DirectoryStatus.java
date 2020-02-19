/*
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

package com.azure.storage.blob.nio;

/**
 * RESERVED FOR INTERNAL USE.
 *
 * An enum to indicate the status of a directory.
 */
enum DirectoryStatus {
    EMPTY, // The directory at least weakly exists and is empty.

    NOT_EMPTY, // The directory at least weakly exists and has one or more children.

    DOES_NOT_EXIST, // There is no resource at this path.

    NOT_A_DIRECTORY // A resource exists at this path, but it is not a directory.
}
