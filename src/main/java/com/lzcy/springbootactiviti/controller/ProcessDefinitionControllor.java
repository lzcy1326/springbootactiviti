package com.lzcy.springbootactiviti.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@RestController
public class ProcessDefinitionControllor{

	@Resource
	private RepositoryService repositoryService;
	@Resource
	private RuntimeService runtimeService;
	
	protected static Log log = LogFactory.getLog(ProcessDefinitionControllor.class);
	
	@RequestMapping(value="/processdefinition/querylist")
	public ModelAndView queryProcessDefinitions(){
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().desc().list();
		ModelAndView mv = new ModelAndView("queryprocessdefinitions");
		mv.getModel().put("flows", list);
		return mv;
	}
	
	@RequestMapping(value="/processdefinition/deploy")
	public ModelAndView deploy(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
		if(file.getSize() < 1){
			throw new IOException("没有可发布的文件内容!");
		}
		Deployment deployment = null;
		if(file.getOriginalFilename().endsWith("zip")){
			ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream());
	        deployment = repositoryService//与流程定义和部署对象相关的Service
	                        .createDeployment()//创建一个部署对象
	                        .name(file.getOriginalFilename() + new Date().getTime())//添加部署名称
	                        .addZipInputStream(zipInputStream)//完成zip文件的部署
	                        .deploy();//完成部署
		}else{
			deployment = repositoryService.createDeployment()
					.name(file.getOriginalFilename() + new Date().getTime())
					.addInputStream(file.getOriginalFilename(), file.getInputStream()).deploy();
		}
		log.debug("发布ID是: " + deployment.getId());
		return queryProcessDefinitions();
	}
	
	@RequestMapping(value="/processdefinition/delete/{processDefinitionId}")
	public @ResponseBody String delete(@PathVariable("processDefinitionId") String processDefinitionId) {
		ProcessDefinition processdefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
		repositoryService.deleteDeployment(processdefinition.getDeploymentId(), true);
		return "processdefinition/querylist";
	}
	
	@RequestMapping("/processdefinition/querypng/{processDefinitionId}")  
	public void showProcessDefinitionPng(@PathVariable("processDefinitionId") String processDefinitionId, HttpServletResponse response) throws Exception{  
	    OutputStream os = response.getOutputStream();
	    byte [] by = new byte[1024];
	    ProcessDefinition processdefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
	    List<String> resourcenames = repositoryService.getDeploymentResourceNames(processdefinition.getDeploymentId());
	    InputStream ip = null;
		for (String s : resourcenames) {
			if(s.endsWith(".png")){
				ip = repositoryService.getResourceAsStream(processdefinition.getDeploymentId(), s);
				break;
			}
		}
	    while (IOUtils.read(ip, by) != 0) {
			IOUtils.write(by, os); 
		}
	}
	
	@RequestMapping(value="/processdefinition/start/{processDefinitionId}")
	public @ResponseBody String start(@PathVariable("processDefinitionId") String processDefinitionId,
			@RequestParam Map<String, String> json){
		Map<String, Object> variables = new HashMap<>();
		variables.putAll(json);
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variables);
		log.debug(processInstance.getProcessDefinitionName() + "流程已经启动");
		log.debug("流程实例id: " + processInstance.getId());
		return "task/querylist/" + processInstance.getId();
	}
	
}
