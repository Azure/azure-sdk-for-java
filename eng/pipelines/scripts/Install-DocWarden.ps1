param (
  [String]$DocWardenVersion,
  [String]$SourcesDirectory
)

Write-Host $SourcesDirectory/eng/.docsettings.yml

pip install setuptools wheel --quiet
pip install doc-warden==$DocWardenVersion --quiet
ward scan -d $SourcesDirectory -c $SourcesDirectory/eng/.docsettings.yml