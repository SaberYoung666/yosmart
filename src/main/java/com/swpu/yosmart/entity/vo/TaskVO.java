package com.swpu.yosmart.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TaskVO {
	private Long id;

	private String description;
	private Integer priority;
	private Integer status;
	private Boolean repeat;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private List<String> tags;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
