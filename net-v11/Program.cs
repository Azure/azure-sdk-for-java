using CommandLine;
using Microsoft.Azure.Storage;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Runtime;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace StoragePerfNet
{
    class Program
    {
        private const string _containerName = "testcontainer";
        private const string _blobName = "testblob";

        private const int _bytesPerMessage = 1024 * 10;
        private static readonly byte[] _payload;
        private static readonly Stream _payloadStream;

        private static int _downloads = 0;

        static Program()
        {
            _payload = new byte[_bytesPerMessage];

            // Initialize payload with stable random data since all-zeros may be compressed or optimized
            (new Random(0)).NextBytes(_payload);

            _payloadStream = new MemoryStream(_payload, writable: false);
        }

        public class Options
        {
            [Option("debug")]
            public bool Debug { get; set; }

            [Option('d', "duration", Default = 10)]
            public int Duration { get; set; }

            [Option('p', "parallel", Default = 1, HelpText = "Number of tasks to execute in parallel")]
            public int Parallel { get; set; }

            [Option('u', "upload")]
            public bool Upload { get; set; }
        }

        static async Task Main(string[] args)
        {
            if (!GCSettings.IsServerGC)
            {
                throw new InvalidOperationException("Requires server GC");
            }

            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            await Parser.Default.ParseArguments<Options>(args).MapResult(
                async o => await Run(connectionString, o),
                errors => Task.CompletedTask);
        }

        static async Task Run(string connectionString, Options options)
        {
#if DEBUG
            if (!options.Debug)
            {
                throw new InvalidOperationException("Requires release configuration");
            }
#endif

            CloudStorageAccount.TryParse(connectionString, out var storageAccount);
            var cloudBlobClient = storageAccount.CreateCloudBlobClient();
            var cloudBlobContainer = cloudBlobClient.GetContainerReference(_containerName);
            var cloudBlockBlob = cloudBlobContainer.GetBlockBlobReference(_blobName);

            Console.WriteLine($"Downloading blob '{_containerName}/{_blobName}' with {options.Parallel} parallel task(s) for {options.Duration} second(s)...");
            Console.WriteLine();

            var duration = TimeSpan.FromSeconds(options.Duration);
            var cts = new CancellationTokenSource(duration);
            var token = cts.Token;

            var tasks = new Task[options.Parallel];
            var sw = Stopwatch.StartNew();
            for (var i = 0; i < options.Parallel; i++)
            {
                tasks[i] = DownloadLoop(cloudBlockBlob, token);
            }
            _ = PrintStatus(token);
            await Task.WhenAll(tasks);
            sw.Stop();

            var elapsedSeconds = sw.Elapsed.TotalSeconds;
            var downloadsPerSecond = _downloads / elapsedSeconds;
            var megabytesPerSecond = (downloadsPerSecond * _bytesPerMessage) / (1024 * 1024);

            Console.WriteLine();
            Console.WriteLine($"Downloaded {_downloads} blobs of size {_bytesPerMessage} in {elapsedSeconds:N2}s " +
                        $"({downloadsPerSecond:N2} blobs/s, {megabytesPerSecond:N2} MB/s)");
        }

        static async Task DownloadLoop(CloudBlockBlob cloudBlockBlob, CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Download(cloudBlockBlob, token);
                    Interlocked.Increment(ref _downloads);
                }
                catch (OperationCanceledException)
                {
                }
                catch (ObjectDisposedException)
                {
                }
                catch (StorageException)
                {
                }
            }
        }

        static async Task Download(CloudBlockBlob cloudBlockBlob, CancellationToken token)
        {
            await cloudBlockBlob.DownloadToStreamAsync(Stream.Null, token);
        }

        static async Task PrintStatus(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(TimeSpan.FromSeconds(1), token);
                    Console.WriteLine(_downloads);
                }
                catch (OperationCanceledException)
                {
                }
                catch (ObjectDisposedException)
                {
                }
                catch (StorageException)
                {
                }
            }
        }
    }
}
