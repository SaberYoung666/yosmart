package com.swpu.yosmart.entity.dto.apidto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubTaskDTO {
	private String description;
	private int priority;
	private boolean repeat;

	@JsonProperty("startTime")
	private String startTime;

	@JsonProperty("endTime")
	private String endTime;

	private List<String> tags;
}