using Azure.Test.PerfStress;
using CommandLine;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public class CountOptions : PerfStressOptions
    {
        [Option('c', "count", Default = 500, HelpText = "Number of blobs")]
        public int Count { get; set; }
    }
}
