package com.manifest.demo.templateObjects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Spec {
	
	private String type;
	
	private List<Ports> ports;
	
	private Selector selector;
	
	private List<EndPoints> endpoints;
	
	private NamespaceSelector namespaceSelector;
	
	private Template template;
	
	private int progressDeadlineSeconds;
	
	private int replicas;
	
	private Strategy strategy;
	
	private int pollingInterval;
	
	private int minReplicaCount;
	
	private int maxReplicaCount;
	
	private List<Triggers> triggers;
	
	private Advanced advanced;

}
