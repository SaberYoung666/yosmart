package com.swpu.yosmart.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node("Person")
@Getter
@Setter
public class UserEntity {
	@Id
	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
