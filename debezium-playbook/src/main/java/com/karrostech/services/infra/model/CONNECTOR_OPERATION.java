package com.karrostech.services.infra.model;


public enum CONNECTOR_OPERATION {
    CONFIG("/config"), STATUS("/status"), PAUSE("/pause"), RESUME("/resume"), DELETE("/");

    private String action;

    public String getAction()
    {
        return this.action;
    }

    // enum constructor - cannot be public or protected
    private CONNECTOR_OPERATION(String action)
    {
        this.action = action;
    }
    }
