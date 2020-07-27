@{
RootModule = 'MavenPackaging.psm1'
ModuleVersion = '1.0'
CompatiblePSEditions = @('Core')
GUID = 'ec795e7e-5074-4a49-acfa-8d8914168882'
Author = 'Azure SDK'

# Company or vendor of this module
CompanyName = 'Microsoft'
Copyright = '(c) mitch. All rights reserved.'
Description = 'Find and extract Maven meta-data by package.'
FunctionsToExport = @(
    'Get-SonaTypeProfileID',
    'Get-MavenPackageDetails',
    'Get-FilteredMavenPackageDetails'
    )
}

