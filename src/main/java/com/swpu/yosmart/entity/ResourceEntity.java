package com.swpu.yosmart.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node("Resource")
@Getter
@Setter
public class ResourceEntity {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String type;
	private String location;
	private LocalDateTime createdAt;
}
