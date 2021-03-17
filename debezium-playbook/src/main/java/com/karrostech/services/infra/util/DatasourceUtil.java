package com.karrostech.services.infra.util;

import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

//@Configuration
//@Lazy
public class DatasourceUtil {

    //@Bean
    public DataSource getDataSource(String dbHost, String userName, String password) {

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://"+dbHost+":5432/Athena")
                .username(userName)
                .password(password)
                .build();
    }
}
