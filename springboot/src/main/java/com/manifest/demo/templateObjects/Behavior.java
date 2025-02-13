package com.manifest.demo.templateObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Behavior {
	
	private ScaleDown scaleDown;

}
