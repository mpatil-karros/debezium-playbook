package com.karrostech.services.infra.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@PropertySource("classpath:${debezium.properties.filename}")
@Component
@Validated
public class DebeziumConfigProperties {

    //FIXME: change it to private
    @Value("${debezium.static.config}")
    public String debeziumStaticConfig;

}
