package com.example.xadessigner.controller;

import com.example.xadessigner.dto.SignatureRequest;
import com.example.xadessigner.service.XadesSignerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.UnrecoverableEntryException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.io.IOException;

@RestController
@RequestMapping("/firma")
@Validated
public class SignatureController {
    
    private final XadesSignerService xadesSignerService;
    
    @Autowired
    public SignatureController(XadesSignerService xadesSignerService) {
        this.xadesSignerService = xadesSignerService;
    }
    
    @PostMapping("/xades-epes")
    public ResponseEntity<?> signXml(@Valid @RequestBody SignatureRequest request) {
        try {
            System.out.println("Debug - Iniciando firma XML...");
            System.out.println("Debug - XML Content length: " + (request.getXmlContent() != null ? request.getXmlContent().length() : "null"));
            System.out.println("Debug - Certificate Path: " + request.getCertificatePath());
            System.out.println("Debug - Certificate Password provided: " + (request.getCertificatePassword() != null ? "yes" : "no"));
            
            String signedXml = xadesSignerService.signXml(request);
            return ResponseEntity.ok(signedXml);
        } catch (UnrecoverableEntryException e) {
            System.err.println("Error de autenticación del keystore: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body("Error de autenticación: Contraseña del certificado incorrecta o alias no encontrado");
        } catch (KeyStoreException e) {
            System.err.println("Error del keystore: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body("Error del keystore: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body("Error de acceso al archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error general al firmar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body("Error al firmar el documento: " + e.getMessage());
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> testEndpoint(@RequestBody SignatureRequest request) {
        System.out.println("=== TEST ENDPOINT ===");
        System.out.println("XML Content: " + (request.getXmlContent() != null ? "NOT NULL" : "NULL"));
        System.out.println("Certificate Path: " + request.getCertificatePath());
        System.out.println("Certificate Password: " + (request.getCertificatePassword() != null ? "NOT NULL" : "NULL"));
        
        if (request.getXmlContent() != null) {
            System.out.println("XML Length: " + request.getXmlContent().length());
            System.out.println("XML Preview: " + request.getXmlContent().substring(0, Math.min(100, request.getXmlContent().length())));
        }
        
        return ResponseEntity.ok("Test endpoint working - JSON received correctly");
    }
}