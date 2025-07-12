# Script de prueba final corregido
Write-Host "=== PRUEBA FINAL CON CERTIFICADO REAL ===" -ForegroundColor Green

# XML de prueba simple (escapado correctamente)
$xml = '<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>test</test>'

# Crear el JSON manualmente para evitar problemas de codificación
$jsonBody = @"
{
    "xmlContent": "$xml",
    "certificatePath": "src/main/resources/keystore/Certifica.p12",
    "certificatePassword": "9Ep3KxPRph"
}
"@

Write-Host "Body a enviar:" -ForegroundColor Yellow
Write-Host $jsonBody -ForegroundColor Cyan

try {
    Write-Host "Enviando request..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/firma/xades-epes" -Method POST -Body $jsonBody -ContentType "application/json"
    
    Write-Host "✅ ÉXITO!" -ForegroundColor Green
    Write-Host "XML firmado recibido correctamente" -ForegroundColor Green
    
    # Guardar el resultado en un archivo
    $response | Out-File -FilePath "xml-firmado-final.xml" -Encoding UTF8
    Write-Host "💾 Resultado guardado en: xml-firmado-final.xml" -ForegroundColor Cyan
    
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

Write-Host ""
Write-Host "🎉 Prueba completada!" -ForegroundColor Green 