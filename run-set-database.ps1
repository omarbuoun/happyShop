# PowerShell script to run SetDatabase and initialize the database with default products
# This will reset the database and add all 20 default products

$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"

# Compile first
Write-Host "Compiling project..." -ForegroundColor Yellow
.\mvnw.cmd compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Running SetDatabase to initialize database with default products..." -ForegroundColor Yellow
Write-Host "WARNING: This will DELETE all existing products and replace them with default products!" -ForegroundColor Red

# Build classpath
$classpath = "target/classes"
$dependencies = Get-ChildItem -Path "target/dependency" -Filter "*.jar" -ErrorAction SilentlyContinue
if ($dependencies) {
    $classpath += ";" + ($dependencies | ForEach-Object { $_.FullName } | Join-String -Separator ";")
}

# Add Derby to classpath if not in dependencies
$derbyPath = "$env:USERPROFILE\.m2\repository\org\apache\derby\derby\10.16.1.1\derby-10.16.1.1.jar"
if (Test-Path $derbyPath) {
    $classpath += ";$derbyPath"
}

# Run SetDatabase
java -cp $classpath ci553.happyshop.systemSetup.SetDatabase

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nDatabase initialized successfully!" -ForegroundColor Green
    Write-Host "You can now run the application to see all 20 default products." -ForegroundColor Green
} else {
    Write-Host "`nFailed to initialize database!" -ForegroundColor Red
    exit 1
}

