package com.manifest.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.manifest.demo.templateObjects.AliasAndNameSpaceValue;
import com.manifest.demo.templateObjects.AnnotationsValues;
import com.manifest.demo.templateObjects.Deployment;
import com.manifest.demo.templateObjects.DeploymentValues;
import com.manifest.demo.templateObjects.Env;
import com.manifest.demo.templateObjects.ImageValues;
import com.manifest.demo.templateObjects.IngressValues;
import com.manifest.demo.templateObjects.ItemsValues;
import com.manifest.demo.templateObjects.KedaValues;
import com.manifest.demo.templateObjects.PortValues;
import com.manifest.demo.templateObjects.ResourceLimitRequestsValues;
import com.manifest.demo.templateObjects.ResourcesValues;
import com.manifest.demo.templateObjects.RollingUpdateValue;
import com.manifest.demo.templateObjects.RolloutStrategyValues;
import com.manifest.demo.templateObjects.SecretResourcesValues;
import com.manifest.demo.templateObjects.SecretValues;
import com.manifest.demo.templateObjects.Secrets;
import com.manifest.demo.templateObjects.StartupProbeValues;
import com.manifest.demo.templateObjects.Values;

@Service
public class HelmServiceImpl {
	
	static YAMLFactory yamlFactory;

	static ObjectMapper mapper;
	
	static Values values;
	static DeploymentValues deploymentValues = new DeploymentValues();
	static String repoName = "";
	static String repoNameWithDashes = "";

	public void convertToHelm(String repoName, MultipartFile file) throws IOException {
		convert(repoName, file);
	}
	
	public static void convert(String reposName, MultipartFile file) throws IOException {
		repoName = reposName;
		repoNameWithDashes = repoName.replace("_", "-");
		values = new Values();
		mapper = new ObjectMapper(new YAMLFactory()
				.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
				.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));
		mapper.disable(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature());
		setDefaultValues();
		System.out.println("Creating new helm files from Manifest file...");
		createHelmObjectFilesFromManifestFile(repoName, file);
		System.out.println("All helm files with temp file names created!!!");
		System.out.println("Renaming files...");
		renameHelmTempFiles();
		System.out.println("Fetching Data from Service Object and pushing it to values yaml...");
		createServieObject();
		System.out.println("Fetching Data from Deployment Object and pushing it to values yaml...");
		createDeploymentAndSecretObject();
		System.out.println("Ingress Object pushing to values yaml...");
		createIngressObject();
		createKedaObject();
		System.out.println("Writing data to...");
		mapper.writeValue(new File(repoName+"_helm/values.yaml"), values);
		System.out.println("Success!!!");
	}
	
	public static void setDefaultValues() {
		values.setLongName(repoNameWithDashes);
		values.setBuildId("&buildId" + " #{Build.BuildId}#");
		values.setBuildRequestedFor("&buildRequestedFor" + " #{Build.RequestedFor}#");
		values.setReleaseRequestedFor("&releaseRequestedFor" + " !{Release.RequestedFor}!");
		values.setReleaseUri("&releaseUri" + " !{Release.ReleaseWebURL}!");
		values.setRepoName("&repoName" + " #{Build.Repository.Uri}#");
		values.setSourceBranch("&sourceBranch" + " #{Build.SourceBranch}#");
		ImageValues imageValues = new ImageValues();
		imageValues.setPullSecrets("gar-image-pull");
		imageValues.setPullPolicy("IfNotPresent");
		imageValues.setRegistry("us-central1-docker.pkg.dev/jbh-prd-devops/jbh-images/" + repoName);
		imageValues.setTag("&image-tag" + " #{Build.SourceVersion}#");
		values.setImage(imageValues);
		AnnotationsValues annotationsValues = new AnnotationsValues();
		annotationsValues.setBuild_id("*buildId");
		annotationsValues.setBuild_reponame("*repoName");
		annotationsValues.setBuild_requested_for("*buildRequestedFor");
		annotationsValues.setBuild_sha("*image-tag");
		annotationsValues.setBuild_sourcebranch("*sourceBranch");
		annotationsValues.setRelease_requested_for("*releaseRequestedFor");
		annotationsValues.setRelease_uri("*releaseUri");
		deploymentValues.setAnnotations(annotationsValues);
	}

	public static void createHelmObjectFilesFromManifestFile(String repoName, MultipartFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
	    BufferedReader reader =   new BufferedReader(new InputStreamReader(inputStream));
		File newFile = null;
		int i = 1;

		try {
			File directory = new File(repoName+"_helm");
		    if (! directory.exists()){
		        directory.mkdir();
		    } else {
		    	FileUtils.cleanDirectory(directory);
		    }
			String line = reader.readLine();

			while (line != null) {
				if(line.trim().contains("---")) {
					newFile = new File(repoName+"_helm/file-" + i + ".yaml");
					i++;
				}
				line = line.replace("!{", "").replace("}!", "").replace("#{", "").replace("}#", "").replace("prometheusServer", "https://thanos-dev.nonprod.jbhunt.com");
				if(newFile != null && !line.trim().contains("---")) {
					try (PrintWriter out = new PrintWriter(new FileWriter(newFile, true))) {
						out.append(line + "\n");
					}
				}
				line = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void renameHelmTempFiles() throws IOException {
		try (Stream<Path> filepath = Files.walk(Paths.get(repoName+"_helm")))
		{
			filepath.forEach(file -> {
				String newFilename = "";
				if(file.getParent() != null) {
					try {
						BufferedReader reader = new BufferedReader(new FileReader(file.toFile()));
						String line = reader.readLine();

						while (line != null) {
							if(line.trim().contains("kind")) {
								String[] string = line.split(":");
								newFilename = string[1].trim();
								break;
							}
							line = reader.readLine();
						}
						reader.close();
					}
					catch (IOException e) {
						System.out.println("Exception during renaming files...");
					}
				}
				if(newFilename.toLowerCase().equalsIgnoreCase("scaledobject")) {
					newFilename = "keda";
				}
				File newFile = new File(repoName+"_helm/" + newFilename.toLowerCase() + ".yaml");
				file.toFile().renameTo(newFile);
			});
		}
		catch (IOException e) {
			throw new IOException("Directory Not Present!");
		}
	}
	
	public static void createServieObject() throws StreamReadException, DatabindException, IOException {
		File f = new File(repoName+"_helm/service.yaml");
		if(f.exists() && !f.isDirectory()) { 
			com.manifest.demo.templateObjects.Service serviceObject = mapper.readValue(new File(repoName+"_helm/service.yaml"), com.manifest.demo.templateObjects.Service.class);
			PortValues portValues = new PortValues();
			portValues.setName(serviceObject.getSpec().getPorts().get(0).getName());
			portValues.setNumber(serviceObject.getSpec().getPorts().get(0).getPort());
			values.setPort(portValues);
		}
	}
	
	public static void createDeploymentAndSecretObject() throws StreamReadException, DatabindException, IOException {
		File f = new File(repoName+"_helm/deployment.yaml");
		if(f.exists() && !f.isDirectory()) { 
			Deployment deployment = mapper.readValue(f, Deployment.class);
			StartupProbeValues startUpProbe = new StartupProbeValues();
			StartupProbeValues livenessProbe = new StartupProbeValues();
			StartupProbeValues readinessProbe = new StartupProbeValues();
			ResourcesValues resourcevalues = new ResourcesValues();
			ResourceLimitRequestsValues limitsValues = new ResourceLimitRequestsValues();
			limitsValues.setCpu(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits().getCpu());
			limitsValues.setMemory(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits().getMemory());
			ResourceLimitRequestsValues requestsValues = new ResourceLimitRequestsValues();
			requestsValues.setCpu(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().getCpu());
			requestsValues.setMemory(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().getMemory());
			resourcevalues.setLimits(limitsValues);
			resourcevalues.setRequests(requestsValues);
			deploymentValues.setResources(resourcevalues);
			
			startUpProbe.setFailureThreshold(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getStartupProbe().getFailureThreshold());
			livenessProbe.setFailureThreshold(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe().getFailureThreshold());
			if(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe() != null)
				readinessProbe.setFailureThreshold(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe().getFailureThreshold());
			
			startUpProbe.setInitialDelaySeconds(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getStartupProbe().getInitialDelaySeconds());
			livenessProbe.setInitialDelaySeconds(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe().getInitialDelaySeconds());
			if(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe() != null)
				readinessProbe.setInitialDelaySeconds(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe().getInitialDelaySeconds());
			if(values.getPort() == null && deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0) != null) {
				PortValues portValues = new PortValues();
				portValues.setNumber(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).getContainerPort());
				values.setPort(portValues);
			}
			for (Env env : deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()) {
				if(env.getName().equalsIgnoreCase("JAVA_OPTS")) {
					deploymentValues.setJavaOpts(env.getValue());
				}
			}
			deploymentValues.setEnableAntiAffinity(true);
			deploymentValues.setStartupProbe(startUpProbe);
			deploymentValues.setLivenessProbe(livenessProbe);
			deploymentValues.setReadinessProbe(readinessProbe);
			deploymentValues.setHealthEndpoint(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getStartupProbe().getHttpGet().getPath());
			String contextPath = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getStartupProbe().getHttpGet().getPath().replace("/health", "");
			deploymentValues.setContextPath(contextPath);
			deploymentValues.setPrometheusEndpoint(contextPath+"/prometheus");
			String timeoutSeconds = null;
			if(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getStartupProbe().getTimeoutSeconds() != null) {
				timeoutSeconds = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getStartupProbe().getTimeoutSeconds();
			} else if (deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe().getTimeoutSeconds() != null) {
				timeoutSeconds = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe().getTimeoutSeconds();
			} else if(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe() != null && deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe().getTimeoutSeconds() != null) {
				timeoutSeconds = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getReadinessProbe().getTimeoutSeconds();
			}
			deploymentValues.setTimeoutSeconds(timeoutSeconds);
			if(deployment.getSpec().getStrategy() != null) {
				RolloutStrategyValues rolloutStrategyValues = new RolloutStrategyValues();
				rolloutStrategyValues.setType(deployment.getSpec().getStrategy().getType());
				RollingUpdateValue rollingUpdateValue = new RollingUpdateValue();
				rollingUpdateValue.setMaxUnavailable(deployment.getSpec().getStrategy().getRollingUpdate().getMaxUnavailable());
				rollingUpdateValue.setMaxSurge("50%");
				rolloutStrategyValues.setRollingUpdate(rollingUpdateValue);
				deploymentValues.setRolloutStrategy(rolloutStrategyValues);
			}
			values.setDeployment(deploymentValues);
			int replicaCount = deployment.getSpec().getReplicas() > 1 ? 1 : 1;
			values.setReplicaCount(replicaCount);
			values.setProgressDeadlineSeconds(deployment.getSpec().getProgressDeadlineSeconds());
			
			File f1 = new File(repoName+"_helm/secret.yaml");
			if(f1.exists() && !f1.isDirectory()) { 
				List<Env> envs = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
				List<SecretResourcesValues> resourcesValues = new ArrayList<>();
				List<ItemsValues> itemsValues = new ArrayList<>();
		
				Secrets secrets = mapper.readValue(f1, Secrets.class);
				envs.stream().forEach(env -> {
					ItemsValues itemsValue = new ItemsValues();
					String name = env.getName();
					if(!name.equalsIgnoreCase("JAVA_OPTS")) {
						String key = env.getValueFrom().getSecretKeyRef().getKey();
						String value = secrets.getData().get(key);
						itemsValue.setKey(key);
						itemsValue.setName(name);
						itemsValue.setValue("!{"+value+"}!");
						itemsValues.add(itemsValue);
					}
				});
				SecretResourcesValues secretResourcesValues = new SecretResourcesValues();
				secretResourcesValues.setItems(itemsValues);
				resourcesValues.add(secretResourcesValues);
				SecretValues secretValues = new SecretValues();
				secretValues.setEncode(false);
				secretValues.setResources(resourcesValues);
				values.setSecrets(secretValues);
			}
		}
	}
	
	public static void createIngressObject() {
		File f = new File(repoName+"_helm/ingress.yaml");
		if(f.exists() && !f.isDirectory()) { 
			IngressValues ingressValues = new IngressValues();
			AliasAndNameSpaceValue alias = new AliasAndNameSpaceValue();
			alias.setHost("loads360executionservices-dev.nonprod.jbhunt.com");
			AliasAndNameSpaceValue namespace = new AliasAndNameSpaceValue();
			namespace.setHost("operationsexecution-dev.nonprod.jbhunt.com");
			ingressValues.setAlias(alias);
			ingressValues.setNamespace(namespace);
			values.setIngress(ingressValues);
		}
	}
	
	public static void createKedaObject() throws StreamReadException, DatabindException, IOException {
		File f = new File(repoName+"_helm/keda.yaml");
		if(f.exists() && !f.isDirectory()) { 
			Deployment keda = mapper.readValue(f, Deployment.class);
			KedaValues kedaValues = new KedaValues();
			kedaValues.setEnabled(true);
			int minReplicaCount = keda.getSpec().getMinReplicaCount() > 1 ? 1 : 1;
			int maxReplicaCount = keda.getSpec().getMaxReplicaCount() > 1 ? 1 : 1;
			kedaValues.setMaxReplicaCount(maxReplicaCount);
			kedaValues.setMinReplicaCount(minReplicaCount);
			kedaValues.setPollingInterval(keda.getSpec().getPollingInterval());
			if(keda.getSpec().getAdvanced() != null)
				kedaValues.setScaleDownDelay(keda.getSpec().getAdvanced().getHorizontalPodAutoscalerConfig().getBehavior().getScaleDown().getStabilizationWindowSeconds());
			kedaValues.setTriggerTemplates(keda.getSpec().getTriggers());
			values.setKeda(kedaValues);
		}
	}

}
