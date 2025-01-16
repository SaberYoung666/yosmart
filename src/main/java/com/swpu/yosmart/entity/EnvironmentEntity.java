package com.swpu.yosmart.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node("Environment")
@Getter
@Setter
public class EnvironmentEntity {
	@Id
	@GeneratedValue
	private Long id;
	private String type;
	private String value;
	private LocalDateTime timestamp;
}
