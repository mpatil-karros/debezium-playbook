package com.karrostech.services.infra.model.request;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class DatabaseConfigRequest {

    private String dbHost;

    private String dbName="Athena";

    private String dbUser="edulog";

    private String dbPassword="edul0g";

    private String dbSchemasToScan="(.*)";
}
