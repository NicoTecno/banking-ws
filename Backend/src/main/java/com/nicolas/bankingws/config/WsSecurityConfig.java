package com.nicolas.bankingws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;

import java.util.Properties;

/**
 * Esto es lo que SOAP tiene "de fábrica" y REST no: seguridad a nivel de
 * mensaje, no de transporte. La credencial viaja DENTRO del <soap:Header>
 * del XML (un <wsse:UsernameToken>), no en un header HTTP como Authorization.
 * Eso significa que sobrevive aunque el mensaje pase por intermediarios que
 * no son HTTPS de punta a punta (un proxy, una cola de mensajes) — la
 * garantía va pegada al mensaje, no al canal que lo transporta.
 *
 * Para que se pueda probar a mano en Postman sin herramientas de WS-Security
 * aparte, esto usa PasswordText (la contraseña viaja en texto plano dentro
 * del XML). Un sistema real usaría PasswordDigest — un hash con nonce y
 * timestamp que Postman no puede armar a mano — combinado con HTTPS
 * obligatorio en el transporte.
 */
@Configuration
public class WsSecurityConfig {

    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidationActions("UsernameToken");
        interceptor.setValidationCallbackHandler(callbackHandler());
        return interceptor;
    }

    @Bean
    public SimplePasswordValidationCallbackHandler callbackHandler() {
        SimplePasswordValidationCallbackHandler handler = new SimplePasswordValidationCallbackHandler();
        Properties users = new Properties();
        users.setProperty("nicolas", "banking123");
        handler.setUsers(users);
        return handler;
    }
}
