package com.swpu.yosmart.entity.dto;

import com.swpu.yosmart.entity.EnvironmentEntity;
import com.swpu.yosmart.entity.ResourceEntity;
import com.swpu.yosmart.entity.TaskEntity;
import com.swpu.yosmart.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AddTaskDTO {
	private String description;
	private Integer priority;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer estimatedTime;
	private List<String> tags;

	/**
	 * 该任务依赖于哪些任务
	 */
	@Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.INCOMING)
	private List<TaskEntity> dependsOn = new ArrayList<>();

	/**
	 * 该任务使用哪些资源
	 */
	@Relationship(type = "USES", direction = Relationship.Direction.INCOMING)
	private List<ResourceEntity> uses = new ArrayList<>();

	/**
	 * 该任务受哪些环境影响
	 */
	@Relationship(type = "AFFECTED_BY", direction = Relationship.Direction.INCOMING)
	private List<EnvironmentEntity> affectedBy = new ArrayList<>();
}
