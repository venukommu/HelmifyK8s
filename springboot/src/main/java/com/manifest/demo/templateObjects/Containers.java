package com.manifest.demo.templateObjects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Containers {
	
	private Resources resources;
	private List<Env> env;
	private StartLiveAndReadinessProbe startupProbe;
	private StartLiveAndReadinessProbe livenessProbe;
	private StartLiveAndReadinessProbe readinessProbe;
	private List<Ports> ports;

}
