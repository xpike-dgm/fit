param(
    [Parameter(Mandatory = $true)]
    [string]$Notes
)

$ErrorActionPreference = "Stop"
$repository = "xpike-dgm/fit"
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$gradleFile = Join-Path $repositoryRoot "app/build.gradle.kts"
$gradleText = [System.IO.File]::ReadAllText($gradleFile)
$versionMatch = [regex]::Match($gradleText, 'val appVersionName = "([0-9]+\.[0-9]+\.[0-9]+)"')

if (-not $versionMatch.Success) {
    throw "Could not read appVersionName from app/build.gradle.kts. Use MAJOR.MINOR.PATCH."
}

$version = $versionMatch.Groups[1].Value
$tag = "v$version"
$expectedMain = ((& git -C $repositoryRoot ls-remote origin refs/heads/main) -split '\s+')[0]
$currentCommit = (& git -C $repositoryRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or -not $expectedMain -or $expectedMain -ne $currentCommit) {
    throw "Push the current commit to origin/main before publishing a release."
}

$dirtyFiles = @(& git -C $repositoryRoot status --porcelain)
if ($dirtyFiles.Count -gt 0) {
    throw "Commit or discard all source changes before publishing a release."
}

Push-Location $repositoryRoot
$notesPath = [System.IO.Path]::GetTempFileName()
try {
    & gh auth status | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "GitHub CLI is not authenticated. Run 'gh auth login' first."
    }

    & .\gradlew.bat assembleRelease
    if ($LASTEXITCODE -ne 0) {
        throw "assembleRelease failed."
    }

    $sourceApk = Join-Path $repositoryRoot "app/build/outputs/apk/release/app-release.apk"
    if (-not (Test-Path -LiteralPath $sourceApk)) {
        throw "Release APK was not produced at $sourceApk."
    }

    $distributionDirectory = Join-Path $repositoryRoot "build/distribution"
    New-Item -ItemType Directory -Force -Path $distributionDirectory | Out-Null
    $releaseApk = Join-Path $distributionDirectory "fit-release.apk"
    Copy-Item -LiteralPath $sourceApk -Destination $releaseApk -Force
    [System.IO.File]::WriteAllText($notesPath, $Notes, [System.Text.UTF8Encoding]::new($false))

    $releaseArguments = @(
        "release", "create", $tag, $releaseApk,
        "--repo", $repository,
        "--title", "Fit $tag",
        "--notes-file", $notesPath,
        "--target", "main"
    )
    & gh @releaseArguments
    if ($LASTEXITCODE -ne 0) {
        throw "GitHub release $tag could not be published."
    }
} finally {
    if (Test-Path -LiteralPath $notesPath) {
        Remove-Item -LiteralPath $notesPath -Force
    }
    Pop-Location
}
