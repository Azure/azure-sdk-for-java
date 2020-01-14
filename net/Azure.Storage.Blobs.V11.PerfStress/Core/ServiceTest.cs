using Azure.Test.PerfStress;
using Microsoft.Azure.Storage;
using Microsoft.Azure.Storage.Blob;
using System;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class ServiceTest<TOptions> : PerfStressTest<TOptions> where TOptions : PerfStressOptions
    {
        protected CloudBlobClient CloudBlobClient { get; private set; }

        public ServiceTest(TOptions options) : base(options)
        {
            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            if (string.IsNullOrEmpty(connectionString))
            {
                throw new InvalidOperationException("Undefined environment variable STORAGE_CONNECTION_STRING");
            }

            CloudStorageAccount.TryParse(connectionString, out var storageAccount);


            CloudBlobClient = storageAccount.CreateCloudBlobClient();
        }
    }
}
