using CommandLine;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime;
using System.Text;
using System.Text.Json;

namespace HttpMock
{
    class Program
    {
        private static ConcurrentDictionary<RequestCacheKey, UpstreamResponse> _cache =
            new ConcurrentDictionary<RequestCacheKey, UpstreamResponse>();

        // Used for perf testing the cache lookup and downstream response generation.  This allows a perf client like
        // "wrk" to directly request the last response without using the server as an HTTP proxy, since "wrk" is much
        // slower when using a proxy (50k vs 6k RPS).
        private static UpstreamResponse _lastUpstreamResponse;

        public class HttpMockOptions
        {
            [Option("cachelast")]
            public bool CacheLast { get; set; }

            [Option("debug", HelpText = "Exposes a /debug URL for debugging basic functionality")]
            public bool Debug { get; set; }

            [Option("dots")]
            public bool Dots { get; set; }

            [Option('h', "headers")]
            public IEnumerable<string> Headers { get; set; }

            [Option('s', "service")]
            public Service? Service { get; set; }
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
            Console.WriteLine("=== Options ===");
            Console.WriteLine(JsonSerializer.Serialize(Options, Options.GetType(), new JsonSerializerOptions()
            {
                WriteIndented = true,                
            }));
            Console.WriteLine();

            var allHeaders = Options.Headers.Concat(ServiceHeaders.Get(Options.Service));

            Console.WriteLine("=== Headers ===");
            foreach (var header in allHeaders)
            {
                Console.WriteLine(header);
            }
            Console.WriteLine();

            Console.WriteLine("=== Startup ===");
            new WebHostBuilder()
                .UseKestrel(options =>
                {
                    options.Listen(IPAddress.Any, 7777);
                    options.Listen(IPAddress.Any, 7778, listenOptions =>
                    {
                        listenOptions.UseHttps("testCert.pfx", "testPassword");
                    });
                })
                .UseContentRoot(Directory.GetCurrentDirectory())
                .Configure(app => app.Run(async context =>
                {
                    try
                    {
                        var request = context.Request;
                        var response = context.Response;

                        if (Options.Debug && request.Path.Value == "/debug")
                        {
                            var buffer = Encoding.UTF8.GetBytes("debug");
                            await response.Body.WriteAsync(buffer, 0, buffer.Length);
                            return;
                        }

                        var key = new RequestCacheKey(request, allHeaders);

                        if (_cache.TryGetValue(key, out var upstreamResponse))
                        {
                            if (Options.Dots)
                            {
                                Console.Write(".");
                            }

                            if (Options.CacheLast)
                            {
                                _lastUpstreamResponse = upstreamResponse;
                            }
                            await Proxy.SendDownstreamResponse(request, upstreamResponse, response, cached: true);
                        }
                        else if (Options.CacheLast && request.Path.Value == "/last")
                        {
                            if (Options.Dots)
                            {
                                Console.Write("@");
                            }

                            // Used for perf testing the cache lookup and downstream response generation.  This allows a perf client like
                            // "wrk" to directly request the last response without using the server as an HTTP proxy, since "wrk" is much
                            // slower when using a proxy (50k vs 6k RPS).
                            await Proxy.SendDownstreamResponse(request, _lastUpstreamResponse, response, cached: true);
                        }
                        else
                        {
                            if (Options.Dots)
                            {
                                Console.Write("*");
                            }

                            upstreamResponse = await Proxy.SendUpstreamRequest(request);
                            await Proxy.SendDownstreamResponse(request, upstreamResponse, response, cached: false);
                            _cache.AddOrUpdate(key, upstreamResponse, (k, r) => upstreamResponse);

                            if (Options.CacheLast)
                            {
                                _lastUpstreamResponse = upstreamResponse;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e);
                    }
                }))
                .Build()
                .Run();
        }
    }
}
