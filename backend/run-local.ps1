$envFile = Join-Path $PSScriptRoot ".env"

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#") -or -not $line.Contains("=")) {
            return
        }

        $parts = $line.Split("=", 2)
        [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
    }
}

Push-Location $PSScriptRoot
try {
    cmd /c ".\mvnw.cmd -DskipTests install"
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    cmd /c ".\mvnw.cmd -f api\pom.xml spring-boot:run"
} finally {
    Pop-Location
}
