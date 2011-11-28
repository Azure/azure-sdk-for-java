package com.microsoft.windowsazure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

import javax.inject.Inject;

import com.microsoft.windowsazure.services.blob.models.AcquireLeaseOptions;
import com.microsoft.windowsazure.services.blob.models.AcquireLeaseResult;
import com.microsoft.windowsazure.services.blob.models.BlobServiceOptions;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ContainerACL;
import com.microsoft.windowsazure.services.blob.models.CopyBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesResult;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteBlobOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerACLResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobsResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;
import com.microsoft.windowsazure.services.blob.models.PageRange;
import com.microsoft.windowsazure.services.blob.models.ServiceProperties;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.SetContainerMetadataOptions;
import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;

public class BlobService implements BlobContract {
    final BlobContract next;

    public BlobService() throws Exception {
        this(null, Configuration.getInstance());
    }

    public BlobService(Configuration config) throws Exception {
        this(null, config);
    }

    public BlobService(String profile) throws Exception {
        this(profile, Configuration.getInstance());
    }

    public BlobService(String profile, Configuration config) throws Exception {
        next = config.create(profile, BlobContract.class);
    }

    @Inject
    public BlobService(BlobContract next) throws Exception {
        this.next = next;
    }

    public BlobContract withFilter(ServiceFilter filter) {
        return next.withFilter(filter);
    }

    public GetServicePropertiesResult getServiceProperties() throws ServiceException {
        return next.getServiceProperties();
    }

    public GetServicePropertiesResult getServiceProperties(BlobServiceOptions options) throws ServiceException {
        return next.getServiceProperties(options);
    }

    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        next.setServiceProperties(serviceProperties);
    }

    public void setServiceProperties(ServiceProperties serviceProperties, BlobServiceOptions options)
            throws ServiceException {
        next.setServiceProperties(serviceProperties, options);
    }

    public ListContainersResult listContainers() throws ServiceException {
        return next.listContainers();
    }

    public ListContainersResult listContainers(ListContainersOptions options) throws ServiceException {
        return next.listContainers(options);
    }

    public void createContainer(String container) throws ServiceException {
        next.createContainer(container);
    }

    public void createContainer(String container, CreateContainerOptions options) throws ServiceException {
        next.createContainer(container, options);
    }

    public void deleteContainer(String container) throws ServiceException {
        next.deleteContainer(container);
    }

    public void deleteContainer(String container, DeleteContainerOptions options) throws ServiceException {
        next.deleteContainer(container, options);
    }

    public GetContainerPropertiesResult getContainerProperties(String container) throws ServiceException {
        return next.getContainerProperties(container);
    }

    public GetContainerPropertiesResult getContainerProperties(String container, BlobServiceOptions options)
            throws ServiceException {
        return next.getContainerProperties(container, options);
    }

    public GetContainerPropertiesResult getContainerMetadata(String container) throws ServiceException {
        return next.getContainerMetadata(container);
    }

    public GetContainerPropertiesResult getContainerMetadata(String container, BlobServiceOptions options)
            throws ServiceException {
        return next.getContainerMetadata(container, options);
    }

    public GetContainerACLResult getContainerACL(String container) throws ServiceException {
        return next.getContainerACL(container);
    }

    public GetContainerACLResult getContainerACL(String container, BlobServiceOptions options) throws ServiceException {
        return next.getContainerACL(container, options);
    }

    public void setContainerACL(String container, ContainerACL acl) throws ServiceException {
        next.setContainerACL(container, acl);
    }

    public void setContainerACL(String container, ContainerACL acl, BlobServiceOptions options) throws ServiceException {
        next.setContainerACL(container, acl, options);
    }

    public void setContainerMetadata(String container, HashMap<String, String> metadata) throws ServiceException {
        next.setContainerMetadata(container, metadata);
    }

    public void setContainerMetadata(String container, HashMap<String, String> metadata,
            SetContainerMetadataOptions options) throws ServiceException {
        next.setContainerMetadata(container, metadata, options);
    }

    public ListBlobsResult listBlobs(String container) throws ServiceException {
        return next.listBlobs(container);
    }

    public ListBlobsResult listBlobs(String container, ListBlobsOptions options) throws ServiceException {
        return next.listBlobs(container, options);
    }

    public void createPageBlob(String container, String blob, int length) throws ServiceException {
        next.createPageBlob(container, blob, length);
    }

    public void createPageBlob(String container, String blob, int length, CreateBlobOptions options)
            throws ServiceException {
        next.createPageBlob(container, blob, length, options);
    }

    public void createBlockBlob(String container, String blob, InputStream contentStream) throws ServiceException {
        next.createBlockBlob(container, blob, contentStream);
    }

    public void createBlockBlob(String container, String blob, InputStream contentStream, CreateBlobOptions options)
            throws ServiceException {
        next.createBlockBlob(container, blob, contentStream, options);
    }

    public CreateBlobPagesResult clearBlobPages(String container, String blob, PageRange range) throws ServiceException {
        return next.clearBlobPages(container, blob, range);
    }

    public CreateBlobPagesResult clearBlobPages(String container, String blob, PageRange range,
            CreateBlobPagesOptions options) throws ServiceException {
        return next.clearBlobPages(container, blob, range, options);
    }

    public CreateBlobPagesResult createBlobPages(String container, String blob, PageRange range, long length,
            InputStream contentStream) throws ServiceException {
        return next.createBlobPages(container, blob, range, length, contentStream);
    }

    public CreateBlobPagesResult createBlobPages(String container, String blob, PageRange range, long length,
            InputStream contentStream, CreateBlobPagesOptions options) throws ServiceException {
        return next.createBlobPages(container, blob, range, length, contentStream, options);
    }

    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream)
            throws ServiceException {
        next.createBlobBlock(container, blob, blockId, contentStream);
    }

    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream,
            CreateBlobBlockOptions options) throws ServiceException {
        next.createBlobBlock(container, blob, blockId, contentStream, options);
    }

    public void commitBlobBlocks(String container, String blob, BlockList blockList) throws ServiceException {
        next.commitBlobBlocks(container, blob, blockList);
    }

    public void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options)
            throws ServiceException {
        next.commitBlobBlocks(container, blob, blockList, options);
    }

    public ListBlobBlocksResult listBlobBlocks(String container, String blob) throws ServiceException {
        return next.listBlobBlocks(container, blob);
    }

    public ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options)
            throws ServiceException {
        return next.listBlobBlocks(container, blob, options);
    }

    public GetBlobPropertiesResult getBlobProperties(String container, String blob) throws ServiceException {
        return next.getBlobProperties(container, blob);
    }

    public GetBlobPropertiesResult getBlobProperties(String container, String blob, GetBlobPropertiesOptions options)
            throws ServiceException {
        return next.getBlobProperties(container, blob, options);
    }

    public GetBlobMetadataResult getBlobMetadata(String container, String blob) throws ServiceException {
        return next.getBlobMetadata(container, blob);
    }

    public GetBlobMetadataResult getBlobMetadata(String container, String blob, GetBlobMetadataOptions options)
            throws ServiceException {
        return next.getBlobMetadata(container, blob, options);
    }

    public ListBlobRegionsResult listBlobRegions(String container, String blob) throws ServiceException {
        return next.listBlobRegions(container, blob);
    }

    public ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options)
            throws ServiceException {
        return next.listBlobRegions(container, blob, options);
    }

    public SetBlobPropertiesResult setBlobProperties(String container, String blob) throws ServiceException {
        return next.setBlobProperties(container, blob);
    }

    public SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options)
            throws ServiceException {
        return next.setBlobProperties(container, blob, options);
    }

    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata)
            throws ServiceException {
        return next.setBlobMetadata(container, blob, metadata);
    }

    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata,
            SetBlobMetadataOptions options) throws ServiceException {
        return next.setBlobMetadata(container, blob, metadata, options);
    }

    public GetBlobResult getBlob(String container, String blob) throws ServiceException {
        return next.getBlob(container, blob);
    }

    public GetBlobResult getBlob(String container, String blob, GetBlobOptions options) throws ServiceException {
        return next.getBlob(container, blob, options);
    }

    public void deleteBlob(String container, String blob) throws ServiceException {
        next.deleteBlob(container, blob);
    }

    public void deleteBlob(String container, String blob, DeleteBlobOptions options) throws ServiceException {
        next.deleteBlob(container, blob, options);
    }

    public CreateBlobSnapshotResult createBlobSnapshot(String container, String blob) throws ServiceException {
        return next.createBlobSnapshot(container, blob);
    }

    public CreateBlobSnapshotResult createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options)
            throws ServiceException {
        return next.createBlobSnapshot(container, blob, options);
    }

    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob)
            throws ServiceException {
        next.copyBlob(destinationContainer, destinationBlob, sourceContainer, sourceBlob);
    }

    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer,
            String sourceBlob, CopyBlobOptions options) throws ServiceException {
        next.copyBlob(destinationContainer, destinationBlob, sourceContainer, sourceBlob, options);
    }

    public AcquireLeaseResult acquireLease(String container, String blob) throws ServiceException {
        return next.acquireLease(container, blob);
    }

    public AcquireLeaseResult acquireLease(String container, String blob, AcquireLeaseOptions options)
            throws ServiceException {
        return next.acquireLease(container, blob, options);
    }

    public AcquireLeaseResult renewLease(String container, String blob, String leaseId) throws ServiceException {
        return next.renewLease(container, blob, leaseId);
    }

    public AcquireLeaseResult renewLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException {
        return next.renewLease(container, blob, leaseId, options);
    }

    public void releaseLease(String container, String blob, String leaseId) throws ServiceException {
        next.releaseLease(container, blob, leaseId);
    }

    public void releaseLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException {
        next.releaseLease(container, blob, leaseId, options);
    }

    public void breakLease(String container, String blob, String leaseId) throws ServiceException {
        next.breakLease(container, blob, leaseId);
    }

    public void breakLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException {
        next.breakLease(container, blob, leaseId, options);
    }
}
