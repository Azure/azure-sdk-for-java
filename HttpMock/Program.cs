using CommandLine;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Runtime;

namespace HttpMock
{
    class Program
    {
        private static ConcurrentDictionary<RequestCacheKey, HttpResponseMessage> _cache =
            new ConcurrentDictionary<RequestCacheKey, HttpResponseMessage>();


        public class HttpMockOptions
        {
            [Option("debug")]
            public bool Debug { get; set; }

            [Option("trace")]
            public bool Trace { get; set; }
        }

        public static HttpMockOptions Options { get; private set; }

        static void Main(string[] args)
        {
            if (!GCSettings.IsServerGC)
            {
                throw new InvalidOperationException("Requires server GC");
            }

            Parser.Default.ParseArguments<HttpMockOptions>(args).WithParsed(o =>
            {
                Options = o;
                Run();
            });
        }

        static void Run()
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
                    Trace("Start Request");

                    var request = context.Request;
                    var response = context.Response;

                    Log.LogRequest(request);

                    var key = new RequestCacheKey(request);

                    Trace("Created Cache Key");

                    if (_cache.TryGetValue(key, out var upstreamResponse))
                    {
                        Trace("Cache Hit");
                        Log.LogUpstreamResponse(upstreamResponse, cached: true);
                        
                        await Proxy.SendDownstreamResponse(upstreamResponse, response);

                        Trace("Sent Downstream Response");
                    }
                    else
                    {
                        Trace("Cache Miss");
                        
                        upstreamResponse = await Proxy.SendUpstreamRequest(request);

                        Trace("Received Upstream Response");                        
                        Log.LogUpstreamResponse(upstreamResponse, cached: false);

                        await Proxy.SendDownstreamResponse(upstreamResponse, response);

                        Trace("Sent Downstream Response");

                        _cache.AddOrUpdate(key, upstreamResponse, (k, r) => upstreamResponse);

                        Trace("Updated Cache");
                    }

                    Trace("End Request" + Environment.NewLine);
                }))
                .Build()
                .Run();
        }
         
        private static void Trace(string message)
        {
            if (Options.Trace)
            {
                Console.WriteLine($"[{DateTime.Now.ToString("o")}] {message}");
            }
        }
    }
}
