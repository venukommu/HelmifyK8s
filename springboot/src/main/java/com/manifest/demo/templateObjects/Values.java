package com.manifest.demo.templateObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Values {
	
	private int replicaCount;
	private int progressDeadlineSeconds;
	private String longName;
	private String buildId;
	private String buildRequestedFor;
	private String releaseRequestedFor;
	private String releaseUri;
	private String repoName;
	private String sourceBranch;
	private PortValues port;
	private ImageValues image;
	private DeploymentValues deployment;
	private IngressValues ingress;
	private KedaValues keda;
	private SecretValues secrets;

}
