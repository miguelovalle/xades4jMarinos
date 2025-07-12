// Ejemplo de cliente JavaScript/Axios para el endpoint de firma XAdES-EPES
// Asegúrate de que el XML esté correctamente escapado para JSON

const axios = require('axios');

// XML de ejemplo (asegúrate de que esté correctamente escapado)
const xmlContent = `<?xml version="1.0" encoding="UTF-8"?>
<FacturaElectronica xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" 
                     xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" 
                     xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
    <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
    <cbc:ID>SETP990000001</cbc:ID>
    <cbc:IssueDate>2024-01-15</cbc:IssueDate>
</FacturaElectronica>`;

// Función para firmar XML
async function firmarXML() {
  try {
    const requestData = {
      xmlContent: xmlContent,
      certificatePath: 'src/main/resources/keystore/keystore-dev.p12',
      certificatePassword: 'changeit',
    };

    console.log('Enviando petición...');
    console.log('XML Content length:', xmlContent.length);

    const response = await axios.post(
      'http://localhost:8080/api/firma/xades-epes',
      requestData,
      {
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        timeout: 30000, // 30 segundos de timeout
      }
    );

    console.log('Respuesta exitosa:');
    console.log('Status:', response.status);
    console.log('XML firmado recibido (primeros 500 caracteres):');
    console.log(response.data.substring(0, 500));

    return response.data;
  } catch (error) {
    console.error('Error en la petición:');
    console.error('Status:', error.response?.status);
    console.error('Message:', error.message);
    console.error('Response data:', error.response?.data);
    throw error;
  }
}

// Función alternativa usando fetch (si no quieres usar axios)
async function firmarXMLConFetch() {
  try {
    const requestData = {
      xmlContent: xmlContent,
      certificatePath: 'src/main/resources/keystore/keystore-dev.p12',
      certificatePassword: 'changeit',
    };

    const response = await fetch('http://localhost:8080/api/firma/xades-epes', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(requestData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText}`);
    }

    const signedXml = await response.text();
    console.log('XML firmado recibido:');
    console.log(signedXml.substring(0, 500));

    return signedXml;
  } catch (error) {
    console.error('Error:', error.message);
    throw error;
  }
}

// Ejecutar la función
if (typeof module !== 'undefined' && module.exports) {
  // Node.js
  module.exports = { firmarXML, firmarXMLConFetch };
} else {
  // Browser
  window.firmarXML = firmarXML;
  window.firmarXMLConFetch = firmarXMLConFetch;
}

// Para ejecutar directamente en Node.js:
// firmarXML().catch(console.error);
