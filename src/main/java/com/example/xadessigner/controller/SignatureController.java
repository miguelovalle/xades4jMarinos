package com.example.xadessigner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/firma")
public class SignatureController {
    
    @PostMapping("/xades-epes")
    public ResponseEntity<String> signXml(@RequestBody String xmlContent) {
        try {
            // Por ahora, solo devolvemos el XML recibido como prueba
            return ResponseEntity.ok(xmlContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }
}