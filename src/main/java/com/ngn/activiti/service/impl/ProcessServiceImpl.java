package com.ngn.activiti.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngn.activiti.pojo.APIResponse;
import com.ngn.activiti.pojo.TaskPojo;
import com.ngn.activiti.pojo.VariablePojo;
import com.ngn.activiti.service.ProcessService;

@Service
public class ProcessServiceImpl implements ProcessService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private TaskService taskService;
	
//	@Autowired
//	private HistoryService historyService;

	@Override
	public APIResponse startProcess(String processKey, Map<String, Object> processVariables) {
		APIResponse apiResponse = new APIResponse();
		ProcessInstance processInstance = null;
		
		try {
			processInstance = runtimeService.startProcessInstanceByKey(processKey, processVariables);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while starting process: ", e);
			apiResponse.setMessage("Process start failed");
			apiResponse.setException(e.getMessage());
			apiResponse.setStatus(500);
			
			return apiResponse;
		}
		
		apiResponse.setMessage("Process started successfully");
		apiResponse.setStatus(200);
		apiResponse.setData(processInstance.getId());
		
		return apiResponse;
	}

	@Override
	public APIResponse listInProgressInstances() {
		APIResponse apiResponse = new APIResponse();
		List<com.ngn.activiti.pojo.ProcessInstance> instancePojos = new ArrayList<com.ngn.activiti.pojo.ProcessInstance>();
		try {
			List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().active().list();
			
			// historyService.createHistoricProcessInstanceQuery().finished().list();
			
			LOGGER.info("Process Instance size: {}", processInstances.size());
			
			for(ProcessInstance processInstance : processInstances) {
				com.ngn.activiti.pojo.ProcessInstance pojo = new com.ngn.activiti.pojo.ProcessInstance();
				pojo.setId(processInstance.getId());
				pojo.setName(processInstance.getName());
				pojo.setProcessDefinitionId(processInstance.getProcessDefinitionId());
				pojo.setProcessDefinitionName(processInstance.getProcessDefinitionName());
				
				instancePojos.add(pojo);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while listing active processes: ", e);
			apiResponse.setMessage("Processes listing failed");
			apiResponse.setException(e.getMessage());
			apiResponse.setStatus(500);
			
			return apiResponse;
		}
		
		apiResponse.setMessage("Active process instances retrieved successfully");
		apiResponse.setStatus(200);
		apiResponse.setData(instancePojos);
		
		return apiResponse;
	}

	@Override
	public APIResponse listTaskForUser(String assignee) {
		APIResponse apiResponse = new APIResponse();
		List<TaskPojo> taskPojos = new ArrayList<TaskPojo>();
		List<VariablePojo> variableList = new ArrayList<VariablePojo>();
		
		try {
			List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).includeTaskLocalVariables().list();
			
			
			for(Task task : tasks) {
				TaskPojo pojo = new TaskPojo();
				pojo.setAssignee(task.getAssignee());
				pojo.setCreatedDate(task.getCreateTime().toString());
				pojo.setId(task.getId());
				pojo.setTaskName(task.getName());
				
				Map<String, VariableInstance> variables = runtimeService.getVariableInstances(task.getProcessInstanceId());
				for(Map.Entry<String, VariableInstance> entry : variables.entrySet()) {
					VariablePojo variablePojo = new VariablePojo();
					variablePojo.setVariableName(entry.getKey());
					variablePojo.setVariableValue(entry.getValue().getValue().toString());
					variableList.add(variablePojo);
				}
				pojo.setTaskVariables(variableList);
				
			//	pojo.setTaskVariables(runtimeService.getVariableInstances(task.getProcessInstanceId()));
				taskPojos.add(pojo);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while listing active tasks: ", e);
			apiResponse.setMessage("Task listing failed");
			apiResponse.setException(e.getMessage());
			apiResponse.setStatus(500);
			
			return apiResponse;
		}
		
		apiResponse.setMessage("Active task instances retrieved successfully");
		apiResponse.setStatus(200);
		apiResponse.setData(taskPojos);
		
		return apiResponse;
	}

}
