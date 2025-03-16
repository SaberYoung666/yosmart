package com.swpu.yosmart.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TaskUpdateDTO {
	private Long id;
	private String description;
	private Integer priority;
	private Boolean repeat;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer status;
	private List<String> tags;
}
