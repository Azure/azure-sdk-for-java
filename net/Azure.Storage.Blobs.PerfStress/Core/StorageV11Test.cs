using Azure.Test.PerfStress;
using Microsoft.Azure.Storage;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Collections.Generic;
using System.Text;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class StorageV11Test<TOptions> : PerfStressTest<TOptions> where TOptions : PerfStressOptions
    {
        private const string _containerName = "perfstress";

        protected CloudBlockBlob CloudBlockBlob { get; private set; }

        public StorageV11Test(string id, TOptions options) : base(id, options)
        {
            var blobName = this.GetType().Name.ToLowerInvariant() + id;
            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            if (string.IsNullOrEmpty(connectionString))
            {
                throw new InvalidOperationException("Undefined environment variable STORAGE_CONNECTION_STRING");
            }

            CloudStorageAccount.TryParse(connectionString, out var storageAccount);
            var cloudBlobClient = storageAccount.CreateCloudBlobClient();

            var cloudBlobContainer = cloudBlobClient.GetContainerReference(_containerName);

            try
            {
                cloudBlobContainer.Create();
            }
            catch (StorageException)
            {
            }


            CloudBlockBlob = cloudBlobContainer.GetBlockBlobReference(blobName);
        }
    }
}
