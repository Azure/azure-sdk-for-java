param(
  [Parameter(Mandatory = $true)]
  $Paths,

  [Parameter(Mandatory = $true)]
  $Repositories
)

function SparseCheckout([Array]$paths, [Array]$Repositories)
{
    $paths = $paths -Join ' '

    foreach ($repo in $Repositories) {
        $dir = $repo.WorkingDirectory 
        if (!$dir) {
          $dir = "./$($repo.Name)"
        }
        New-Item $dir -ItemType Directory -Force
        Push-Location $dir

        if (Test-Path .git/info/sparse-checkout) {
          $hasInitialized = $true
          Write-Host "Repository $($repo.Name) has already been initialized. Skipping this step."
        } else {
          Write-Host "Repository $($repo.Name) is being initialized."
          git clone --no-checkout --filter=tree:0 git://github.com/$($repo.Name) .
          git sparse-checkout init
          git sparse-checkout set eng
        }

        Invoke-Expression -Command "git sparse-checkout add $paths"

        Write-Host "Set sparse checkout paths to:"
        Get-Content .git/info/sparse-checkout

        # sparse-checkout commands after initial checkout will auto-checkout again
        if (!$hasInitialized) {
          git checkout $($repo.Commitish)  # this will use the default branch if repo.Commitish is empty
        }

        Pop-Location
    }
}

function DeserializeParameter([Object]$parameter)
{
    if ($parameter -is [String]) {
        return $parameter | ConvertFrom-Json
    }
    return $parameter
}

$paths = DeserializeParameter $Paths
$repositories = DeserializeParameter $Repositories
SparseCheckout $paths $Repositories
