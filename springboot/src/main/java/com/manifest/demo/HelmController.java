package com.manifest.demo;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/helmApi/v1")
public class HelmController {
	
	@Autowired
	HelmServiceImpl helmService;
	
	@PostMapping(value = "/convert/{repoName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
	private String convert(@PathVariable("repoName") String repoName, @RequestBody MultipartFile file) throws IOException {
		helmService.convertToHelm(repoName, file);
		return "Success!!!";
	}

}
