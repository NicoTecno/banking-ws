package com.nicolas.bankingws.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;

/**
 * @EnableWs prende el equivalente SOAP de @EnableWebMvc. MessageDispatcherServlet
 * es el DispatcherServlet de este mundo: recibe el POST con el sobre SOAP,
 * decide qué @Endpoint le corresponde. El WSDL en sí no se escribe a mano —
 * DefaultWsdl11Definition lo arma automáticamente combinando el XSD
 * (el contrato de datos) con esta configuración (dónde vive el servicio).
 */
@EnableWs
@Configuration
public class WebServiceConfig implements WsConfigurer {

    private final Wss4jSecurityInterceptor securityInterceptor;

    public WebServiceConfig(Wss4jSecurityInterceptor securityInterceptor) {
        this.securityInterceptor = securityInterceptor;
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "banking")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema bankingSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("BankingPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://bankingws.nicolas.dev/schemas");
        wsdl11Definition.setSchema(bankingSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema bankingSchema() {
        return new SimpleXsdSchema(new ClassPathResource("banking.xsd"));
    }

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(securityInterceptor);
    }
}
