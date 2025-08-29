package com.pm.authservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class AppConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

  private static boolean isRunningInsideDocker(){
    try{
      String cgroup = new String(Files.readAllBytes(Paths.get("/proc/1/cgroup")));
      return cgroup.contains("docker") || cgroup.contains("kubepods");
    }catch (Exception e){
      LOGGER.error("Error while reading docker cgroup", e.getMessage());
      return false;
    }
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer =  new PropertySourcesPlaceholderConfigurer();

    /*
    configurer.setLocation( new FileSystemResource(
      isRunningInsideDocker()
        ? ".env.docker"
        : ".env.local"
    ));
    */
    return configurer;
  }
}
