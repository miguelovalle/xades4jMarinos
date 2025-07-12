package com.example.xadessigner.service;

import com.example.xadessigner.XadesSignerApplication;
import com.example.xadessigner.config.XadesConfigProperties;
import com.example.xadessigner.dto.SignatureRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = XadesSignerApplication.class)
@ActiveProfiles("dev")
class XadesSignerServiceTest {

    @Autowired
    private XadesSignerService xadesSignerService;

    @Autowired
    private XadesConfigProperties configProperties;

    private String testXmlContent;
    private SignatureRequest signatureRequest;

    @BeforeEach
    void setUp() throws IOException {
        // Cargar el XML de prueba
        ClassPathResource resource = new ClassPathResource("test-document.xml");
        testXmlContent = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        // Configurar la solicitud de firma (usando certificado por defecto)
        signatureRequest = new SignatureRequest();
        signatureRequest.setXmlContent(testXmlContent);
        // No establecer certificatePath ni certificatePassword para usar el certificado por defecto
    }

    @Test
    void testSignXml() throws Exception {
        // Ejecutar la firma
        String signedXml = xadesSignerService.signXml(signatureRequest);
        
        // Verificar que el resultado no es nulo ni vacío
        assertNotNull(signedXml, "El XML firmado no debe ser nulo");
        assertFalse(signedXml.isEmpty(), "El XML firmado no debe estar vacío");
        
        // Verificar que el XML firmado contiene elementos de firma XAdES
        assertTrue(signedXml.contains("Signature"), "El XML firmado debe contener un elemento Signature");
        assertTrue(signedXml.contains("SignedInfo"), "El XML firmado debe contener un elemento SignedInfo");
        assertTrue(signedXml.contains("SignatureValue"), "El XML firmado debe contener un elemento SignatureValue");
        assertTrue(signedXml.contains("KeyInfo"), "El XML firmado debe contener un elemento KeyInfo");
        
        // Verificar que el contenido original está presente
        assertTrue(signedXml.contains("Documento de Prueba"), "El XML firmado debe contener el contenido original");
    }

    @Test
    void testSignXmlWithInvalidCertificate() {
        // Configurar una solicitud con un certificado inválido
        SignatureRequest invalidRequest = new SignatureRequest();
        invalidRequest.setXmlContent(testXmlContent);
        invalidRequest.setCertificatePath("src/main/resources/keystore/nonexistent.p12");
        invalidRequest.setCertificatePassword("wrongpassword");
        
        // Verificar que se lanza una excepción
        assertThrows(Exception.class, () -> {
            xadesSignerService.signXml(invalidRequest);
        }, "Debe lanzar una excepción con un certificado inválido");
    }
}
