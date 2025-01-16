package com.swpu.yosmart.repository;

import com.swpu.yosmart.entity.TaskEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends Neo4jRepository<TaskEntity, Long> {

	@Query("MATCH (p:Person {name: $userName})-[:ASSIGNED_TO]->(t:Task) RETURN t")
	List<TaskEntity> findByAssignedToUser(@Param("userName") String userName);
}
