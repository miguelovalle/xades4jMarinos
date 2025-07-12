# Script para probar el endpoint con el certificado real del usuario
Write-Host "Probando endpoint con certificado real..." -ForegroundColor Green

# XML de prueba
$xml = '<?xml version="1.0" encoding="UTF-8"?><FacturaElectronica xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:ID>SETP990000001</cbc:ID><cbc:IssueDate>2024-01-15</cbc:IssueDate></FacturaElectronica>'

# Crear el cuerpo del request
$body = @{
    xmlContent = $xml
    certificatePath = "src/main/resources/keystore/Certifica.p12"
    certificatePassword = "9Ep3KxPRph"
} | ConvertTo-Json

Write-Host "Enviando request al endpoint..." -ForegroundColor Yellow
Write-Host "Certificado: Certifica.p12" -ForegroundColor Cyan
Write-Host "Alias: marino s bar pescadero restaurante sas" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/firma/xades-epes" -Method POST -Body $body -ContentType "application/json"
    
    Write-Host "✅ Firma exitosa!" -ForegroundColor Green
    Write-Host "📄 XML firmado recibido correctamente" -ForegroundColor Green
    
    # Guardar el resultado en un archivo
    $response | Out-File -FilePath "xml-firmado-resultado.xml" -Encoding UTF8
    Write-Host "💾 Resultado guardado en: xml-firmado-resultado.xml" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Error en la firma:" -ForegroundColor Red
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