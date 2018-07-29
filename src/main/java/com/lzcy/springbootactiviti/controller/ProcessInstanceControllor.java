package com.iflytek.jdxt.jdactiviti.controller;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;


@RestController
public class ProcessInstanceControllor {
	
	protected static Log log = LogFactory.getLog(ProcessInstanceControllor.class);
	
	@Resource
	private RuntimeService runtimeService;
	@Resource
	private RepositoryService repositoryService;
	@Resource
	private HistoryService historyService;
	@Resource
	private ManagementService managementService;
	
	@RequestMapping(value="/processinstance/querylist/{processDefinitionId}")
	public ModelAndView queryProcessInstances(@PathVariable("processDefinitionId") String processDefinitionId){
		List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinitionId)
				.orderByProcessInstanceId().asc().list();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
			.processDefinitionId(processDefinitionId).singleResult();
		List<HistoricProcessInstance> hislist = historyService.createHistoricProcessInstanceQuery()
				.processDefinitionId(processDefinitionId).orderByProcessInstanceStartTime().desc().list();
		ModelAndView mv = new ModelAndView("queryprocessinstances");
		mv.getModel().put("flows", list);
		mv.getModel().put("hisflows", hislist);
		mv.getModel().put("name", processDefinition.getName());
		return mv;
	}
	
	/*@RequestMapping("/processinstance/trace/{processInstanceId}")  
	public void showProcessInstanceTracePng (@PathVariable("processInstanceId") String processInstanceId, HttpServletResponse response) throws Exception{  
	    response.setContentType("image/png"); 
	    Command<InputStream> cmd = new MyHistoryProcessInstanceDiagramCmd(processInstanceId);
	    OutputStream os = response.getOutputStream();
	    byte [] by = new byte[1024];
	    InputStream is = managementService.executeCommand(cmd);
	    while (IOUtils.read(is, by) != 0) {
			IOUtils.write(by, os); 
		}
	}*/

}
