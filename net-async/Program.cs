using Azure.Core.Pipeline;
using Azure.Storage.Blobs;
using Azure.Storage.Common;
using CommandLine;
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
        
        private static int _downloads = 0;

        public class Options
        {
            [Option("debug")]
            public bool Debug { get; set; }

            [Option('d', "duration", Default = 10)]
            public int Duration { get; set; }

            [Option('p', "parallel", Default = 1, HelpText = "Number of tasks to execute in parallel")]
            public int Parallel { get; set; }

            [Option('s', "size", Default = 10 * 1024, HelpText = "Size of message (in bytes)")]
            public int Size { get; set; }

            [Option('t', "maximumThreadCount", Default = -1)]
            public int MaximumThreadCount { get; set; }

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

            var httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true;
            var httpClient = new HttpClient(httpClientHandler);

            var blobClientOptions = new BlobClientOptions();
            blobClientOptions.Transport = new HttpClientTransport(httpClient);

            var client = new BlobClient(connectionString, _containerName, _blobName, blobClientOptions);

            if (options.Upload)
            {
                await UploadAndVerifyDownload(client, options.Size);
            }

            var parallelTransferOptions = new ParallelTransferOptions();
            if (options.MaximumThreadCount != -1)
            {
                parallelTransferOptions.MaximumThreadCount = options.MaximumThreadCount;
            }

            Console.WriteLine($"Downloading blob '{_containerName}/{_blobName}' with {options.Parallel} parallel task(s) for {options.Duration} second(s)...");
            Console.WriteLine();

            var duration = TimeSpan.FromSeconds(options.Duration);
            var cts = new CancellationTokenSource(duration);
            var token = cts.Token;

            var tasks = new Task[options.Parallel];
            var sw = Stopwatch.StartNew();
            for (var i=0; i < options.Parallel; i++)
            {
                tasks[i] = DownloadLoop(client, parallelTransferOptions, token);
            }
            _ = PrintStatus(token);
            await Task.WhenAll(tasks);
            sw.Stop();

            var elapsedSeconds = sw.Elapsed.TotalSeconds;
            var downloadsPerSecond = _downloads / elapsedSeconds;
            var megabytesPerSecond = (downloadsPerSecond * options.Size) / (1024 * 1024);

            Console.WriteLine();
            Console.WriteLine($"Downloaded {_downloads} blobs of size {options.Size} in {elapsedSeconds:N2}s " +
                        $"({downloadsPerSecond:N2} blobs/s, {megabytesPerSecond:N2} MB/s)");
        }

        static async Task DownloadLoop(BlobClient client, ParallelTransferOptions parallelTransferOptions, CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await client.DownloadAsync(Stream.Null, parallelTransferOptions: parallelTransferOptions, cancellationToken: token);
                    Interlocked.Increment(ref _downloads);
                }
                catch (OperationCanceledException)
                {
                }
            }
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
            }
        }

        static async Task UploadAndVerifyDownload(BlobClient client, int size)
        {
            var payload = new byte[size];
            // Initialize payload with stable random data since all-zeros may be compressed or optimized
            (new Random(0)).NextBytes(payload);
            var payloadStream = new MemoryStream(payload, writable: false);

            await client.UploadAsync(payloadStream);

            var downloadResponse = await client.DownloadAsync();

            byte[] downloadPayload;
            using (var memoryStream = new MemoryStream((int)downloadResponse.Value.ContentLength))
            {
                await downloadResponse.Value.Content.CopyToAsync(memoryStream);
                downloadPayload = memoryStream.ToArray();
            }

            if (!((ReadOnlySpan<byte>)payload).SequenceEqual(downloadPayload))
            {
                var sb = new StringBuilder();
                sb.Append($"Downloaded {downloadPayload.Length} bytes, not equal to uploaded {payload.Length} bytes.");

                if (downloadPayload.Length == payload.Length)
                {
                    for (var i = 0; i < payload.Length; i++)
                    {
                        if (payload[i] != downloadPayload[i])
                        {
                            sb.Append($"First difference at position {i}.  Uploaded {payload[i]}, downloaded {downloadPayload[i]}");
                            break;
                        }
                    }
                }

                throw new InvalidOperationException(sb.ToString());
            }
        }
    }
}
