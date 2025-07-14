package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthResourceMockTest {

    @Mock
    private JsonWebToken jwt;

    private AuthResource authResource;

    @BeforeEach
    void setUp() {
        authResource = new AuthResource();
        authResource.jwt = jwt;
        
        // Simula configurações através de reflexão para os testes
        try {
            var authServerUrlField = AuthResource.class.getDeclaredField("authServerUrl");
            authServerUrlField.setAccessible(true);
            authServerUrlField.set(authResource, "http://localhost:8080/realms/durval-crm");
            
            var clientIdField = AuthResource.class.getDeclaredField("clientId");
            clientIdField.setAccessible(true);
            clientIdField.set(authResource, "durvalcrm-app");
            
            var clientSecretField = AuthResource.class.getDeclaredField("clientSecret");
            clientSecretField.setAccessible(true);
            clientSecretField.set(authResource, "test-secret");
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao configurar testes", e);
        }
    }

    @Test
    void testGetLoginInfo_ShouldReturnCorrectConfiguration() {
        // Act
        Map<String, Object> response = authResource.getLoginInfo();

        // Assert
        assertNotNull(response);
        assertEquals("http://localhost:8080/realms/durval-crm", response.get("authServerUrl"));
        assertEquals("durvalcrm-app", response.get("clientId"));
        assertEquals("durval-crm", response.get("realm"));
        assertNotNull(response.get("loginUrl"));
        
        String loginUrl = (String) response.get("loginUrl");
        assertTrue(loginUrl.contains("protocol/openid-connect/auth"));
        assertTrue(loginUrl.contains("client_id=durvalcrm-app"));
        assertTrue(loginUrl.contains("response_type=code"));
    }

    @Test
    void testExtractRealmFromUrl_ShouldReturnCorrectRealm() {
        // Usa reflexão para testar método privado
        try {
            var method = AuthResource.class.getDeclaredMethod("extractRealmFromUrl", String.class);
            method.setAccessible(true);
            
            // Act
            String realm = (String) method.invoke(authResource, "http://localhost:8080/realms/test-realm");
            
            // Assert
            assertEquals("test-realm", realm);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao testar método privado", e);
        }
    }

    @Test
    void testBuildLoginUrl_ShouldContainRequiredParameters() {
        // Usa reflexão para testar método privado
        try {
            var method = AuthResource.class.getDeclaredMethod("buildLoginUrl");
            method.setAccessible(true);
            
            // Act
            String loginUrl = (String) method.invoke(authResource);
            
            // Assert
            assertNotNull(loginUrl);
            assertTrue(loginUrl.contains("protocol/openid-connect/auth"));
            assertTrue(loginUrl.contains("client_id=durvalcrm-app"));
            assertTrue(loginUrl.contains("response_type=code"));
            assertTrue(loginUrl.contains("scope=openid"));
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao testar método privado", e);
        }
    }

    // Nota: Testes para endpoints que fazem chamadas HTTP reais (como handleCallback)
    // devem ser implementados como testes de integração com Keycloak real rodando
    // ou usando bibliotecas como WireMock para simular as respostas HTTP
}