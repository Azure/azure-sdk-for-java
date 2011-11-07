package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class BlobServiceImpl implements BlobService {
    private static Log log = LogFactory.getLog(BlobServiceImpl.class);
    private final BlobService service;

    public BlobServiceImpl(BlobServiceForJersey service) {
        this.service = service;
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("blob", e);
    }

    public ServiceProperties getServiceProperties() throws ServiceException {
        try {
            return service.getServiceProperties();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        try {
            service.setServiceProperties(serviceProperties);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListContainersResult listContainers() throws ServiceException {
        try {
            return service.listContainers();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListContainersResult listContainers(ListContainersOptions options) throws ServiceException {
        try {
            return service.listContainers(options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createContainer(String container) throws ServiceException {
        try {
            service.createContainer(container);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createContainer(String container, CreateContainerOptions options) throws ServiceException {
        try {
            service.createContainer(container, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteContainer(String container) throws ServiceException {
        try {
            service.deleteContainer(container);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteContainer(String container, DeleteContainerOptions options) throws ServiceException {
        try {
            service.deleteContainer(container, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ContainerProperties getContainerProperties(String container) throws ServiceException {
        try {
            return service.getContainerProperties(container);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ContainerProperties getContainerMetadata(String container) throws ServiceException {
        try {
            return service.getContainerMetadata(container);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ContainerACL getContainerACL(String container) throws ServiceException {
        try {
            return service.getContainerACL(container);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setContainerACL(String container, ContainerACL acl) throws ServiceException {
        try {
            service.setContainerACL(container, acl);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setContainerMetadata(String container, HashMap<String, String> metadata) throws ServiceException {
        try {
            service.setContainerMetadata(container, metadata);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setContainerMetadata(String container, HashMap<String, String> metadata, SetContainerMetadataOptions options) throws ServiceException {
        try {
            service.setContainerMetadata(container, metadata, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListBlobsResult listBlobs(String container) throws ServiceException {
        try {
            return service.listBlobs(container);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListBlobsResult listBlobs(String container, ListBlobsOptions options) throws ServiceException {
        try {
            return service.listBlobs(container, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createPageBlob(String container, String blob, int length) throws ServiceException {
        try {
            service.createPageBlob(container, blob, length);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createPageBlob(String container, String blob, int length, CreateBlobOptions options) throws ServiceException {
        try {
            service.createPageBlob(container, blob, length, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createBlockBlob(String container, String blob, InputStream contentStream) throws ServiceException {
        try {
            service.createBlockBlob(container, blob, contentStream);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createBlockBlob(String container, String blob, InputStream contentStream, CreateBlobOptions options) throws ServiceException {
        try {
            service.createBlockBlob(container, blob, contentStream, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd) throws ServiceException {
        try {
            return service.clearBlobPages(container, blob, rangeStart, rangeEnd);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd, CreateBlobPagesOptions options)
            throws ServiceException {
        try {
            return service.clearBlobPages(container, blob, rangeStart, rangeEnd, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream)
            throws ServiceException {
        try {
            return service.createBlobPages(container, blob, rangeStart, rangeEnd, length, contentStream);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream,
            CreateBlobPagesOptions options) throws ServiceException {
        try {
            return service.createBlobPages(container, blob, rangeStart, rangeEnd, length, contentStream, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream) throws ServiceException {
        try {
            service.createBlobBlock(container, blob, blockId, contentStream);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream, CreateBlobBlockOptions options)
            throws ServiceException {
        try {
            service.createBlobBlock(container, blob, blockId, contentStream, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void commitBlobBlocks(String container, String blob, BlockList blockList) throws ServiceException {
        try {
            service.commitBlobBlocks(container, blob, blockList);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options) throws ServiceException {
        try {
            service.commitBlobBlocks(container, blob, blockList, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListBlobBlocksResult listBlobBlocks(String container, String blob) throws ServiceException {
        try {
            return service.listBlobBlocks(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options) throws ServiceException {
        try {
            return service.listBlobBlocks(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public BlobProperties getBlobProperties(String container, String blob) throws ServiceException {
        try {
            return service.getBlobProperties(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options) throws ServiceException {
        try {
            return service.getBlobProperties(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetBlobMetadataResult getBlobMetadata(String container, String blob) throws ServiceException {
        try {
            return service.getBlobMetadata(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetBlobMetadataResult getBlobMetadata(String container, String blob, GetBlobMetadataOptions options) throws ServiceException {
        try {
            return service.getBlobMetadata(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListBlobRegionsResult listBlobRegions(String container, String blob) throws ServiceException {
        try {
            return service.listBlobRegions(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options) throws ServiceException {
        try {
            return service.listBlobRegions(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options) throws ServiceException {
        try {
            return service.setBlobProperties(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata) throws ServiceException {
        try {
            return service.setBlobMetadata(container, blob, metadata);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata, SetBlobMetadataOptions options)
            throws ServiceException {
        try {
            return service.setBlobMetadata(container, blob, metadata, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public Blob getBlob(String container, String blob) throws ServiceException {
        try {
            return service.getBlob(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public Blob getBlob(String container, String blob, GetBlobOptions options) throws ServiceException {
        try {
            return service.getBlob(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteBlob(String container, String blob) throws ServiceException {
        try {
            service.deleteBlob(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteBlob(String container, String blob, DeleteBlobOptions options) throws ServiceException {
        try {
            service.deleteBlob(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public BlobSnapshot createBlobSnapshot(String container, String blob) throws ServiceException {
        try {
            return service.createBlobSnapshot(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public BlobSnapshot createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options) throws ServiceException {
        try {
            return service.createBlobSnapshot(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob) throws ServiceException {
        try {
            service.copyBlob(destinationContainer, destinationBlob, sourceContainer, sourceBlob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob, CopyBlobOptions options)
            throws ServiceException {
        try {
            service.copyBlob(destinationContainer, destinationBlob, sourceContainer, sourceBlob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public String acquireLease(String container, String blob) throws ServiceException {
        try {
            return service.acquireLease(container, blob);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public String acquireLease(String container, String blob, AcquireLeaseOptions options) throws ServiceException {
        try {
            return service.acquireLease(container, blob, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public String renewLease(String container, String blob, String leaseId) throws ServiceException {
        try {
            return service.renewLease(container, blob, leaseId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void releaseLease(String container, String blob, String leaseId) throws ServiceException {
        try {
            service.releaseLease(container, blob, leaseId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void breakLease(String container, String blob, String leaseId) throws ServiceException {
        try {
            service.breakLease(container, blob, leaseId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }
}
