package com.manifest.demo.templateObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartLiveAndReadinessProbe {
	
	private String failureThreshold;
	
	private String timeoutSeconds;
	
	private String initialDelaySeconds;
	
	private HttpGet httpGet;

}
