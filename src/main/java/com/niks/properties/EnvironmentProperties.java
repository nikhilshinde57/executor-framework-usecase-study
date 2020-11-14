package com.niks.properties;

import com.niks.constants.EnvironmentConstants;
import com.niks.constants.PropertiesConstant;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import com.niks.constants.ErrorConstants;
import com.niks.utils.EnvironmentVarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentProperties {

  private String currentProfile;
  private Properties properties;
  private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentProperties.class);

  @PostConstruct
  private void init() throws Exception {
    currentProfile = getProfileFromEnvironmentVar();
    if (currentProfile.equals(EnvironmentConstants.LOCAL_ENVIRONMENT)) {
      properties = getLocalEnvironmentConfig();
    } else if (kubernetesEnvironmentFileExists()) {
      properties = getEnvironmentConfigFromKubernetes();
    } else {
      LOGGER.error(ErrorConstants.ENV_PROP_LOADING_FAILED);
      throw new IllegalStateException(ErrorConstants.ENV_PROP_LOADING_FAILED);
    }
  }

  public static String getProfileFromEnvironmentVar() {
    final String profileName = EnvironmentVarUtils.getEnvironmentVar(EnvironmentConstants.ENVIRONMENT_VAR_NAME);

    if (profileName == null) {
      return EnvironmentConstants.LOCAL_ENVIRONMENT;
    }
    return profileName;
  }

  private Properties getLocalEnvironmentConfig() throws Exception {
    final Properties prop = new Properties();
    try {
      InputStream inputStream =
          getClass().getClassLoader().getResourceAsStream(PropertiesConstant.LOCAL_PROP_FILE_NAME);
      prop.load(inputStream);
    } catch (Exception e) {
      LOGGER.error(ErrorConstants.ENV_LOCAL_CONFIG_ACCESS_DENIED);
      LOGGER.error(String.format("%s", e));
      throw e;
    }
    return prop;
  }

  private boolean kubernetesEnvironmentFileExists() {
    final File configFile = new File(EnvironmentConstants.ENVIRONMENT_CONFIG_KUBERNETES_PATH);
    return configFile.exists();
  }

  private Properties getEnvironmentConfigFromKubernetes() throws Exception {
    final Properties prop = new Properties();
    try {
      final File configFile = new File(EnvironmentConstants.ENVIRONMENT_CONFIG_KUBERNETES_PATH);
      if (configFile.length() != 0) {
        prop.load(new FileInputStream(configFile));
      } else {
        LOGGER.error(ErrorConstants.ENV_PROP_FILE_EMPTY);
        throw new IllegalStateException(ErrorConstants.ENV_PROP_FILE_EMPTY);
      }
    } catch (Exception e) {
      LOGGER.error(ErrorConstants.ENV_K8S_CONFIG_ACCESS_DENIED);
      LOGGER.error(String.format("%s", e));
      throw e;
    }
    return prop;
  }

  public String getProperty(final String property) {
    if (!properties.containsKey(property)) {
      throw new NoSuchElementException(ErrorConstants.ENV_PROPERTY_NOT_FOUND+property);
    }
    return properties.getProperty(property);
  }

  public String getCurrentProfile() {
    return currentProfile;
  }
}
