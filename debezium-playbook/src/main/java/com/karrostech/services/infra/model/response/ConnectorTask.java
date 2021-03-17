package com.karrostech.services.infra.model.response;

import com.karrostech.services.infra.model.CONNECTOR_STATUS;
import lombok.Data;

@Data
public class ConnectorTask {
    private int id;
    private CONNECTOR_STATUS state;
    private String worker_id;
    private String trace;

}
