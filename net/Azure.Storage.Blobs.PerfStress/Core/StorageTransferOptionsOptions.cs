using CommandLine;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public class StorageTransferOptionsOptions : SizeOptions
    {
        private int? _maximumTransferLength;
        private int? _MaximumConcurrency;

        [Option('l', "maximumTransferLength")]
        public int? MaximumTransferLength
        {
            get => _maximumTransferLength;
            set
            {
                _maximumTransferLength = value;
                UpdateStorageTransferOptions();
            }
        }

        [Option('t', "MaximumConcurrency")]
        public int? MaximumConcurrency
        {
            get => _MaximumConcurrency;
            set
            {
                _MaximumConcurrency = value;
                UpdateStorageTransferOptions();
            }
        }

        public StorageTransferOptions StorageTransferOptions { get; private set; } = new StorageTransferOptions();

        private void UpdateStorageTransferOptions()
        {
            StorageTransferOptions = new StorageTransferOptions()
            {
                MaximumConcurrency = MaximumConcurrency,
                MaximumTransferLength = MaximumTransferLength
            };
        }
    }
}
