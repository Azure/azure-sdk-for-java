using CommandLine;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Runtime;

namespace HttpMock
{
    class Program
    {
        // PERF: Store custom value instead of HttpResponseMessage
        private static ConcurrentDictionary<RequestCacheKey, HttpResponseMessage> _cache =
            new ConcurrentDictionary<RequestCacheKey, HttpResponseMessage>();


        public class HttpMockOptions
        {
            [Option("debug")]
            public bool Debug { get; set; }

            [Option("silent")]
            public bool Silent { get; set; }

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
            new WebHostBuilder()
                .UseKestrel(options =>
                {
                    options.Listen(IPAddress.Any, 8888);
                    options.Listen(IPAddress.Any, 8889, listenOptions =>
                    {
                        listenOptions.UseHttps("testCert.pfx", "testPassword");
                    });
                })
                .UseContentRoot(Directory.GetCurrentDirectory())
                .Configure(app => app.Run(async context =>
                {
                    // var sw = Stopwatch.StartNew();

                    // Trace("Start Request", sw);

                    var request = context.Request;
                    var response = context.Response;

                    // Log.LogRequest(request);

                    var key = new RequestCacheKey(request);

                    // Trace("Created Cache Key", sw);

                    if (_cache.TryGetValue(key, out var upstreamResponse))
                    {
                        // Trace("Cache Hit", sw);
                        // Log.LogUpstreamResponse(upstreamResponse, cached: true);
                        
                        await Proxy.SendDownstreamResponse(upstreamResponse, response);

                        // Trace("Sent Downstream Response", sw);
                    }
                    else
                    {
                        // Trace("Cache Miss", sw);
                        
                        upstreamResponse = await Proxy.SendUpstreamRequest(request);

                        // Trace("Received Upstream Response", sw);
                        // Log.LogUpstreamResponse(upstreamResponse, cached: false);

                        await Proxy.SendDownstreamResponse(upstreamResponse, response);

                        // Trace("Sent Downstream Response", sw);

                        _cache.AddOrUpdate(key, upstreamResponse, (k, r) => upstreamResponse);

                        // Trace("Updated Cache", sw);
                    }

                    // Trace("End Request" + Environment.NewLine, sw);
                }))
                .Build()
                .Run();
        }
         
        private static void Trace(string message, Stopwatch stopwatch)
        {
            if (Options.Trace)
            {
                Console.WriteLine($"[{stopwatch.Elapsed.TotalSeconds:N4}] {message}");
            }
        }
    }
}
