package com.manifest.demo.templateObjects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class KedaValues {
	
	private boolean enabled;
	
	private int pollingInterval;
	
	private int minReplicaCount;
	
	private int maxReplicaCount;
	
	private int scaleDownDelay;
	
	private List<Triggers> triggerTemplates;

}
