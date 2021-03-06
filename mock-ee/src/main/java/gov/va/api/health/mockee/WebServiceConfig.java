package gov.va.api.health.mockee;

import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  public static final String mockEeVersion = "/v0";

  @Value("${ee.header.username}")
  private String eeHeaderUsername;

  @Value("${ee.header.password}")
  private String eeHeaderPassword;

  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    interceptors.add(securityInterceptor());
  }

  /** Default Wsdl. */
  @Bean(name = "summaries")
  public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema eeSchema) {
    DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
    wsdl11Definition.setPortTypeName("SummaryPort");
    wsdl11Definition.setLocationUri(mockEeVersion + "/ws");
    wsdl11Definition.setTargetNamespace("http://jaxws.webservices.esr.med.va.gov/schemas");
    wsdl11Definition.setSchema(eeSchema);
    return wsdl11Definition;
  }

  /** Get XSD Schema. */
  @Bean
  public XsdSchema eeSchema() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/eeSummary.xsd"));
  }

  /** Set up Servlet. */
  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<MessageDispatcherServlet>(servlet, mockEeVersion + "/ws/*");
  }

  /** Validation for user/password. */
  @Bean
  public SimplePasswordValidationCallbackHandler securityCallbackHandler() {
    SimplePasswordValidationCallbackHandler callbackHandler =
        new SimplePasswordValidationCallbackHandler();
    Properties users = new Properties();
    users.setProperty(eeHeaderUsername, eeHeaderPassword);
    callbackHandler.setUsers(users);
    return callbackHandler;
  }

  /** Security Interceptor. */
  @Bean
  public Wss4jSecurityInterceptor securityInterceptor() {
    Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
    securityInterceptor.setValidationActions("UsernameToken");
    securityInterceptor.setValidationCallbackHandler(securityCallbackHandler());
    return securityInterceptor;
  }
}
