using Azure.Core.Pipeline;
using Azure.Test.PerfStress;
using System;
using System.Net.Http;

namespace Azure.Storage.Blobs.PerfStress
{
    public abstract class ServiceTest<TOptions> : PerfStressTest<TOptions> where TOptions : PerfStressOptions
    {
        protected BlobServiceClient BlobServiceClient { get; private set; }

        public ServiceTest(TOptions options) : base(options)
        {
            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            if (string.IsNullOrEmpty(connectionString))
            {
                throw new InvalidOperationException("Undefined environment variable STORAGE_CONNECTION_STRING");
            }

            var blobClientOptions = new BlobClientOptions()
            {
                Transport = PerfStressTransport.Create(options)
            };

            BlobServiceClient = new BlobServiceClient(connectionString, blobClientOptions);
        }
    }
}
