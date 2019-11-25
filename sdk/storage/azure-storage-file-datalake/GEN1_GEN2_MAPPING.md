<h1>Mapping from ADLS Gen1 API -> ADLS Gen2 API</h1>
<table style="background:white">
<thead>
<tr>
<th>ADLS Gen1 API</th>
<th>Note for Gen1 API</th>
<th>ADLS Gen2 API</th>
<th>Note for API Mapping</th>
</tr>
</thead>
<tbody>
<tr>
<td>checkAccess</td>
<td>Checks whether the calling user has the required permissions for the file/directory.</td>
<td><strong>DataLakeFileClient.getAccessControl</strong> and <strong>DataLakeDirectoryClient.getAccessControl</strong></td>
<td>In the response, check if the user/group is the Owner/Group, or has an entry in the ACL.</td>
</tr>
<tr>
<td>checkExists</td>
<td>Check whether a file or directory exists.</td>
<td><strong>DataLakeFileClient.getProperties</strong> and <strong>DataLakeDirectoryClient.getProperties</strong></td>
<td>An exception will be thrown if the file or directory does not exist.</td>
</tr>
<tr>
<td>concatenateFiles</td>
<td>Concatenate the specified list of files into this file.</td>
<td>N/A</td>
<td></td>
</tr>
<tr>
<td>createClient</td>
<td>Gets an ADLStoreClient object.</td>
<td><strong>DataLakeServiceClientBuilder.buildClient</strong></td>
<td>Set the appropriate <strong>endpoint</strong> and <strong>credential</strong></td>
</tr>
<tr>
<td>createDirectory</td>
<td>Creates a directory and all it's parent directories if they do not already exist.</td>
<td><strong>DataLakeDirectoryClient.create</strong></td>
<td></td>
</tr>
<tr>
<td>createEmptyFile</td>
<td>Creates an empty file.</td>
<td><strong>DataLakeFileClient.create</strong></td>
<td>By default this method will overwrite an existing file, set <strong>BlobRequestConditions.setIfNoneMatch("*")</strong> to prevent overwriting.</td>
</tr>
<tr>
<td>createFile</td>
<td>Creates a file.</td>
<td><strong>DataLakeFileClient.create</strong></td>
<td>By default this method will overwrite an existing file, set <strong>BlobRequestConditions.setIfNoneMatch("*")</strong> to prevent overwriting.</td>
</tr>
<tr>
<td>delete</td>
<td>Deletes a file or directory.</td>
<td><strong>DataLakeFileClient.delete</strong> and <strong>DataLakeDirectoryClient.delete</strong></td>
<td></td>
</tr>
<tr>
<td>deleteRecursive</td>
<td>Deletes a directory and all it's child directories and file recursively.</td>
<td><strong>DataLakeDirectoryClient.delete</strong></td>
<td>Set the <strong>recursive</strong> parameter to <strong>true</strong></td>
</tr>
<tr>
<td>enumerateDirectory</td>
<td>Enumerates the contents of a directory, returning a List of Directory Entry objects, one per file or directory in the specified directory.</td>
<td><strong>DataLakeFileSystemClient.listPaths</strong></td>
<td>Set <strong>ListPathsOptions.path</strong> to the desired path</td>
</tr>
<tr>
<td>getAclStatus</td>
<td>Queries the ACLs and permissions for a file or directory.</td>
<td><strong>DataLakeFileClient.getAccessControl</strong> and <strong>DataLakeDirectoryClient.getAccessControl</strong></td>
<td></td>
</tr>
<tr>
<td>getAppendStream</td>
<td>Appends to an existing file.</td>
<td><strong>DataLakeFileClient.append</strong> followed by <strong>DataLakeFileClient.flush</strong></td>
<td><strong>append</strong> should be followed by <strong>flush</strong> to actually write data into the file. <strong>append</strong> is used to stage data, not actually write data into file.</td>
</tr>
<tr>
<td>getContentSummary</td>
<td>Gets the content summary of a file or directory.</td>
<td><strong>DataLakeFileClient.getProperties</strong> and <strong>DataLakeDirectoryClient.getProperties</strong></td>
<td></td>
</tr>
<tr>
<td>getDirectoryEntry</td>
<td>Gets the directory metadata about this file or directory.</td>
<td><strong>DataLakeFileClient.getProperties</strong> and <strong>DataLakeDirectoryClient.getProperties</strong></td>
<td></td>
</tr>
<tr>
<td>getReadStream</td>
<td>Opens a file for read and returns an ADLFileInputStream to read the file contents from.</td>
<td><strong>DataLakeFileClient.read</strong></td>
<td></td>
</tr>
<tr>
<td>modifyAclEntries</td>
<td>Modify the acl entries for a file or directory.</td>
<td><strong>DataLakeFileClient.setAccessControlList</strong> and <strong>DataLakeDirectoryClient.setAccessControlList</strong></td>
<td></td>
</tr>
<tr>
<td>removeAclEntries</td>
<td>Removes the specified ACL entries from a file or directory.</td>
<td>N/A</td>
<td></td>
</tr>
<tr>
<td>removeAllAcls</td>
<td>Removes all acl entries from a file or directory.</td>
<td>N/A</td>
<td></td>
</tr>
<tr>
<td>removeDefaultAcls</td>
<td>Removes all default acl entries from a directory.</td>
<td>N/A</td>
<td></td>
</tr>
<tr>
<td>rename</td>
<td>Rename a file or directory.</td>
<td><strong>DataLakeFileClient.rename</strong> and <strong>DataLakeDirectoryClient.rename</strong></td>
<td></td>
</tr>
<tr>
<td>setAcl</td>
<td>Sets the ACLs for a file or directory.</td>
<td><strong>DataLakeFileClient.setAccessControlList</strong> and <strong>DataLakeDirectoryClient.setAccessControlList</strong></td>
<td></td>
</tr>
<tr>
<td>setExpiryTime</td>
<td>Sets the expiry time on a file.</td>
<td>N/A</td>
<td></td>
</tr>
<tr>
<td>setOwner</td>
<td>Sets the owning user and group of the file.</td>
<td><strong>DataLakeFileClient.setPermissions</strong> and <strong>DataLakeDirectoryClient.setPermissions</strong></td>
<td></td>
</tr>
<tr>
<td>setPermission</td>
<td>Sets the permissions of the specified file or directory.</td>
<td><strong>DataLakeFileClient.setPermissions</strong> and <strong>DataLakeDirectoryClient.setPermissions</strong></td>
<td></td>
</tr>
<tr>
<td>setTimes</td>
<td>Sets one or both of the times (Modified and Access time) of the file or directory.</td>
<td>N/A</td>
<td></td>
</tr>
</tbody>
</table>
