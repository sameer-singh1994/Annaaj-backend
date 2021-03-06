package com.annaaj.store.config;

import com.zaxxer.hikari.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Configuration
public class DatabaseConfig {

  @Value("${spring.datasource.url}")
  //@Value("postgres://bleobskvknjebh:0f375194cb9cc4f76743f959ba0e72a17a4b90592b984e4ec35d5d8908323342@ec2-107-22-245-82.compute-1.amazonaws.com:5432/d1pv0jjbf5qlbd")
  private String url;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Bean
  public DataSource dataSource() {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(url);
      config.setUsername(username);
      config.setPassword(password);
      return new HikariDataSource(config);
  }
}
