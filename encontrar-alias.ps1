# Script para encontrar el alias de un certificado .p12
# Uso: .\encontrar-alias.ps1 "ruta\a\tu\certifica-p12" "tu_contraseña"

param(
    [Parameter(Mandatory=$true)]
    [string]$KeystorePath,
    
    [Parameter(Mandatory=$true)]
    [string]$Password
)

Write-Host "Buscando alias en el certificado: $KeystorePath" -ForegroundColor Green

try {
    # Comando para listar los alias del certificado
    $command = "keytool -list -keystore `"$KeystorePath`" -storepass `"$Password`" -storetype PKCS12"
    
    Write-Host "Ejecutando: $command" -ForegroundColor Yellow
    Write-Host ""
    
    # Ejecutar el comando
    Invoke-Expression $command
    
    Write-Host ""
    Write-Host "✅ Comando ejecutado exitosamente" -ForegroundColor Green
    Write-Host "📝 Copia el alias que aparece arriba y úsalo en la configuración" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Error al ejecutar el comando: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Asegúrate de:" -ForegroundColor Yellow
    Write-Host "   - La ruta del archivo es correcta" -ForegroundColor Yellow
    Write-Host "   - La contraseña es correcta" -ForegroundColor Yellow
    Write-Host "   - El archivo existe y es válido" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "📋 Ejemplo de uso:" -ForegroundColor Cyan
Write-Host "   .\encontrar-alias.ps1 `"C:\ruta\a\tu\certifica-p12`" `"tu_contraseña`"" 