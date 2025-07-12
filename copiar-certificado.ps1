# Script para copiar el certificado al proyecto
# Uso: .\copiar-certificado.ps1 "ruta\a\tu\certifica-p12"

param(
    [Parameter(Mandatory=$true)]
    [string]$CertificadoPath
)

Write-Host "Copiando certificado al proyecto..." -ForegroundColor Green

# Verificar que el archivo existe
if (-not (Test-Path $CertificadoPath)) {
    Write-Host "❌ Error: El archivo no existe en la ruta: $CertificadoPath" -ForegroundColor Red
    exit 1
}

# Crear el directorio de keystore si no existe
$keystoreDir = "src\main\resources\keystore"
if (-not (Test-Path $keystoreDir)) {
    New-Item -ItemType Directory -Path $keystoreDir -Force
    Write-Host "📁 Directorio creado: $keystoreDir" -ForegroundColor Yellow
}

# Copiar el certificado
$destino = "$keystoreDir\certifica-p12"
try {
    Copy-Item $CertificadoPath $destino -Force
    Write-Host "✅ Certificado copiado exitosamente a: $destino" -ForegroundColor Green
    
    # Verificar que se copió correctamente
    if (Test-Path $destino) {
        $size = (Get-Item $destino).Length
        Write-Host "📊 Tamaño del archivo: $size bytes" -ForegroundColor Cyan
    }
    
} catch {
    Write-Host "❌ Error al copiar el archivo: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 Certificado copiado correctamente!" -ForegroundColor Green
Write-Host "📝 Ahora puedes usar el script 'encontrar-alias.ps1' para obtener el alias" -ForegroundColor Cyan
Write-Host "📋 Ejemplo: .\encontrar-alias.ps1 `"$destino`" `"tu_contraseña`"" -ForegroundColor Yellow 