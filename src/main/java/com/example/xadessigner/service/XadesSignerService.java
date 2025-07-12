package com.example.xadessigner.service;

import com.example.xadessigner.config.XadesConfigProperties;
import com.example.xadessigner.dto.SignatureRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import xades4j.production.XadesSigner;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.SignedDataObjects;
import xades4j.production.DataObjectReference;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SigningKeyException;
import xades4j.verification.UnexpectedJCAException;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import xades4j.properties.DataObjectDesc;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Service
public class XadesSignerService {

    private final XadesConfigProperties configProperties;

    @Autowired
    public XadesSignerService(XadesConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public String signXml(SignatureRequest request) throws Exception {
        System.out.println("Debug - Iniciando proceso de firma...");
        
        // Determinar si usar certificado del cliente, certificado especificado o keystore por defecto
        if (request.getCertificateBase64() != null && request.getPrivateKeyBase64() != null) {
            System.out.println("Debug - Usando certificado y clave privada del cliente");
            return signWithClientCertificate(request);
        } else if (request.getCertificatePath() != null && !request.getCertificatePath().isEmpty()) {
            System.out.println("Debug - Usando certificado especificado: " + request.getCertificatePath());
            return signWithSpecifiedCertificate(request);
        } else {
            System.out.println("Debug - Usando keystore por defecto");
            return signWithDefaultKeystore(request);
        }
    }
    
    private String signWithClientCertificate(SignatureRequest request) throws Exception {
        System.out.println("Debug - Procesando certificado del cliente...");
        
        // Decodificar certificado y clave privada
        byte[] certBytes = Base64.getDecoder().decode(request.getCertificateBase64());
        byte[] keyBytes = Base64.getDecoder().decode(request.getPrivateKeyBase64());
        
        // Crear certificado X509
        java.security.cert.CertificateFactory certFactory = java.security.cert.CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        
        // Crear clave privada (asumiendo formato PKCS8)
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        System.out.println("Debug - Certificado y clave privada cargados exitosamente");
        
        // Crear proveedor de claves con certificado del cliente
        KeyingDataProvider kskp = new KeyingDataProvider() {
            @Override
            public PrivateKey getSigningKey(X509Certificate cert) throws SigningKeyException, UnexpectedJCAException {
                System.out.println("Debug - Obteniendo clave de firma del cliente...");
                return privateKey;
            }
            
            @Override
            public List<X509Certificate> getSigningCertificateChain() {
                System.out.println("Debug - Obteniendo cadena de certificados del cliente...");
                List<X509Certificate> certChain = new ArrayList<>();
                certChain.add(certificate);
                System.out.println("Debug - Cadena de certificados del cliente obtenida: " + certChain.size() + " certificados");
                return certChain;
            }
        };
        
        return signDocument(request, kskp);
    }
    
    private String signWithSpecifiedCertificate(SignatureRequest request) throws Exception {
        System.out.println("Debug - Usando certificado especificado: " + request.getCertificatePath());
        
        // Obtener configuración del certificado especificado
        String keystorePath = request.getCertificatePath();
        final char[] keystorePassword = request.getCertificatePassword().toCharArray();
        final String keyAlias = "marino s bar pescadero restaurante sas"; // Alias del certificado real
        
        System.out.println("Debug - Keystore path: " + keystorePath);
        System.out.println("Debug - Key alias: " + keyAlias);
        System.out.println("Debug - Keystore password length: " + keystorePassword.length);
        
        // Cargar el keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keystoreStream = new FileInputStream(keystorePath)) {
            System.out.println("Debug - Cargando keystore...");
            keyStore.load(keystoreStream, keystorePassword);
            System.out.println("Debug - Keystore cargado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al cargar keystore: " + e.getMessage());
            throw e;
        }
        
        // Obtener la entrada de la clave privada
        KeyStore.PrivateKeyEntry privateKeyEntry;
        try {
            System.out.println("Debug - Obteniendo entrada de clave privada...");
            privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    keyAlias, 
                    new KeyStore.PasswordProtection(keystorePassword)
            );
            System.out.println("Debug - Entrada de clave privada obtenida exitosamente");
        } catch (Exception e) {
            System.err.println("Error al obtener clave privada: " + e.getMessage());
            throw e;
        }
        
        // Crear un proveedor de claves personalizado
        System.out.println("Debug - Creando proveedor de claves...");
        KeyingDataProvider kskp = new KeyingDataProvider() {
            @Override
            public PrivateKey getSigningKey(X509Certificate cert) throws SigningKeyException, UnexpectedJCAException {
                try {
                    System.out.println("Debug - Obteniendo clave de firma...");
                    return privateKeyEntry.getPrivateKey();
                } catch (Exception e) {
                    System.err.println("Error al obtener clave de firma: " + e.getMessage());
                    throw new SigningKeyException("Error al obtener la clave de firma", e);
                }
            }
            
            @Override
            public List<X509Certificate> getSigningCertificateChain() {
                try {
                    System.out.println("Debug - Obteniendo cadena de certificados...");
                    Certificate[] chain = privateKeyEntry.getCertificateChain();
                    List<X509Certificate> certChain = new ArrayList<>();
                    for (Certificate cert : chain) {
                        if (cert instanceof X509Certificate) {
                            certChain.add((X509Certificate) cert);
                        }
                    }
                    System.out.println("Debug - Cadena de certificados obtenida: " + certChain.size() + " certificados");
                    return certChain;
                } catch (Exception e) {
                    System.err.println("Error al obtener cadena de certificados: " + e.getMessage());
                    throw new RuntimeException("Error al obtener la cadena de certificados", e);
                }
            }
        };
        
        return signDocument(request, kskp);
    }
    
    private String signWithDefaultKeystore(SignatureRequest request) throws Exception {
        System.out.println("Debug - Usando keystore por defecto...");
        
        // Obtener configuración del keystore
        String keystorePath = configProperties.getKeystore().getPath();
        final char[] keystorePassword = (request.getCertificatePassword() != null ? 
                request.getCertificatePassword() : 
                configProperties.getKeystore().getPassword()).toCharArray();
        final char[] keyPassword = (configProperties.getKeystore().getKeyPassword() != null ? 
                configProperties.getKeystore().getKeyPassword() : 
                new String(keystorePassword)).toCharArray();
        final String keyAlias = configProperties.getKeystore().getKeyAlias();
        
        System.out.println("Debug - Keystore path: " + keystorePath);
        System.out.println("Debug - Key alias: " + keyAlias);
        
        // Cargar el keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keystoreStream = keystorePath.startsWith("classpath:") ?
                new ClassPathResource(keystorePath.replace("classpath:", "")).getInputStream() :
                new FileInputStream(keystorePath)) {
            System.out.println("Debug - Cargando keystore...");
            keyStore.load(keystoreStream, keystorePassword);
            System.out.println("Debug - Keystore cargado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al cargar keystore: " + e.getMessage());
            throw e;
        }
        
        // Obtener la entrada de la clave privada
        KeyStore.PrivateKeyEntry privateKeyEntry;
        try {
            System.out.println("Debug - Obteniendo entrada de clave privada...");
            privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    keyAlias, 
                    new KeyStore.PasswordProtection(keyPassword)
            );
            System.out.println("Debug - Entrada de clave privada obtenida exitosamente");
        } catch (Exception e) {
            System.err.println("Error al obtener clave privada: " + e.getMessage());
            throw e;
        }
        
        // Crear un proveedor de claves personalizado
        System.out.println("Debug - Creando proveedor de claves...");
        KeyingDataProvider kskp = new KeyingDataProvider() {
            @Override
            public PrivateKey getSigningKey(X509Certificate cert) throws SigningKeyException, UnexpectedJCAException {
                try {
                    System.out.println("Debug - Obteniendo clave de firma...");
                    return privateKeyEntry.getPrivateKey();
                } catch (Exception e) {
                    System.err.println("Error al obtener clave de firma: " + e.getMessage());
                    throw new SigningKeyException("Error al obtener la clave de firma", e);
                }
            }
            
            @Override
            public List<X509Certificate> getSigningCertificateChain() {
                try {
                    System.out.println("Debug - Obteniendo cadena de certificados...");
                    Certificate[] chain = privateKeyEntry.getCertificateChain();
                    List<X509Certificate> certChain = new ArrayList<>();
                    for (Certificate cert : chain) {
                        if (cert instanceof X509Certificate) {
                            certChain.add((X509Certificate) cert);
                        }
                    }
                    System.out.println("Debug - Cadena de certificados obtenida: " + certChain.size() + " certificados");
                    return certChain;
                } catch (Exception e) {
                    System.err.println("Error al obtener cadena de certificados: " + e.getMessage());
                    throw new RuntimeException("Error al obtener la cadena de certificados", e);
                }
            }
        };
        
        return signDocument(request, kskp);
    }
    
    private String signDocument(SignatureRequest request, KeyingDataProvider kskp) throws Exception {
        // Crear el perfil de firma básico
        System.out.println("Debug - Creando perfil de firma...");
        XadesBesSigningProfile profile;
        XadesSigner signer;
        try {
            profile = new XadesBesSigningProfile(kskp);
            System.out.println("Debug - Perfil de firma creado exitosamente");
            
            // Crear el firmador
            System.out.println("Debug - Creando firmador...");
            signer = profile.newSigner();
            System.out.println("Debug - Firmador creado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al crear perfil o firmador: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        try (InputStream is = new ByteArrayInputStream(request.getXmlContent().getBytes(StandardCharsets.UTF_8))) {
            System.out.println("Debug - Procesando documento XML...");
            
            // Cargar el documento XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            
            // Asegurar que el elemento raíz tenga un ID para la firma
            String elementId = doc.getDocumentElement().getAttribute("Id");
            if (elementId == null || elementId.isEmpty()) {
                elementId = "signed-content";
                doc.getDocumentElement().setAttribute("Id", elementId);
            }
            
            // Registrar el atributo ID como tipo ID en el DOM
            doc.getDocumentElement().setIdAttribute("Id", true);
            
            System.out.println("Debug - Elemento raíz ID: " + elementId);
            
            // Crear objeto de datos firmados
            SignedDataObjects signedDataObjects = new SignedDataObjects();
            
            // Crear una referencia al objeto de datos a firmar
            String referenceId = "#" + elementId;
            DataObjectReference dataObjRef = new DataObjectReference(referenceId);
            
            // Agregar la referencia a los objetos firmados
            signedDataObjects.withSignedDataObject(dataObjRef);
            
            System.out.println("Debug - Firmando documento con referencia: " + referenceId);
            
            // Firmar el documento - usar el elemento raíz como nodo padre
            signer.sign(signedDataObjects, doc.getDocumentElement());
            System.out.println("Debug - Documento firmado exitosamente");
            
            // Convertir el documento firmado a String
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
                javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(baos);
                transformer.transform(source, result);
                String resultXml = baos.toString("UTF-8");
                System.out.println("Debug - Transformación completada. Tamaño del resultado: " + resultXml.length());
                return resultXml;
            }
        } catch (Exception e) {
            System.err.println("Error durante el proceso de firma: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Carga la clave privada desde el keystore.
     * @deprecated Este método ya no es necesario ya que se usa KeyStoreKeyingDataProvider directamente
     */
    @Deprecated
    private KeyStore.PrivateKeyEntry loadPrivateKey(
            String keystorePath,
            char[] keystorePassword,
            String keyAlias,
            char[] keyPassword) throws KeyStoreException, NoSuchAlgorithmException, 
            CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, UnrecoverableEntryException {
        
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keystoreStream = keystorePath.startsWith("classpath:") ?
                new ClassPathResource(keystorePath.replace("classpath:", "")).getInputStream() :
                new FileInputStream(keystorePath)) {
            
            keyStore.load(keystoreStream, keystorePassword);
            
            // Buscar el alias de la clave si no se especifica
            if (keyAlias == null || keyAlias.isEmpty()) {
                Enumeration<String> aliases = keyStore.aliases();
                if (aliases.hasMoreElements()) {
                    keyAlias = aliases.nextElement();
                } else {
                    throw new KeyStoreException("No se encontraron entradas en el keystore");
                }
            }
            
            // Obtener la entrada de la clave privada
            KeyStore.ProtectionParameter keyPasswordParam = new KeyStore.PasswordProtection(keyPassword);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
                    keyStore.getEntry(keyAlias, keyPasswordParam);
                    
            if (privateKeyEntry == null) {
                throw new KeyStoreException("No se encontró la clave privada con el alias: " + keyAlias);
            }
            
            return privateKeyEntry;
        }
    }
}
