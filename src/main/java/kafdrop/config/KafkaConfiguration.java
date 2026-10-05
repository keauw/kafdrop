package kafdrop.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


@Component
@ConfigurationProperties(prefix = "kafka")
@Data
public final class KafkaConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);

  private String brokerConnect;
  private String saslMechanism;
  private String securityProtocol;
  private String truststoreFile;
  private String propertiesFile;
  private String keystoreFile;
  private String jaasConfig;
  private String clientCallback;
  private String iamEnabled;
  private String saslEnabled;


  public void applyCommon(Properties properties) {
    properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokerConnect);

    if (securityProtocol.equals("SSL")) {
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
    }

    LOG.info("Is SASL enabled : {}", saslEnabled);
    if (Boolean.parseBoolean(saslEnabled)) {
      LOG.info("Setting sasl.jaas.config {}", jaasConfig);
      LOG.info("Setting security protocol to {}", securityProtocol);
      LOG.info("Setting sasl mechanism to {}", saslMechanism);
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
      properties.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
      properties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
    }

    LOG.info("Is iam enabled : {}", iamEnabled);
    if (Boolean.parseBoolean(iamEnabled)) {
      LOG.info("Setting sasl.jaas.config {} and sasl and callback callback properties {}", jaasConfig, clientCallback);
      LOG.info("Setting security protocol to {}", securityProtocol);
      LOG.info("Setting sasl mechanism to {}", saslMechanism);
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
      properties.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
      properties.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, clientCallback);
      properties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
    }

    LOG.info("Checking truststore file {}", truststoreFile);
    if (new File(truststoreFile).isFile()) {
      LOG.info("Assigning truststore location to {}", truststoreFile);
      properties.put("ssl.truststore.location", truststoreFile);
    }

    LOG.info("Checking keystore file {}", keystoreFile);
    if (new File(keystoreFile).isFile()) {
      LOG.info("Assigning keystore location to {}", keystoreFile);
      properties.put("ssl.keystore.location", keystoreFile);
    }

    LOG.info("Checking properties file {}", propertiesFile);
    Optional<AbstractResource> propertiesResource = StringUtils.isBlank(propertiesFile) ? Optional.empty() :
      Stream.of(new FileSystemResource(propertiesFile),
          new ClassPathResource(propertiesFile))
        .filter(Resource::isReadable)
        .findFirst();
    if (propertiesResource.isPresent()) {
      LOG.info("Loading properties from {}", propertiesFile);
      final var propertyOverrides = new Properties();
      try {
        propertyOverrides.load(propertiesResource.get().getInputStream());
      } catch (IOException e) {
        throw new KafkaConfigurationException(e);
      }
      properties.putAll(propertyOverrides);
    }
  }
}
