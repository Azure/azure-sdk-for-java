using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Net.Http;

namespace HttpMock
{
    class Program
    {
        private static ConcurrentDictionary<RequestCacheKey, HttpResponseMessage> _cache =
            new ConcurrentDictionary<RequestCacheKey, HttpResponseMessage>();

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

                    Log.LogRequest(request);

                    var key = new RequestCacheKey(request);
                    if (_cache.TryGetValue(key, out var upstreamResponse))
                    {
                        Log.LogUpstreamResponse(upstreamResponse, cached: true);
                        await Proxy.SendDownstreamResponse(upstreamResponse, response);
                    }
                    else
                    {
                        upstreamResponse = await Proxy.SendUpstreamRequest(request);
                        Log.LogUpstreamResponse(upstreamResponse, cached: false);
                        await Proxy.SendDownstreamResponse(upstreamResponse, response);
                        _cache.AddOrUpdate(key, upstreamResponse, (k, r) => upstreamResponse);
                    }

                }))
                .Build()
                .Run();
        }
    }
}
