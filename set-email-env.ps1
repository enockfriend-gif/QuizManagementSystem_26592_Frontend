# Email Configuration Script for Windows PowerShell
# Run this script BEFORE starting Spring Boot

Write-Host "=== Setting Email Environment Variables ===" -ForegroundColor Cyan

# Set your Gmail credentials here
$email = Read-Host "Enter your Gmail address (e.g., your-email@gmail.com)"
$appPassword = Read-Host "Enter your Gmail App Password (16 characters, no spaces)" -AsSecureString
$appPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($appPassword)
)

# Set environment variables
$env:MAIL_USERNAME = $email
$env:MAIL_PASSWORD = $appPasswordPlain

Write-Host "`n=== Verification ===" -ForegroundColor Green
Write-Host "MAIL_USERNAME: $env:MAIL_USERNAME"
Write-Host "MAIL_PASSWORD: $($env:MAIL_PASSWORD.Length) characters (hidden)"

Write-Host "`n=== Next Steps ===" -ForegroundColor Yellow
Write-Host "1. Keep this terminal window open"
Write-Host "2. Run: mvn spring-boot:run"
Write-Host "3. Check logs for 'Email Configuration Check'"
Write-Host "`nPress any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

