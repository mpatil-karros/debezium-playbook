package com.karrostech.services.infra.model.response;

import lombok.Data;

import java.util.List;

@Data
public class ConnectorStatusResponse {

    private String name;
    private ConnectorResponse connector;
    private List<ConnectorTask> tasks;

}
