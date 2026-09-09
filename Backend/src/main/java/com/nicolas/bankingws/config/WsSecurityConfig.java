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
 * El frontend usa PasswordDigest: la contraseña nunca viaja en texto plano.
 * El cliente calcula Base64(SHA-1(nonce + created + password)) y lo envía
 * junto con el nonce y el timestamp. El servidor (SimplePasswordValidationCallbackHandler)
 * obtiene el password en claro de su mapa, recalcula el digest y lo compara.
 * Si no coincide → SOAP Fault. Si un atacante intercepta el mensaje, solo
 * ve el hash — no puede recuperar la contraseña original.
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
        users.setProperty("julian", "banking456");
        users.setProperty("carla", "banking789");
        handler.setUsers(users);
        return handler;
    }
}
