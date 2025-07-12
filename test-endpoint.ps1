# Script de prueba para el endpoint de firma XAdES-EPES

# XML de prueba con namespaces correctos
$xml = '<?xml version="1.0" encoding="UTF-8"?><FacturaElectronica xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:ID>SETP990000001</cbc:ID><cbc:IssueDate>2024-01-15</cbc:IssueDate></FacturaElectronica>'

# Crear el cuerpo del request
$body = @{
    xmlContent = $xml
    certificatePath = "src/main/resources/keystore/keystore-dev.p12"
    certificatePassword = "changeit"
}

# Convertir a JSON
$jsonBody = $body | ConvertTo-Json -Depth 10

Write-Host "JSON a enviar:"
Write-Host $jsonBody
Write-Host ""

# Realizar la petición
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/firma/xades-epes" -Method POST -Body $jsonBody -ContentType "application/json"
    Write-Host "Respuesta exitosa:"
    Write-Host $response
} catch {
    Write-Host "Error en la petición:"
    Write-Host $_.Exception.Message
    Write-Host "Detalles del error:"
    Write-Host $_.Exception.Response
} 