package com.manifest.demo.templateObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class AnnotationsValues {
	
	private String build_id;
	private String build_reponame;
	private String build_requested_for;
	private String build_sha;
	private String build_sourcebranch;
	private String release_requested_for;
	private String release_uri;

}
