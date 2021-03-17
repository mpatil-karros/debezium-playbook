package com.karrostech.services.infra.service;

import com.karrostech.services.infra.model.CONNECTOR_OPERATION;
import com.karrostech.services.infra.model.CONNECTOR_STATUS;
import com.karrostech.services.infra.model.ConnectorStatus;
import com.karrostech.services.infra.model.request.DatabaseConfigRequest;
import com.karrostech.services.infra.model.response.ConnectorStatusResponse;
import com.karrostech.services.infra.properties.DebeziumConfigProperties;
import com.karrostech.services.infra.properties.GenericConfigProperties;
import com.karrostech.services.infra.properties.TopicProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DebeziumServiceImpl implements DebeziumService {

    @Autowired
    DebeziumConfigProperties debeziumConfigProperties;

    @Autowired
    TopicProperties topicProperties;

    @Autowired
    GenericConfigProperties genericConfigProperties;

    @Autowired
    RestTemplate restTemplate;

   // @Autowired
   // DatasourceUtil datasourceUtil;





    @Override
    public List<ConnectorStatus> onBoardConnectors(String tenantName, DatabaseConfigRequest databaseConfigRequest) {

        List<ConnectorStatus> connectorStatuses = new ArrayList<>();
       // performDatabasePreReqTasks(databaseConfigRequest);

        topicProperties.topicModels.stream().forEach(model -> {
            connectorStatuses.add(updateConnectorConfig(tenantName, model, databaseConfigRequest,false));
        });

        return connectorStatuses;
    }




    @Override
    public List<ConnectorStatus> getTenantConnectors(String tenantName,String connectorName) {
        List<ConnectorStatus> connectorStatuses = new ArrayList<>();
        if(StringUtils.isBlank(connectorName)){
            topicProperties.topicModels.stream().forEach(connector -> {
                String hydratedConnectorName = String.format(topicProperties.connecterNameFormat, tenantName, connector);
                connectorStatuses.add(getConnnectorStatus(hydratedConnectorName));
            });
        }else {
            connectorStatuses.add(getConnnectorStatus(connectorName));
        }

        return connectorStatuses;
    }

    private ConnectorStatus getConnnectorStatus(String connectorName){
        Triplet<CONNECTOR_STATUS,String,String> statusTriplet = getStatus(null,connectorName,CONNECTOR_OPERATION.STATUS);
        return ConnectorStatus.builder()
                .connectorName(connectorName)
                .connectorsStatus(statusTriplet.getValue0())
                .statusMessage(statusTriplet.getValue1())
                .build();
    }


    /*
    1. Remove the connector
    2. Remove the slot, else the slot will keep growing.
    3. (Optional) Remove the record from debezium_heartbeat.
     */
    @Override
    public List<ConnectorStatus> offBoardConnectors(String tenantName,String cName) {

        List<ConnectorStatus> connectorStatuses = new ArrayList<>();

        topicProperties.topicModels.stream().forEach(connector -> {
            String connectorName = String.format(topicProperties.connecterNameFormat, tenantName, connector);
            log.warn("Deleting the connector : {}", connectorName);

            log.info("Performing first step to clean up the slot,by adding `slot.drop.on.stop`");
            Triplet<CONNECTOR_STATUS,String,String> statusTriplet = getStatus(null,connectorName,CONNECTOR_OPERATION.CONFIG);
            if(statusTriplet.getValue0()!=CONNECTOR_STATUS.RUNNING){
                connectorStatuses.add(ConnectorStatus.builder()
                        .connectorName(connectorName)
                        .connectorsStatus(CONNECTOR_STATUS.ERRORED)
                        .statusMessage("Connector Not Found").build());
                return;
            }

            JSONObject connectorConfig = new JSONObject(getStatus(null,connectorName,CONNECTOR_OPERATION.CONFIG).getValue2());
            connectorConfig.put("slot.drop.on.stop","true");
            handlePUT(connectorConfig.toString(),connectorName,HttpMethod.PUT,CONNECTOR_OPERATION.CONFIG);
            connectorStatuses.add(handlePUT(null,connectorName,HttpMethod.DELETE,CONNECTOR_OPERATION.DELETE));
        });
        return connectorStatuses;
    }

    @Override
    public List<ConnectorStatus> pauseOrResumeConnector(String tenantName,String cName, CONNECTOR_OPERATION operation) {
        List<ConnectorStatus> connectorStatuses = new ArrayList<>();
        if(StringUtils.isBlank(cName)){
            topicProperties.topicModels.stream().forEach(connector -> {
                String connectorName = String.format(topicProperties.connecterNameFormat, tenantName, connector);
                connectorStatuses.add(pauseOrResumeConnector(connectorName, operation));
            });
        }else {
            connectorStatuses.add(pauseOrResumeConnector(cName, operation));
        }

        return connectorStatuses;
    }

    private ConnectorStatus pauseOrResumeConnector(String connectorName, CONNECTOR_OPERATION operation) {
        return handlePUT(null,connectorName,HttpMethod.PUT,operation);
    }





    private ConnectorStatus updateConnectorConfig(String tenantName, String modelName, DatabaseConfigRequest databaseConfigRequest,boolean isOffload) {
        String connectorName = String.format(topicProperties.connecterNameFormat, tenantName, modelName);

        log.debug("Default Debezium Config: \n {}", debeziumConfigProperties.debeziumStaticConfig);
        JSONObject debeziumConfig = new JSONObject(debeziumConfigProperties.debeziumStaticConfig);

        debeziumConfig.put("name", connectorName);
        debeziumConfig.put("slot.name", tenantName.replaceAll("-", "_") + "_" + modelName);
        debeziumConfig.put("table.whitelist", "(.*)." + modelName);
        debeziumConfig.put("transforms.Reroute.topic.replacement", String.format(topicProperties.topicFormat, tenantName, modelName));
        debeziumConfig.put("heartbeat.action.query",
                "INSERT INTO PUBLIC.DEBEZIUM_HEARTBEAT (CONNECTOR_NAME,LAST_HEARTBEAT_TS) VALUES " +
                        "('" + connectorName + "',NOW()) ON CONFLICT (CONNECTOR_NAME) DO UPDATE SET LAST_HEARTBEAT_TS = NOW();");

        //FIXME: Move it out or pass via Incoming payload
        debeziumConfig.put("database.user", databaseConfigRequest.getDbUser());
        debeziumConfig.put("database.dbname", databaseConfigRequest.getDbName());
        debeziumConfig.put("database.server.name", databaseConfigRequest.getDbHost().split("\\.")[0]);
        debeziumConfig.put("transforms.Reroute.topic.regex", databaseConfigRequest.getDbHost().split("\\.")[0] + databaseConfigRequest.getDbSchemasToScan());
        debeziumConfig.put("database.hostname", databaseConfigRequest.getDbHost());
        debeziumConfig.put("database.password", databaseConfigRequest.getDbPassword());


        return handlePUT(debeziumConfig.toString(), connectorName, HttpMethod.PUT,CONNECTOR_OPERATION.CONFIG);

    }
    private ConnectorStatus handlePUT(String requestString, String connectorName,HttpMethod httpMethod, CONNECTOR_OPERATION operation) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestString, headers);
        ConnectorStatus connectorStatus;
        log.info("Creating/Updating connector with config:{}\n",requestString);
        try {
            String connectorConfigUrl = new StringBuilder(genericConfigProperties.debeziumClusterUrl).append("/").append(connectorName).append(operation.getAction()).toString();
            //FIXME: Move Error handling to ResponseErrorHandler
            log.info("Performing {} on the connector : {}", operation, connectorName);
            ResponseEntity<Object> responseEntity = restTemplate.exchange(connectorConfigUrl, httpMethod, entity, Object.class);
            Triplet<CONNECTOR_STATUS,String,String> statusTriplet = getStatus(requestString,connectorName,CONNECTOR_OPERATION.STATUS);
            connectorStatus = ConnectorStatus.builder()
                    .connectorName(connectorName)
                    .connectorsStatus(statusTriplet.getValue0())
                    .statusMessage(statusTriplet.getValue1())
                    .build();
        } catch (HttpClientErrorException responseException) {
            log.error("Recieved an Error from Debezium Connector:{}", responseException.getMessage());
            connectorStatus = ConnectorStatus.builder().connectorName(connectorName).connectorsStatus(CONNECTOR_STATUS.FAILED).statusMessage(responseException.getLocalizedMessage()).build();
            responseException.printStackTrace();
        } catch (Exception exception) {
            log.error("Recieved an Exception:{}", exception.getMessage());
            connectorStatus = ConnectorStatus.builder().connectorName(connectorName).connectorsStatus(CONNECTOR_STATUS.FAILED).statusMessage(exception.getLocalizedMessage()).build();
            exception.printStackTrace();
        }
        return connectorStatus;

    }


    private Triplet<CONNECTOR_STATUS,String,String> getStatus(String requestString, String connectorName, CONNECTOR_OPERATION operation) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestString, headers);
        Triplet<CONNECTOR_STATUS,String ,String> getResponse;

        try {
            String connectorConfigUrl = new StringBuilder(genericConfigProperties.debeziumClusterUrl).append("/").append(connectorName).append(operation.getAction()).toString();
            log.info("Getting {} from the connector : {}", operation, connectorName);
            if(operation==CONNECTOR_OPERATION.STATUS){
                ResponseEntity<ConnectorStatusResponse> responseEntity = restTemplate.exchange(connectorConfigUrl, HttpMethod.GET, entity, ConnectorStatusResponse.class);
                ConnectorStatusResponse connectorResponse = responseEntity.getBody();
                getResponse = Triplet.with(CONNECTOR_STATUS.valueOf(connectorResponse.getConnector().getState()),
                        "Task Executing this connector is at:  "+connectorResponse.getTasks().get(0).getWorker_id() +" with Task Status: "+connectorResponse.getTasks().get(0).getState(),
                       // "",
                        "NA");
            }else{
                ResponseEntity<String> responseEntity = restTemplate.exchange(connectorConfigUrl, HttpMethod.GET, entity, String.class);
                String config = responseEntity.getBody();
                getResponse = Triplet.with(CONNECTOR_STATUS.RUNNING,
                        "Getting Config",
                        config);
            }




        } catch (HttpClientErrorException responseException) {
            log.error("Recieved an Error from Debezium Connector:{}", responseException.getMessage());
            getResponse = Triplet.with(CONNECTOR_STATUS.ERRORED,
                    responseException.getLocalizedMessage(),
                    "NA");
            responseException.printStackTrace();
        } catch (Exception exception) {
            log.error("Recieved an Exception:{}", exception.getMessage());
            getResponse = Triplet.with(CONNECTOR_STATUS.ERRORED,
                    exception.getLocalizedMessage(),
                    "NA");
            exception.printStackTrace();
        }
        return getResponse;

    }

//    private void performDatabasePreReqTasks(DatabaseConfigRequest databaseConfigRequest){
//        log.info("Checking if database is having all the valid PreRequisites: {}", databaseConfigRequest.getJdbcUrl());
//        try{
//            DataSource datasource = datasourceUtil.getDataSource(databaseConfigRequest.getJdbcUrl(), databaseConfigRequest.getDbUser(), databaseConfigRequest.getDbPassword());
//            JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
//
//            Connection databaseConnection = jdbcTemplate.getDataSource().getConnection();
//            DatabaseMetaData databaseMetaData = databaseConnection.getMetaData();
//            //Default Connects to `public` database
//            ResultSet dbzHeartBeatTable = databaseMetaData.getTables(null,"public","DEBEZIUM_HEARTBEAT",null);
//            //Create only if it does not exists.
//            if(!dbzHeartBeatTable.next()){
//                log.info("Creating public.debezium_heartbeat Table!!");
//                jdbcTemplate.execute("CREATE TABLE public.debezium_heartbeat (\n" +
//                        "\t\tconnector_name text PRIMARY KEY,\n" +
//                        "    last_heartbeat_ts TIMESTAMPTZ DEFAULT NOW() \n" +
//                        ")");
//            }
//
//            ResultSet rpSchemasResultSet = databaseMetaData.getSchemas(null,"rp_%");
//
//
//        }catch (SQLException sqlException){
//            log.error("Exception while connecting to Database:{} ", databaseConfigRequest.getJdbcUrl());
//            log.error("Exception Occurred:{} ", sqlException.getLocalizedMessage());
//            sqlException.printStackTrace();
//        }
//
//    }
}
