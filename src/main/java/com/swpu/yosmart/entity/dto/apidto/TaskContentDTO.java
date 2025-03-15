package com.swpu.yosmart.entity.dto.apidto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskContentDTO {
	@JsonProperty("main_task")
	private String mainTask;

	@JsonProperty("sub_tasks")
	private List<SubTaskDTO> subTasks;
}
