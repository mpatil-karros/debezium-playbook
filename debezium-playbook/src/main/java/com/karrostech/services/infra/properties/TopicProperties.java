package com.karrostech.services.infra.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@PropertySource("classpath:${topic.properties.filename}")
@Component
@Validated
public class TopicProperties {

    //FIXME: change it to private

    @Value("${integration.topic.pattern}")
    public String topicFormat;

    @Value("${integration.connector.pattern}")
    public String connecterNameFormat;

    @Value("${integration.models.list}")
    public List<String> topicModels;



}
