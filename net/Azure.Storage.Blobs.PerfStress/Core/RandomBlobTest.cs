using Azure.Storage.Blobs.Specialized;
using System;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class RandomBlobTest<TOptions> : ContainerTest<TOptions> where TOptions : SizeOptions
    {
        protected BlobClient BlobClient { get; private set; }
        protected BlockBlobClient BlockBlobClient { get; private set; }

        public RandomBlobTest(TOptions options) : base(options)
        {
            var blobName = "uploadtest-" + Guid.NewGuid();
            BlobClient = BlobContainerClient.GetBlobClient(blobName);
            BlockBlobClient = BlobContainerClient.GetBlockBlobClient(blobName);
        }
    }
}
