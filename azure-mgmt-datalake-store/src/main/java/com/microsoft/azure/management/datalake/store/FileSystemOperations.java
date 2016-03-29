/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.datalake.store.models.AclStatusResult;
import com.microsoft.azure.management.datalake.store.models.AppendModeType;
import com.microsoft.azure.management.datalake.store.models.ContentSummaryResult;
import com.microsoft.azure.management.datalake.store.models.FileOperationResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusesResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/**
 * An instance of this class provides access to all the operations defined
 * in FileSystemOperations.
 */
public interface FileSystemOperations {
    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> concurrentAppend(String filePath, String accountName, byte[] streamContents) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall concurrentAppendAsync(String filePath, String accountName, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param appendMode Indicates the concurrent append call should create the file if it doesn't exist or just open the existing file for append. Possible values include: 'autocreate'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> concurrentAppend(String filePath, String accountName, byte[] streamContents, AppendModeType appendMode) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param appendMode Indicates the concurrent append call should create the file if it doesn't exist or just open the existing file for append. Possible values include: 'autocreate'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall concurrentAppendAsync(String filePath, String accountName, byte[] streamContents, AppendModeType appendMode, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> checkAccess(String path, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall checkAccessAsync(String path, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param fsaction File system operation read/write/execute in string form, matching regex pattern '[rwx-]{3}'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> checkAccess(String path, String accountName, String fsaction) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param fsaction File system operation read/write/execute in string form, matching regex pattern '[rwx-]{3}'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall checkAccessAsync(String path, String accountName, String fsaction, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Creates a directory.
     *
     * @param path The Data Lake Store path (starting with '/') of the directory to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<FileOperationResult> mkdirs(String path, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Creates a directory.
     *
     * @param path The Data Lake Store path (starting with '/') of the directory to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall mkdirsAsync(String path, String accountName, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Concatenates the list of source files into the destination file, removing all source files upon success.
     *
     * @param destinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param sources A list of comma seperated Data Lake Store paths (starting with '/') of the files to concatenate, in the order in which they should be concatenated.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> concat(String destinationPath, String accountName, List<String> sources) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Concatenates the list of source files into the destination file, removing all source files upon success.
     *
     * @param destinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param sources A list of comma seperated Data Lake Store paths (starting with '/') of the files to concatenate, in the order in which they should be concatenated.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall concatAsync(String destinationPath, String accountName, List<String> sources, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> msConcat(String msConcatDestinationPath, String accountName, byte[] streamContents) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall msConcatAsync(String msConcatDestinationPath, String accountName, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param deleteSourceDirectory Indicates that as an optimization instead of deleting each individual source stream, delete the source stream folder if all streams are in the same folder instead. This results in a substantial performance improvement when the only streams in the folder are part of the concatenation operation. WARNING: This includes the deletion of any other files that are not source files. Only set this to true when source files are the only files in the source directory.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> msConcat(String msConcatDestinationPath, String accountName, byte[] streamContents, Boolean deleteSourceDirectory) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param deleteSourceDirectory Indicates that as an optimization instead of deleting each individual source stream, delete the source stream folder if all streams are in the same folder instead. This results in a substantial performance improvement when the only streams in the folder are part of the concatenation operation. WARNING: This includes the deletion of any other files that are not source files. Only set this to true when source files are the only files in the source directory.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall msConcatAsync(String msConcatDestinationPath, String accountName, byte[] streamContents, Boolean deleteSourceDirectory, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusesResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<FileStatusesResult> listFileStatus(String listFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFileStatusAsync(String listFilePath, String accountName, final ServiceCallback<FileStatusesResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the file content summary object specified by the file path.
     *
     * @param getContentSummaryFilePath The Data Lake Store path (starting with '/') of the file for which to retrieve the summary.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ContentSummaryResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<ContentSummaryResult> getContentSummary(String getContentSummaryFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets the file content summary object specified by the file path.
     *
     * @param getContentSummaryFilePath The Data Lake Store path (starting with '/') of the file for which to retrieve the summary.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getContentSummaryAsync(String getContentSummaryFilePath, String accountName, final ServiceCallback<ContentSummaryResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Get the file status object specified by the file path.
     *
     * @param getFilePath The Data Lake Store path (starting with '/') of the file or directory for which to retrieve the status.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<FileStatusResult> getFileStatus(String getFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Get the file status object specified by the file path.
     *
     * @param getFilePath The Data Lake Store path (starting with '/') of the file or directory for which to retrieve the status.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getFileStatusAsync(String getFilePath, String accountName, final ServiceCallback<FileStatusResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Appends to the specified file. This method does not support multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option. Use the ConcurrentAppend option if you would like support for concurrent appends.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to which to append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> append(String directFilePath, String accountName, byte[] streamContents) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Appends to the specified file. This method does not support multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option. Use the ConcurrentAppend option if you would like support for concurrent appends.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to which to append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall appendAsync(String directFilePath, String accountName, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> create(String directFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createAsync(String directFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when creating the file. This parameter is optional, resulting in an empty file if not specified.
     * @param overwrite The indication of if the file should be overwritten.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> create(String directFilePath, String accountName, byte[] streamContents, Boolean overwrite) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when creating the file. This parameter is optional, resulting in an empty file if not specified.
     * @param overwrite The indication of if the file should be overwritten.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createAsync(String directFilePath, String accountName, byte[] streamContents, Boolean overwrite, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<InputStream> open(String directFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall openAsync(String directFilePath, String accountName, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;
    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param length the Long value
     * @param offset the Long value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<InputStream> open(String directFilePath, String accountName, Long length, Long offset) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param length the Long value
     * @param offset the Long value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall openAsync(String directFilePath, String accountName, Long length, Long offset, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;

    /**
     * Sets the Access Control List (ACL) for a file or folder.
     *
     * @param setAclFilePath The Data Lake Store path (starting with '/') of the file or directory on which to set the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL creation operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> setAcl(String setAclFilePath, String accountName, String aclspec) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Sets the Access Control List (ACL) for a file or folder.
     *
     * @param setAclFilePath The Data Lake Store path (starting with '/') of the file or directory on which to set the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL creation operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall setAclAsync(String setAclFilePath, String accountName, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Modifies existing Access Control List (ACL) entries on a file or folder.
     *
     * @param modifyAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being modified.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL specification included in ACL modification operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> modifyAclEntries(String modifyAclFilePath, String accountName, String aclspec) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Modifies existing Access Control List (ACL) entries on a file or folder.
     *
     * @param modifyAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being modified.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL specification included in ACL modification operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall modifyAclEntriesAsync(String modifyAclFilePath, String accountName, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Removes existing Access Control List (ACL) entries for a file or folder.
     *
     * @param removeAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL removal operations in the format '[default:]user|group|other'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> removeAclEntries(String removeAclFilePath, String accountName, String aclspec) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Removes existing Access Control List (ACL) entries for a file or folder.
     *
     * @param removeAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL removal operations in the format '[default:]user|group|other'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall removeAclEntriesAsync(String removeAclFilePath, String accountName, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Removes the existing Access Control List (ACL) of the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> removeAcl(String aclFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Removes the existing Access Control List (ACL) of the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall removeAclAsync(String aclFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets Access Control List (ACL) entries for the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory for which to get the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AclStatusResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<AclStatusResult> getAclStatus(String aclFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets Access Control List (ACL) entries for the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory for which to get the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAclStatusAsync(String aclFilePath, String accountName, final ServiceCallback<AclStatusResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<FileOperationResult> delete(String filePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String filePath, String accountName, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param recursive The optional switch indicating if the delete should be recursive
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<FileOperationResult> delete(String filePath, String accountName, Boolean recursive) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param recursive The optional switch indicating if the delete should be recursive
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String filePath, String accountName, Boolean recursive, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Rename a file or directory.
     *
     * @param renameFilePath The Data Lake Store path (starting with '/') of the file or directory to move/rename.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param destination The path to move/rename the file or folder to
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<FileOperationResult> rename(String renameFilePath, String accountName, String destination) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Rename a file or directory.
     *
     * @param renameFilePath The Data Lake Store path (starting with '/') of the file or directory to move/rename.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param destination The path to move/rename the file or folder to
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall renameAsync(String renameFilePath, String accountName, String destination, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> setOwner(String setOwnerFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall setOwnerAsync(String setOwnerFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param owner The AAD Object ID of the user owner of the file or directory. If empty, the property will remain unchanged.
     * @param group The AAD Object ID of the group owner of the file or directory. If empty, the property will remain unchanged.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> setOwner(String setOwnerFilePath, String accountName, String owner, String group) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param owner The AAD Object ID of the user owner of the file or directory. If empty, the property will remain unchanged.
     * @param group The AAD Object ID of the group owner of the file or directory. If empty, the property will remain unchanged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall setOwnerAsync(String setOwnerFilePath, String accountName, String owner, String group, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> setPermission(String setPermissionFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall setPermissionAsync(String setPermissionFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param permission A string representation of the permission (i.e 'rwx'). If empty, this property remains unchanged.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> setPermission(String setPermissionFilePath, String accountName, String permission) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param permission A string representation of the permission (i.e 'rwx'). If empty, this property remains unchanged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall setPermissionAsync(String setPermissionFilePath, String accountName, String permission, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

}
