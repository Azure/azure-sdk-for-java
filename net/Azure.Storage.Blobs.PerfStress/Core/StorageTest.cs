using Azure.Core.Pipeline;
using Azure.Test.PerfStress;
using System;
using System.Net.Http;

namespace Azure.Storage.Blobs.PerfStress
{
    public abstract class StorageTest<TOptions> : PerfStressTest<TOptions> where TOptions: PerfStressOptions
    {
        private const string _containerName = "testcontainer";

        protected BlobClient BlobClient { get; private set; }

        public StorageTest(TOptions options) : base(options)
        {
            var blobName = this.GetType().Name.ToLowerInvariant();
            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            var httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true;
            var httpClient = new HttpClient(httpClientHandler);

            var blobClientOptions = new BlobClientOptions();
            blobClientOptions.Transport = new HttpClientTransport(httpClient);

            BlobClient = new BlobClient(connectionString, _containerName, blobName, blobClientOptions);
        }
    }
}
