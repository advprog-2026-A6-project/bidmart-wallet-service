param(
    [string]$JMeterHome = "C:\Users\Asus\Documents\apache-jmeter-5.6.3\apache-jmeter-5.6.3",
    [string]$JavaHome = "C:\Program Files\Java\jdk-21",
    [string]$Plan = "profiling\wallet-jmeter-profiling.jmx"
)

$ErrorActionPreference = "Stop"

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultFile = "profiling\wallet-profile-results-$timestamp.jtl"
$reportDir = "profiling\wallet-profile-report-$timestamp"
$jmeter = Join-Path $JMeterHome "bin\jmeter.bat"

if (-not (Test-Path $jmeter)) {
    throw "JMeter not found at $jmeter"
}

if (-not (Test-Path $JavaHome)) {
    throw "JDK not found at $JavaHome"
}

$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$env:Path"

& $jmeter -n -t $Plan -l $resultFile -e -o $reportDir

Write-Host ""
Write-Host "JMeter result: $resultFile"
Write-Host "HTML report:   $reportDir\index.html"
