using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Threading.Tasks;

namespace StorageMock
{
    class Program
    {
        private static readonly Dictionary<string, (byte[], DateTime)> _files = new Dictionary<string, (byte[], DateTime)>();

        static void Main(string[] args)
        {
            Console.WriteLine("Connection String (unencrypted): \"BlobEndpoint=http://<hostname>:5000\"");
            Console.WriteLine("Connection String (encrypted): \"BlobEndpoint=https://<hostname>:5001\"");
            Console.WriteLine();

            new WebHostBuilder()
                .UseKestrel(options =>
                {
                    options.Listen(IPAddress.Any, 5000);
                    options.Listen(IPAddress.Any, 5001, listenOptions =>
                    {
                        listenOptions.UseHttps("testCert.pfx", "testPassword");
                    });
                })
                .UseContentRoot(Directory.GetCurrentDirectory())
                .Configure(app => app.Run(async context =>
                {
                    var request = context.Request;
                    var response = context.Response;
                    if (request.Method == HttpMethods.Put)
                    {
                        await Put(request, response);
                    }
                    else if (request.Method == HttpMethods.Get)
                    {
                        if (request.Path == "/debug")
                        {
                            await Debug(request, response);
                        }
                        else
                        {
                            await Get(request, response);
                        }
                    }

                }))
                .Build()
                .Run();
        }

        private static async Task Put(HttpRequest request, HttpResponse response)
        {
            byte[] bodyContent;

            using (var memoryStream = new MemoryStream())
            {
                await request.Body.CopyToAsync(memoryStream);
                bodyContent = memoryStream.ToArray();
            }

            _files[request.Path] = (bodyContent, DateTime.UtcNow);

            // HTTP/1.1 201 Created
            // Content-MD5: 1IbBQxpftsNd4a6CAVvHIQ==
            // Last-Modified: Tue, 27 Aug 2019 01:24:52 GMT
            // ETag: "0x8D72A8D5CA707D9"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 9dd11a98-f01e-00af-5976-5ce8eb000000
            // x-ms-version: 2018-11-09
            // x-ms-request-server-encrypted: true
            // Date: Tue, 27 Aug 2019 01:24:51 GMT
            // Content-Length: 0

            response.StatusCode = (int)HttpStatusCode.Created;

            var headers = response.Headers;

            // TODO: Compute MD5
            headers.Add("Content-MD5", "1IbBQxpftsNd4a6CAVvHIQ==");

            // Current time in RFC1123 format ("Mon, 15 Jun 2009 20:45:30 GMT")
            headers.Add("Last-Modified", DateTime.Now.ToString("r"));

            // TODO: Compute ETag
            headers.Add("ETag", "\"0x8D72A8D5CA707D9\"");

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            // TODO: Compute GUID
            headers.Add("x-ms-request-id", "9dd11a98-f01e-00af-5976-5ce8eb000000");

            headers.Add("x-ms-version", "2018-11-09");

            headers.Add("x-ms-request-server-encrypted", "true");
        }

        private static async Task Get(HttpRequest request, HttpResponse response)
        {
            // HTTP/1.1 206 Partial Content
            // Content-Length: 1024
            // Content-Type: application/octet-stream
            // Content-Range: bytes 0-1023/1024
            // Last-Modified: Tue, 27 Aug 2019 01:24:52 GMT
            // Accept-Ranges: bytes
            // ETag: "0x8D72A8D5CA707D9"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 9dd11a9d-f01e-00af-5d76-5ce8eb000000
            // x-ms-version: 2018-11-09
            // x-ms-tag-count: 0
            // x-ms-creation-time: Tue, 27 Aug 2019 01:23:14 GMT
            // x-ms-blob-content-md5: 1IbBQxpftsNd4a6CAVvHIQ==
            // x-ms-lease-status: unlocked
            // x-ms-lease-state: available
            // x-ms-blob-type: BlockBlob
            // x-ms-server-encrypted: true
            // Date: Tue, 27 Aug 2019 01:24:52 GMT

            var (content, lastUpdated) = _files[request.Path];

            response.StatusCode = (int)HttpStatusCode.PartialContent;
            response.ContentLength = content.Length;
            response.ContentType = "application/octet-stream";

            var headers = response.Headers;
            
            headers.Add("Content-Range", $"bytes 0-{content.Length - 1}/{content.Length}");

            // Last modified time in RFC1123 format ("Mon, 15 Jun 2009 20:45:30 GMT")
            headers.Add("Last-Modified", lastUpdated.ToString("r"));

            headers.Add("Accept-Ranges", "bytes");

            // TODO: Compute ETag
            headers.Add("ETag", "\"0x8D72A8D5CA707D9\"");

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            // TODO: Compute GUID
            headers.Add("x-ms-request-id", "9dd11a98-f01e-00af-5976-5ce8eb000000");

            headers.Add("x-ms-version", "2018-11-09");

            headers.Add("x-ms-tag-count", "0");

            // TODO: Track creation time separate from last updated time
            // Creation time in RFC1123 format ("Mon, 15 Jun 2009 20:45:30 GMT")
            headers.Add("x-ms-creation-time", lastUpdated.ToString("r"));

            // TODO: Compute MD5
            headers.Add("x-ms-blob-content-md5", "1IbBQxpftsNd4a6CAVvHIQ==");

            headers.Add("x-ms-lease-status", "unlocked");
            headers.Add("x-ms-lease-state", "available");
            headers.Add("x-ms-blob-type", "BlockBlob");
            headers.Add("x-ms-server-encrypted", "true");

            await response.Body.WriteAsync(content, 0, content.Length);
        }

        private static Task Debug(HttpRequest request, HttpResponse response)
        {
            response.StatusCode = (int)HttpStatusCode.OK;

            foreach (var kvp in _files)
            {
                Console.WriteLine($"{kvp.Key} {kvp.Value.Item2}");
                var payload = kvp.Value.Item1;
                for (var i=0; i < payload.Length; i++)
                {
                    Console.Write(payload[i] + " ");
                }
                Console.WriteLine();
                Console.WriteLine();
            }

            return Task.CompletedTask;
        }
    }
}
