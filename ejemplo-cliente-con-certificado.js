// Ejemplo de cliente JavaScript para enviar certificado y clave privada
// Este ejemplo muestra cómo usar el endpoint con tu propio certificado

// Función para convertir certificado y clave privada a Base64
function convertCertificateToBase64(certificatePem) {
  // Remover headers y footers del certificado PEM
  const certContent = certificatePem
    .replace(/-----BEGIN CERTIFICATE-----/, '')
    .replace(/-----END CERTIFICATE-----/, '')
    .replace(/\s/g, ''); // Remover espacios y saltos de línea

  return certContent;
}

function convertPrivateKeyToBase64(privateKeyPem) {
  // Remover headers y footers de la clave privada PEM
  const keyContent = privateKeyPem
    .replace(/-----BEGIN PRIVATE KEY-----/, '')
    .replace(/-----END PRIVATE KEY-----/, '')
    .replace(/\s/g, ''); // Remover espacios y saltos de línea

  return keyContent;
}

// Ejemplo de uso con Axios
async function signXmlWithClientCertificate(
  xmlContent,
  certificatePem,
  privateKeyPem
) {
  try {
    // Convertir certificado y clave privada a Base64
    const certificateBase64 = convertCertificateToBase64(certificatePem);
    const privateKeyBase64 = convertPrivateKeyToBase64(privateKeyPem);

    // Crear el objeto de request
    const requestData = {
      xmlContent: xmlContent,
      certificateBase64: certificateBase64,
      privateKeyBase64: privateKeyBase64,
      // Los campos del keystore por defecto ya no son necesarios
      certificatePath: '', // Opcional cuando usas certificado del cliente
      certificatePassword: '', // Opcional cuando usas certificado del cliente
    };

    console.log('Enviando request con certificado del cliente...');

    // Realizar la petición
    const response = await axios.post(
      'http://localhost:8080/api/firma/xades-epes',
      requestData,
      {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
        },
      }
    );

    console.log('XML firmado exitosamente:', response.data);
    return response.data;
  } catch (error) {
    console.error(
      'Error al firmar XML:',
      error.response?.data || error.message
    );
    throw error;
  }
}

// Ejemplo de uso con Fetch API
async function signXmlWithFetch(xmlContent, certificatePem, privateKeyPem) {
  try {
    // Convertir certificado y clave privada a Base64
    const certificateBase64 = convertCertificateToBase64(certificatePem);
    const privateKeyBase64 = convertPrivateKeyToBase64(privateKeyPem);

    // Crear el objeto de request
    const requestData = {
      xmlContent: xmlContent,
      certificateBase64: certificateBase64,
      privateKeyBase64: privateKeyBase64,
    };

    console.log('Enviando request con certificado del cliente...');

    // Realizar la petición
    const response = await fetch('http://localhost:8080/api/firma/xades-epes', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json; charset=UTF-8',
      },
      body: JSON.stringify(requestData),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const signedXml = await response.text();
    console.log('XML firmado exitosamente:', signedXml);
    return signedXml;
  } catch (error) {
    console.error('Error al firmar XML:', error.message);
    throw error;
  }
}

// Ejemplo de certificado y clave privada (reemplaza con los tuyos)
const exampleCertificatePem = `-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJAKoK/OvHhH7PMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV
BAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX
aWRnaXRzIFB0eSBMdGQwHhcNMTcwMzE2MTQzNzU5WhcNMTgwMzE2MTQzNzU5WjBF
MQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50
ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB
CgKCAQEA...
-----END CERTIFICATE-----`;

const examplePrivateKeyPem = `-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6Cr8q8eEfs8wD
QYZKqZIdvQEEJwEBAQEFAAOCAQ8AMIIBCgKCAQEA...
-----END PRIVATE KEY-----`;

// Ejemplo de XML a firmar
const exampleXml = `<?xml version="1.0" encoding="UTF-8"?>
<FacturaElectronica xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" 
                     xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" 
                     xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
    <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
    <cbc:ID>SETP990000001</cbc:ID>
    <cbc:IssueDate>2024-01-15</cbc:IssueDate>
</FacturaElectronica>`;

// Función para probar el endpoint
async function testClientCertificate() {
  try {
    console.log('Probando firma con certificado del cliente...');

    // Usar Axios
    const signedXml = await signXmlWithClientCertificate(
      exampleXml,
      exampleCertificatePem,
      examplePrivateKeyPem
    );

    console.log('✅ Firma exitosa con certificado del cliente');
    return signedXml;
  } catch (error) {
    console.error('❌ Error en la firma:', error);
    throw error;
  }
}

// Exportar funciones para uso en otros módulos
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    signXmlWithClientCertificate,
    signXmlWithFetch,
    convertCertificateToBase64,
    convertPrivateKeyToBase64,
    testClientCertificate,
  };
}
