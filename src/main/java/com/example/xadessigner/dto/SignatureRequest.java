package com.example.xadessigner.dto;

import jakarta.validation.constraints.NotBlank;

public class SignatureRequest {
    
    @NotBlank(message = "El contenido XML es obligatorio")
    private String xmlContent;
    
    // Campos opcionales para certificado específico
    private String certificatePath;
    
    private String certificatePassword;
    
    // Nuevos campos para certificado y clave privada del cliente
    private String certificateBase64; // Certificado en formato Base64
    private String privateKeyBase64; // Clave privada en formato Base64
    private String certificateAlias; // Alias del certificado (opcional)
    
    // Constructores
    public SignatureRequest() {}
    
    public SignatureRequest(String xmlContent, String certificatePath, String certificatePassword) {
        this.xmlContent = xmlContent;
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword;
    }
    
    // Getters y Setters existentes
    public String getXmlContent() {
        return xmlContent;
    }
    
    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }
    
    public String getCertificatePath() {
        return certificatePath;
    }
    
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
    
    public String getCertificatePassword() {
        return certificatePassword;
    }
    
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }
    
    // Nuevos getters y setters
    public String getCertificateBase64() {
        return certificateBase64;
    }
    
    public void setCertificateBase64(String certificateBase64) {
        this.certificateBase64 = certificateBase64;
    }
    
    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }
    
    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }
    
    public String getCertificateAlias() {
        return certificateAlias;
    }
    
    public void setCertificateAlias(String certificateAlias) {
        this.certificateAlias = certificateAlias;
    }
}
