using CommandLine;

namespace Azure.Test.PerfStress
{
    public class PerfStressOptions
    {
        [Option('d', "duration", Default = 10, HelpText = "Duration of test in seconds")]
        public int Duration { get; set; }

        [Option("no-cleanup", HelpText = "Disables test cleanup")]
        public bool NoCleanup { get; set; }

        [Option('p', "parallel", Default = 1, HelpText = "Number of operations to execute in parallel")]
        public int Parallel { get; set; }

        [Option("sync", HelpText = "Runs sync version of test")]
        public bool Sync { get; set; }
    }
}
