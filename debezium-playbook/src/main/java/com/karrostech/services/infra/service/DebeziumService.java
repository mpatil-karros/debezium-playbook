package com.karrostech.services.infra.service;

import com.karrostech.services.infra.model.CONNECTOR_OPERATION;
import com.karrostech.services.infra.model.ConnectorStatus;
import com.karrostech.services.infra.model.request.DatabaseConfigRequest;

import java.util.List;

public interface DebeziumService {

    List<ConnectorStatus> onBoardConnectors(String tenantName, DatabaseConfigRequest databaseConfigRequest);
    List<ConnectorStatus> getTenantConnectors(String tenantName,String connectorName);
    List<ConnectorStatus> offBoardConnectors(String tenantName,String connectorName);
    List<ConnectorStatus> pauseOrResumeConnector(String tenantName, String connectorName, CONNECTOR_OPERATION operation);

}
