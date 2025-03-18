package com.swpu.yosmart.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TaskUpdateStatusDTO {
	private Long taskId;
	private Integer status;
}
