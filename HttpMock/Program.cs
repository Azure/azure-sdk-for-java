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
        // TODO: Store custom value instead of HttpResponseMessage?
        private static ConcurrentDictionary<RequestCacheKey, HttpResponseMessage> _cache =
            new ConcurrentDictionary<RequestCacheKey, HttpResponseMessage>();

        // Used for perf testing the cache lookup and downstream response generation.  This allows a perf client like
        // "wrk" to directly request the last response without using the server as an HTTP proxy, since "wrk" is much
        // slower when using a proxy (50k vs 6k RPS).
        private static HttpResponseMessage _lastUpstreamResponse;

        public class HttpMockOptions
        {
            [Option("cachelast")]
            public bool CacheLast { get; set; }
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
                    var request = context.Request;
                    var response = context.Response;

                    var key = new RequestCacheKey(request);

                    if (_cache.TryGetValue(key, out var upstreamResponse))
                    {
                        if (Options.CacheLast)
                        {
                            _lastUpstreamResponse = upstreamResponse;
                        }
                        await Proxy.SendDownstreamResponse(upstreamResponse, response);
                    }
                    else if (Options.CacheLast && request.QueryString.Value == "?last")
                    {
                        // Used for perf testing the cache lookup and downstream response generation.  This allows a perf client like
                        // "wrk" to directly request the last response without using the server as an HTTP proxy, since "wrk" is much
                        // slower when using a proxy (50k vs 6k RPS).
                        await Proxy.SendDownstreamResponse(_lastUpstreamResponse, response);
                    }
                    else
                    {
                        upstreamResponse = await Proxy.SendUpstreamRequest(request);
                        await Proxy.SendDownstreamResponse(upstreamResponse, response);
                        _cache.AddOrUpdate(key, upstreamResponse, (k, r) => upstreamResponse);

                        if (Options.CacheLast)
                        {
                            _lastUpstreamResponse = upstreamResponse;
                        }
                    }
                }))
                .Build()
                .Run();
        }
    }
}
