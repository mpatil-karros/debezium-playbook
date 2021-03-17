package com.karrostech.services.infra.properties;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Component
@Validated
public class GenericConfigProperties {

    //FIXME: change it to private

    @Value("${debezium.cluster.url}")
    public String debeziumClusterUrl;
}
