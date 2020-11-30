# Azure Storage NIO Design Doc

# Background

Please refer to the [Project Overview](https://microsoft-my.sharepoint.com/:w:/p/frley/EQfMXjgWA4NPrAE9IIt7PUsBC-sahzFdMkc6im0Y4R4cww) for highlevel background on this project.

## NIO

The [nio package](https://docs.oracle.com/javase/7/docs/api/java/nio/file/package-summary.html) is reasonably large and has several subpackages. The docs are quite thorough in specifying expected behavior for implementing the interfaces and extending the abstract types.

Oracle has written a [tutorial](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html) on this package that can be helpful for getting started and understanding the fundamentals of how customers may use the FileSystem APIs.

## Providers

Java frequently works with a Service Provider Interface (SPI) architecture. This architecture is largely built on the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) type. In short, the JDK will define a static factory type, in this case [FileSystems](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html), that is used to instantiate providers, or implementations of the given service. When a client issues a request to the factory for a new instance, the ServiceLoader is invoked to search for installed implementations of this type. The requirements for installation are somewhat specific to the service, but in this case the type must be on the classpath and the package must have a resource file pointing to the implementation type. Once the ServiceLoader loads all available instances, it will query each to see if it fits the criteria that satisfies the client&#39;s request. In the case of FileSystems, it will look for a FileSystemProvider that uses a scheme which matches that of the passed URI. Upon finding the appropriate implementation, the service API is interacted with as normal.

# Entry, Configuration, and Authentication

## Entry

The JVM relies on the [FileSystems](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html) API to dynamically load FileSystems. Assuming our package is [properly configured](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) and loaded on the classpath (probably via a Maven dependency), a customer need only call [newFileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html#newFileSystem(java.net.URI,%20java.util.Map)) to create a new FileSystem backed by Azure Blob Storage or [getFileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html#getFileSystem(java.net.URI)) to retrieve an existing instance.

A FileSystem is an abstract concept that may be distributed across one or more accounts. In the simple case, a FileSystem corresponds to an account and will be uniquely identified by the account name. E.g. a FileSystem backed by Azure Storage account &quot;xclientdev&quot; will be identified by the URI &quot;azb://?account=xclientdev&quot;. Using the account as the analog for a FileSystem allows containers to be used as different FileStores (the equivalent of different drives on Windows).

If data required by the FileSystem is spread across multiple accounts, the FileSystem will be uniquely identified by a UUID. In this case, the URI must be of the format &quot;azb://?fileSystemId=\&lt;UUID\&gt;&quot;. The difference in query parameter will signal to the FileSystem that its storage is distributed across accounts. The account name and fileSystemId will be used to index the open FileSystems in the same way, so these values cannot be shared between two different FileSystems. The difference in query parameter is only a hint to the FileSystem. (See &quot;Configuration and Authentication&quot; below for further information on how this affects configuration).

The scheme used for Azure Storage&#39;s implementation will be &quot;azb&quot;. We specify &#39;b&#39; as it is more flexible. This will leave room for later implementations to be built on top of Datalake (&quot;azd&quot;) which will enable scenarios like working with [POSIX permissions](https://docs.oracle.com/javase/tutorial/essential/io/fileAttr.html#posix). It could also allow for loading a provider backed by Azure Share Files (&quot;azs&quot;) for a fuller set of FileSystem features.

A best effort attempt to make a request to the storage account will be made upon initialization by making a call to [getContainerProperties](https://docs.microsoft.com/rest/api/storageservices/get-container-properties) for each container specified (See &quot;Configuration and Authentication&quot; below). Failure to complete this connection on any container will result in an IOException and failure to load the FileSystem. Because this is a best effort check, it merely validates the existence of and minimal authorization to the FileSystem. It does not guarantee that there are sufficient permissions for all FileSystem operations.

Once a FileSystem instance has been loaded and returned, a customer may perform their normal FileSystem operations backed by Azure Blob Storage.

## Configuration and Authentication

A FileSystem will be configured and authenticated via the options map available on [newFile](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html#newFileSystem(java.net.URI,%20java.util.Map))[S](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html#newFileSystem(java.net.URI,%20java.util.Map))[ystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html#newFileSystem(java.net.URI,%20java.util.Map)). It is left to the customer how to build this map (e.g. querying another source, reading a file, etc.), but if only one account is used to back the FileSystem, it must specify one of the following keys with an appropriate value for authentication:

- AzureStorageAccountKey: String
- AzureStorageSasToken: String

The map is queried in the above order, and the first one found is the authentication method used. If a Sas token is used, the customer must take care that it has appropriate permissions to perform the actions demanded of the FileSystem in a given workflow, including the initial connection check specified above. Furthermore, it must have an expiry time set after the client is expected to finish using the FileSystem. No token refresh is currently offered by the FileSystem implementation, though it is possible one may be offered in the future through some means of specifying a refresh period and location to read the new token at the correct time in the options. If the FileSystem is backed by multiple accounts, a SasToken must be attached to each container as specified below.

A client must also specify the FileStores that they would like to configure. FileStores will correspond to containers, and the listed containers will be created if they do not already exist. Existing data will be preserved and if it is in one of the listed containers may be accessed via the FileSystem APIs, though care should be taken to ensure that the hierarchy is structured in a way intelligible to this implementation or behavior will be undefined (See &quot;Directories&quot; below). Any containers otherwise existing in the account will be ignored. The list of specified containers will be the return value for the name property on each value returned from [getFileStores](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getFileStores()). The result of  [getRootDirectories](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getRootDirectories()) will be &quot;\&lt;containerName\&gt;:/&quot;. This implies that all paths in this FileSystem will be prefixed with &quot;\&lt;containerName\&gt;:/&quot;, or, more completely, a URI to a file in a FileSystem backed by Azure Blob Storage will always have the prefix &quot;azb://\&lt;containerName\&gt;:/&quot;. The colon indicates a FileStore and is therefore disallowed in path elements other than the root directory.

This entry must use the key &quot;AzureStorageFileStores&quot; and the value is an Iterable\&lt;String\&gt;. The format of each entry depends on the URI used to create the FileSystem. If the &quot;account&quot; parameter was used, the strings are simply container names. The same credentials will be applied to each container. If the &quot;fileSystemId&quot; parameter was used, the auth parameters will be ignored, and each container name must be fully qualified with the host and include a sas token that can access the container. E.g. &quot;account.blob.core.windows.net/c1?\&lt;sasToken\&gt;&quot;. In either case, the first container listed will be considered the default and hence its root directory will be the default directory for the FileSystem.

The following options allow for configuring the underlying blob client. If they are not specified, defaults from the blob sdk will be used:

- AzureStorageHttpLogDetailLevel: com.azure.core.http.policy.HttpLogLevelDetail
- AzureStorageMaxTries: Integer
- AzureStorageTryTimeout: Integer
- AzureStorageRetryDelayInMs: Long
- AzureStorageMaxRetryDelayInMs: Long
- AzureStorageRetryPolicyType: com.azure.storage.common.policy.RetryPolicyType
- AzureStorageSecondaryHost: String
- AzureStorageUploadBlockSize: Long
- AzureStorageDownloadResumeRetries: Integer
- AzureStorageUseHttps: Boolean

Using this map for configuration will allow for future extensibility. See the &quot;Open Questions/Future Development&quot; section below for more details.

# Technical Details

## Concurrent Use of Account and Containers by Other Applications

Taken from the java.nio [package overview](https://docs.oracle.com/javase/7/docs/api/java/nio/file/package-summary.html):

The view of the files and file system provided by classes in this package are guaranteed to be consistent with other views provided by other instances in the same Java virtual machine. The view may or may not, however, be consistent with the view of the file system as seen by other concurrently running programs due to caching performed by the underlying operating system and delays induced by network-filesystem protocols. This is true regardless of the language in which these other programs are written, and whether they are running on the same machine or on some other machine. The exact nature of any such inconsistencies are system-dependent and are therefore unspecified.

Likewise for the AzureFileSystem, the view of the FileSystem from within an instance of the JVM will be consistent, but the AzureFileSystem makes no guarantees on behavior or state should other processes operate on the same data. The AzureFileSystem will assume that it has exclusive access to the resources stored in Azure Blob Storage and will behave without regard for potential interfering applications.

Moreover, even from within a given application, it should be remembered that using a remote FileSystem introduces higher latency. Because of this, particular care must be taken when managing concurrency. Race conditions are more likely to manifest, network failures occur more frequently than disk failures, and other such distributed application scenarios must be considered when working with this FileSystem. While the AzureFileSystem will ensure it takes appropriate steps towards robustness and reliability, the application developer must also design around these failure scenarios and have fallback and retry options available.

## Limitations

It is important to recognize that Azure Blob Storage is not a true FileSystem, nor is it the goal of this project to force Azure Blob Storage to act like a full-fledged FileSystem. While providing FileSystem APIs on top of Azure Blob Storage can offer convenience and ease of access in certain cases, trying to force the Storage service to work in scenarios it is not designed for is bound to introduce performance and stability problems. To that end, this project will only offer APIs that can be sensibly and cleanly built on top of Azure Blob Storage APIs. We recognize that this will leave some scenarios unsupported indefinitely, but we would rather offer a product that works predictably and reliably in its well defined scenarios than eagerly support all possible scenarios at the expense of quality.

Azure Storage has other storage offerings, such as Azure Datalake and Azure Files. Each of these has semantics that approach a traditional FileSystem more closely than Azure Blobs. Should there arise a need for greater nio support on top of Azure Storage, we may choose to implement these APIs on top of one of those services as well.

## File Open Options

Due to the above limitations, not all file I/O operations can be supported. In particular, random writes on existing data are not feasible on top of Azure Blob Storage. (See the &quot;Open Questions/Future Development&quot; section for a discussion on random IO. See the write()/close() operation notes in the &quot;API&quot; section below for more information on the implementation of writing).

Due to these constraints, writing is only permitted in very specific scenarios. The supported [StandardOpenOptions](https://docs.oracle.com/javase/7/docs/api/java/nio/file/StandardOpenOption.html) are as follows:

- APPEND: It should be possible to append to existing blobs by writing new blocks, retrieving the existing block list, and appending the new blocks to the list before committing.
- CREATE
- CREATE\_NEW
- DELETE\_ON\_CLOSE
- DSYNC: Every write requires a getBlockList + commitBlockList
- READ: Random reads are supported and fairly straightforward with Azure Blob Storage.
- SYNC
- TRUNCATE\_EXISTING: We would not follow the specified behavior exactly as we would simply commit a block list over the existing blob. This has the same result upon closing but does not actually involve a truncate operation.
- WRITE: Must be specified with APPEND to ensure that any write operations will not be random. If TRUNCATE\_EXISTING is specified, we will write as normal and blow away the old blob with a commitBlockList upon closing.

## Directories

Azure Blob Storage does not support actual directories. Virtual directories are supported by specifying a blob name that includes one or more path separators. Blobs may then be listed using a prefix and by specifying the delimiter to approximate a directory structure. The delimiter in this case is &#39;/&#39;.

This project will use the same directory notation as blobFuse and other existing tools. Specifically, when creating a directory a zero-length blob whose name is the desired path and has a metadata value of &quot;is\_hdi\_folder=true&quot; will be created. Operations targeting directories will target blobs with these properties. In cases where there is existing data in the containers that appears to use virtual directories (determined by the presence of path separators) but does not have the empty blob and metadata markers, behavior will be undefined as specified above. One notable example is the case where deleting the only blob in a &quot;directory&quot; that does not have this marker will actually delete the directory because there will be no marker blob present to persist the path.

## Optimistic Concurrency

Though there are limitations on how much safety we can guarantee because of the limitations of a remote Storage system, we should attempt to be safe wherever possible and use ETag-locking to ensure we are giving a consistent view of a given file when required.

# Release Criteria and Timeline

## Preview Criteria

In order to preview, the AzureFileSystem must implement the full set of features necessary to support the [Cromwell](https://github.com/broadinstitute/cromwell) scientific engine. Integration into this engine represents our MVP scenario and will give us a live environment in which we can exercise the preview for stress and perf. The set of APIs that must be included are listed below. Unless otherwise specified, their behavior will be as defined in the Oracle javadocs for the given type. Notes about behavior particular to our implementation are included inline. Anything not included in this list but included in the java.nio package will throw an UnsupportedOperationException unless otherwise specified by the Oracle docs. Release of the first preview should be targeted for the end of April.

## GA Criteria

In order to release a GA version, the AzureFileSystem must:

- Be fully integrated into the Azure Sdk Repo. This includes CI checks, docs, samples, and any other infrastructure specified in the repo guidelines.
- Have a fully functional and thorough test suite with sufficient test coverage. Testing should include unit testing on any internal types and scenarios tests that include loading the FileSystemProvider and interacting with it as if it were a production environment (this may require a separate package that simply runs an end to end test).
- A CaseRunner should be written and tested on the Developer Experience team&#39;s perf framework.
- At least two extra customers of reasonable size should have engaged with the product in a meaningful way. We should engage the customers who requested this project on the azure-storage-java repo.

Per Microsoft&#39;s guidelines and assuming all criteria are met, the product should GA no later than six months after preview. Additional time may be required for customer adoption, however.

# APIs for Preview

## [FileSystemProvider](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html)

Note that this type contains the core implementations for the FileSystem apis and [Files](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html) methods delegate here. It is also important that these methods are threadsafe.

- [checkAccess](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#checkAccess(java.nio.file.Path,%20java.nio.file.AccessMode...)): AccessDeniedException is thrown in all cases where the Execute option is passed. In all other cases, no AccessDeniedException will ever be thrown as Azure Blob Storage does not keep track of permissions on a per-blob basis, and it is assumed that the authentication method provided is sufficient for accessing the blobs in the desired manner. While it would be feasible to test read access by attempting a read, it would not be safe to do the same for write access, and in this case it is preferable to keep the assumption consistent, so we check neither. Similarly, we could check the query string of a sas token for permissions, but we cannot do the same for token authentication, and we choose here to be consistent in our assumption for clarity.
- [copy](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#copy(java.nio.file.Path,%20java.nio.file.Path,%20java.nio.file.CopyOption...)): COPY\_ATTRIBUTES must be true as it is impossible not to copy blob properties; if this option is not passed, an UnsupportedOperationException (UOE) will be thrown. All copies within an account are atomic, so ATOMIC\_MOVE should be supported and in fact will always be the case even when unspecified for a FileSystem using one account. If the FileSystem uses multiple accounts, the account name of the source and destination will be compared, and an IOException will be thrown if they do not match. If REPLACE\_EXISTING is not passed, we will use an If-None-Match:&quot;\*&quot; condition on the destination to prevent overwrites. The authentication method used on each will be the same as configured on entry. Note that copies between accounts are implicitly disallowed because we cannot copy from outside the FileSystem.
- [createDirectory](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#createDirectory(java.nio.file.Path,%20java.nio.file.attribute.FileAttribute...)): Use Etag conditions to fulfill the required atomicity of check + create. See the section on directory behavior above.
- [delete](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#delete(java.nio.file.Path))
- [deleteIfExists](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#deleteIfExists(java.nio.file.Path))
- [getFileAttributeView](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#getFileAttributeView(java.nio.file.Path,%20java.lang.Class,%20java.nio.file.LinkOption...)): Please see the AttributeView section below.
- [getFileStore](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#getFileStore(java.nio.file.Path)): The FileStore (container) does not depend on the existence of the file (blob). See the [FileStore](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileStore.html) section below.
- [getFileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#getFileSystem(java.net.URI)): Once a FileSystem is closed, it will be removed from the FileSystemProvider&#39;s internal map. Therefore, trying to retrieve a closed FileSystem will throw a FileSystemNotFoundException. Note that it is possible to create a second instance of a FileSystem with the same URI if the first one was closed.
- [getPath](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#getPath(java.net.URI)): See the Path section below.
- [getScheme](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#getScheme()): Returns &quot;azb&quot;.
- [isSameFile](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#isSameFile(java.nio.file.Path,%20java.nio.file.Path))
- [move](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#move(java.nio.file.Path,%20java.nio.file.Path,%20java.nio.file.CopyOption...)): Implemented as a copy and a delete. An AtomicMoveNotSupportedException will be thrown if the ATOMIC\_MOVE flag is passed. The same authentication method will be applied to both the source and the destination. We cannot copy the LMT of the source; the LMT will be updated as the copy time on the new blob, which is in violation of the javadocs but we do not have an alternative.
- [newDirectoryStream](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#newDirectoryStream(java.nio.file.Path,%20java.nio.file.DirectoryStream.Filter)): See the DirectoryStream section below.
- [newFileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#newFileSystem(java.net.URI,%20java.util.Map)): See the FileSystem section below.
- [newInputStream](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#newInputStream(java.nio.file.Path,%20java.nio.file.OpenOption...)): See the InputStream section below.
- [newOutputStream](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#newOutputStream(java.nio.file.Path,%20java.nio.file.OpenOption...)): See the OutputStream section below.
- [readAttributes](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#readAttributes(java.nio.file.Path,%20java.lang.Class,%20java.nio.file.LinkOption...))
- [readAttributes](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#readAttributes(java.nio.file.Path,%20java.lang.String,%20java.nio.file.LinkOption...))
- [setAttributes](https://docs.oracle.com/javase/7/docs/api/java/nio/file/spi/FileSystemProvider.html#setAttribute(java.nio.file.Path,%20java.lang.String,%20java.lang.Object,%20java.nio.file.LinkOption...))

## [Path](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html)

Note the need to support empty paths. Most methods in this type are straightforward and do not need further commentary. In this section we list only the methods that will **NOT** be supported.

- [register](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#register(java.nio.file.WatchService,%20java.nio.file.WatchEvent.Kind...)) (both overloads; support may come at a later date)
- [toRealPath](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#toRealPath(java.nio.file.LinkOption...)) (pending sym link support)

## [InputStream](https://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html)/[OutputStream](https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html)

We should be able to reuse BlobInputStream and BlobOutputStream from the blob package for these types. See above notes on OpenOptions for details on which options may be passed.

## [DirectoryStream](https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html)

A blob listing with a prefix and delimiter should suffice as we already return an Iterable.

## [FileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html)

A FileSystem is backed by an account.

- [close](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close()): The need for throwing a possible exception will require maintaining a &quot;closed&quot; Boolean. Because this closes all associated channels, etc., child objects will need to maintain a reference to their parent FileSystem and query it performing any operations. Because we don&#39;t hold onto any system resources outside of making network requests, outstanding operations can be allowed to finish and the channel will be considered closed upon the next attempted operation when the parent FileSystem is queried.
- [getFileStores](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getFileStores()): No permissions are checked. The list of FileStores will be the list passed in upon configuration. An exists call will be made on the container before returning it to ensure it is still viable.
- [getPath](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPath(java.lang.String,%20java.lang.String...)): See the Path section above.
- [getRootDirectories](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getRootDirectories()): Returns the same list as getFileStores, but each element has a &#39;/&#39; appended to it.
- [getSeparator](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getSeparator()): Returns &#39;/&#39;.
- [isOpen](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#isOpen())
- [isReadOnly](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#isReadOnly()): Returns false.
- [provider](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#provider())
- [supportedFileAttributeViews](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#supportedFileAttributeViews()): Set AttributeViews section below.

## [FileStore](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html)

A FileStore is backed by a container. As mentioned above, a list of containers is passed in upon initialization of the FileSystem. Because there is no limit to the storage space of a container, unallocated/usable/total space is MAX\_LONG. Other methods are self-evident.

## AttributeViews

- [BasicFileAttributeView](https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/BasicFileAttributeView.html):
  - [setTimes](https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/BasicFileAttributeView.html#setTimes(java.nio.file.attribute.FileTime,%20java.nio.file.attribute.FileTime,%20java.nio.file.attribute.FileTime)): a copy in place can be used to update the LMT. UnsupportedOperationException thrown for other time values
  - Symlink support pending
- [UserDefinedFileAttributeView](https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/UserDefinedFileAttributeView.html): Stored as metadata on the blob. Both keys and values are Strings. RuntimePermission(&quot;accessUserDefinedAttributes&quot;) is not required.
- AzureStorageFileAttributeView: A new type that will allow clients to set Storage related properties such as tier.

## [File](https://docs.oracle.com/javase/7/docs/api/java/io/File.html)

Many of these methods are implemented by deferring to the Files implementation of many of these methods (paying attention to differences in behavior). Here again, only the methods that are NOT implemented are listed as most of these methods can be deferred to another type and are therefore fairly transparent to implement.

- [isHidden](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#isHidden())
- [setWritable](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#setWritable(boolean,%20boolean))/[setReadable](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#setReadable(boolean,%20boolean))/[setExecutable](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#setExecutable(boolean,%20boolean))/[setLastmodified](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#setLastModified(long))/[setReadOnly](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#setReadOnly())

## [AsynchronousFileChannel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousFileChannel.html)

- [force](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousFileChannel.html#force(boolean)): No-op as we don&#39;t keep a local cache, so all write go directly to the service.
- [open](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousFileChannel.html#open(java.nio.file.Path,%20java.nio.file.OpenOption...)): See the above OpenOptions section for more information. Opening with the ExecutorService is not initially supported.
- [read](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousFileChannel.html#read(java.nio.ByteBuffer,%20long)): CompletionEventHandler not initially supported.
- [size](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousFileChannel.html#size())
- [write](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousFileChannel.html#write(java.nio.ByteBuffer,%20long,%20A,%20java.nio.channels.CompletionHandler)): CompletionEventHandler not initially supported. Additional checks are required before closing. Each write will add an entry to a (threadsafe) set of Strings that represent the range. At the time of closing, the set will be examined to ensure it forms a continuous range from 0 to the size of the blob. If it does not, an IOException will be thrown. If it does, the ranges will be converted to blockIDs and the list will be committed. This will enable parallel write scenarios for writing an entire file while ensuring that there is no random IO happening. Note that the docs do not specify the APPEND option for the open API. In this case, TRUNCATE\_EXISTING must be specified.

## [SeekableByteChannel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SeekableByteChannel.html)

See the above OpenOptions section for more information.

- [position](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SeekableByteChannel.html#position(long)): If the position is set to any value other than the current size of the file, attempting to write will throw an UnsupportedOperationException. In read-only workloads, the position may be set wherever the client desires. Reading may fail even after a write if the channel is opened to a new blob because the data will not have been committed yet.
- [read](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SeekableByteChannel.html#read(java.nio.ByteBuffer))
- [size](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SeekableByteChannel.html#size())
- [write](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SeekableByteChannel.html#write(java.nio.ByteBuffer))

## [FileChannel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/FileChannel.html)

Note that this implements a SeekableByteChannel. Many of the methods should be deferred to an internal instance or this type should extend from our implementation of SeekableByteChannel. As such, it&#39;s seeking and writing behavior is the same as SeekableByteChannel. Mapping is not supported. Locking is not supported. Note the possible connection between a FileChannel and the stream that created it; where possible, a FileChannel should reference the position of the stream rather than maintaining its own pointer.

# Open Questions/Further Development

The following are not immediately necessary but may reasonably be implemented at a later time.

- Symbolic links (createSymbolicLink) could be an empty blob with metadata field like x-ms-meta-link-target:path. Must be wary of link chains and circular links.
- Hard links (createLink)
- Hidden files (isHidden) could be a metadata field like x-ms-meta-is-hidden:true
- Random IO (newAsynchronousFileChannel, newSeekableByteChannel, newFileChannel). It would be theoretically possible to implement this functionality by downloading the file, working on a locally cached copy, and reuploading the file, but that incurs significant performance costs on large files (caching also introduces a significant amount of complexity when trying to maintain cache correctness and consistency in multithreaded environments). Because our MVP scenario is in workloads with large files, there is not much benefit to this option. Another alternative would be to use system wherein blocks roughly correlate to pages in traditional file I/O: the blockIds correspond to the range of data they hold. A random write would require downloading only a few blocks containing that range, making the desired edits, uploading the edited blocks, and re-committing the block list. This, too, introduces a large amount of complexity, a high number of round trip requests, and can be unsafe in multithreaded environments.
- Watches on directory events.
- PathMatcher (glob syntax?)
- File locks (leases? Can only be over the whole file. Can only be exclusive.)
- Read only FileSystem/Containers. Marking certain stores as read only could be configured in the initialization step if necessary. It could be a locally maintained list or we could require that the customer preconfigure the containers to be WORM containers.
- Opening AsyncFileChannel with the ExecutorService; reading with a CompletionEventHandler
- FileOwnership and POSIX permissions require ADLS. Random I/O may be improved with the use of Azure Files.
- Should we support AAD tokens? If so, we should probably look at azcopy for token refresh strategies.
- Which version should we release for GA? Should we jump to v12 to be consistent with other Storage offerings?
- Allowing customers access to single blobs. It is possible that customers may only need to access one blob from a given account. If that is the case, their credentials will likely be scoped just to that blob, and even checking the existence of a container upon initialization will be too restrictive. We can add an AzureStorageSkipInitialConnectionCheck parameter that bypasses this check and trusts the users credentials, allowing them access just to that blob.
- Providers built on other services. See comments in the &quot;Entry&quot; section.
- Some possible options for new flags include flags to optimize for certain behavior, to allow the filesystem to use all extant containers as FileStores rather than being restricted to the specified list, toggle the creation of specified containers, or to allow for specifying a CDN that can override the account name found in the URI.
