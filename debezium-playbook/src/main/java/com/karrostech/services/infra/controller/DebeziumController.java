package com.karrostech.services.infra.controller;

import com.karrostech.services.infra.model.CONNECTOR_OPERATION;
import com.karrostech.services.infra.model.ConnectorStatus;
import com.karrostech.services.infra.model.request.DatabaseConfigRequest;
import com.karrostech.services.infra.service.DebeziumService;
import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController("/tenant")
@Slf4j
public class DebeziumController {

    @Autowired
    DebeziumService debeziumService;

    @GetMapping("/status")
    public @ResponseBody
    List<ConnectorStatus> tenantStatus(@RequestParam String tenantName, @RequestParam(required = false) @ApiIgnore String connectorName){
      log.info("Calling Tenant Status for the tenant:{}", tenantName);
      return debeziumService.getTenantConnectors(tenantName.toLowerCase(),connectorName);
    }

    @PutMapping("/pause")
    public @ResponseBody
    List<ConnectorStatus> pauseTenant(@RequestParam String tenantName, @RequestParam(required = false) @ApiIgnore String connectorName){
        log.info("Pausing the Connector for the tenant:{}", tenantName);
        return debeziumService.pauseOrResumeConnector(tenantName.toLowerCase(),connectorName, CONNECTOR_OPERATION.PAUSE);
    }

    @PutMapping("/resume")
    public @ResponseBody
    List<ConnectorStatus> resumeTenant(@RequestParam String tenantName, @RequestParam(required = false) @ApiIgnore String connectorName){
        log.info("Resuming the Connector for the tenant:{}", tenantName);
        return debeziumService.pauseOrResumeConnector(tenantName.toLowerCase(),connectorName, CONNECTOR_OPERATION.RESUME);
    }

    @PutMapping("/onBoard")
    public @ResponseBody
    List<ConnectorStatus> createConnector(@RequestBody @Validated DatabaseConfigRequest databaseConfigRequest, @RequestParam @NotNull String tenantName){
        log.info("Calling Tenant Status for the Creating connectors:{}",tenantName);
        return debeziumService.onBoardConnectors(tenantName.toLowerCase(), databaseConfigRequest);
    }

    @PutMapping("/offBoard")
    public @ResponseBody
    List<ConnectorStatus> deleteConnector(@RequestParam  String tenantName, @RequestParam(required = false) @ApiIgnore String connectorName){
        log.info("Deleting Connectors for tenant:{}",tenantName);
        return debeziumService.offBoardConnectors(tenantName.toLowerCase(),connectorName);
    }

}
