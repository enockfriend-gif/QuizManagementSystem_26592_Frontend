# PowerShell script to set email environment variables and start the backend
# This ensures the backend picks up the email configuration

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setting Email Environment Variables" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Set email environment variables
$env:MAIL_USERNAME = "friendeno123@gmail.com"
$env:MAIL_PASSWORD = "wbhapqvejzoflhoe"

Write-Host "✓ MAIL_USERNAME set: $env:MAIL_USERNAME" -ForegroundColor Green
Write-Host "✓ MAIL_PASSWORD set: *** (16 characters)" -ForegroundColor Green
Write-Host ""

# Verify they're set
if ($env:MAIL_USERNAME -and $env:MAIL_PASSWORD) {
    Write-Host "Environment variables configured successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Starting backend server..." -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    # Start the backend
    mvn spring-boot:run
} else {
    Write-Host "ERROR: Failed to set environment variables!" -ForegroundColor Red
    exit 1
}

