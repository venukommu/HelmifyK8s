package com.manifest.demo.templateObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class RolloutStrategyValues {
	
	private String type;
	private RollingUpdateValue rollingUpdate;

}
