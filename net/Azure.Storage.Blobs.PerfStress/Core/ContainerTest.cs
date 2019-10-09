using Azure.Test.PerfStress;
using System;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class ContainerTest<TOptions> : ServiceTest<TOptions> where TOptions : PerfStressOptions
    {
        protected static string ContainerName { get; } = "perfstress-" + Guid.NewGuid().ToString();

        protected BlobContainerClient BlobContainerClient { get; private set; }

        public ContainerTest(TOptions options) : base(options)
        {
            BlobContainerClient = BlobServiceClient.GetBlobContainerClient(ContainerName);
        }

        public override async Task GlobalSetup()
        {
            await base.GlobalSetup();
            await BlobContainerClient.CreateAsync();
        }

        public override async Task GlobalCleanup()
        {
            await BlobContainerClient.DeleteAsync();
            await base.GlobalCleanup();
        }
    }
}
