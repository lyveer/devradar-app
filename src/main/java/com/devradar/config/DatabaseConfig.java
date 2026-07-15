package com.devradar.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();

        boolean isRender = System.getenv("RENDER") != null;

        // If running on Render and the URL points to localhost/127.0.0.1 (default MySQL fallback),
        // automatically switch to in-memory H2 so the service starts up without crashing.
        if (isRender && (url == null || url.contains("localhost:3306") || url.contains("127.0.0.1:3306"))) {
            url = "jdbc:h2:mem:devradar;DB_CLOSE_DELAY=-1";
            username = "sa";
            password = "";
        }

        // Check if we have a postgres:// or postgresql:// URL from Render/Heroku
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                // Adjust scheme to avoid URI parsing issues with postgres://
                String uriString = url.replace("postgres://", "postgresql://");
                URI uri = new URI(uriString);
                String host = uri.getHost();
                int port = uri.getPort();
                if (port == -1) {
                    port = 5432;
                }
                String path = uri.getPath();
                
                // Construct valid JDBC connection URL
                url = "jdbc:postgresql://" + host + ":" + port + path;
                
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    if (parts.length > 1) {
                        password = parts[1];
                    }
                }
            } catch (URISyntaxException e) {
                // Fall back to original properties if URI parsing fails
            }
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        if (properties.getDriverClassName() != null) {
            dataSource.setDriverClassName(properties.getDriverClassName());
        }
        return dataSource;
    }
}
