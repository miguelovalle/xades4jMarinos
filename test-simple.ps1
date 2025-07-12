# Script de prueba simple
Write-Host "=== PRUEBA SIMPLE ===" -ForegroundColor Green

# Crear el cuerpo del request
$body = @{
    xmlContent = "<?xml version='1.0' encoding='UTF-8'?><test>test</test>"
    certificatePath = "src/main/resources/keystore/Certifica.p12"
    certificatePassword = "9Ep3KxPRph"
} | ConvertTo-Json

Write-Host "Body a enviar:" -ForegroundColor Yellow
Write-Host $body -ForegroundColor Cyan

try {
    Write-Host "Enviando request..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/firma/xades-epes" -Method POST -Body $body -ContentType "application/json"
    
    Write-Host "✅ ÉXITO!" -ForegroundColor Green
    Write-Host "Respuesta recibida" -ForegroundColor Green
    
} catch {
    Write-Host "❌ ERROR:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Detalles del error:" -ForegroundColor Red
        Write-Host $responseBody -ForegroundColor Red
    }
} 