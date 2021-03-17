package com.karrostech.services.infra.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectorStatus {
    private String connectorName;
    private CONNECTOR_STATUS connectorsStatus;
    private String statusMessage;
}
