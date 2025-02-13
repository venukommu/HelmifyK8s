package com.manifest.demo.templateObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class DeploymentValues {
	
	private boolean enableAntiAffinity;
	private String contextPath;
	private String healthEndpoint;
	private String prometheusEndpoint;
	private String timeoutSeconds;
	private StartupProbeValues startupProbe;
	private StartupProbeValues livenessProbe;
	private StartupProbeValues readinessProbe;
	private RolloutStrategyValues rolloutStrategy;
	private ResourcesValues resources;
	private String javaOpts;
	private AnnotationsValues annotations;

}
