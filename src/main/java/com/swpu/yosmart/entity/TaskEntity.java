package com.swpu.yosmart.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("Task")
@Getter
@Setter
public class TaskEntity {

	@Id
	@GeneratedValue
	private Long id;

	private String description;
	private Integer priority;
	private Integer status;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer estimatedTime;
	private Integer actualTime;
	private List<String> tags;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	/**
	 * 该任务依赖于哪些任务
	 */
	@Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.INCOMING)
	private List<TaskEntity> dependsOn = new ArrayList<>();

	/**
	 * 该任务属于哪个用户
	 */
	@Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.INCOMING)
	private List<UserEntity> assignedTo = new ArrayList<>();

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
