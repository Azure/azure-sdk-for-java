using Azure.Storage.Blobs.PerfStress.Core;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadFromFileTest : RandomBlobTest<StorageTransferOptionsOptions>
    {
        private static string _tempFile;

        public UploadFromFileTest(StorageTransferOptionsOptions options) : base(options)
        {
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            _tempFile = Path.GetTempFileName();

            using var randomStream = RandomStream.Create(Options.Size);
            using var fileStream = File.OpenWrite(_tempFile);
            await randomStream.CopyToAsync(fileStream);
            fileStream.Close();
        }

        public override async Task GlobalCleanupAsync()
        {
            File.Delete(_tempFile);
            await base.GlobalCleanupAsync();
        }

        public override void Run(CancellationToken cancellationToken)
        {
            BlobClient.Upload(_tempFile, transferOptions: Options.StorageTransferOptions, cancellationToken: cancellationToken);
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            return BlobClient.UploadAsync(_tempFile, transferOptions: Options.StorageTransferOptions, cancellationToken: cancellationToken);
        }
    }
}
