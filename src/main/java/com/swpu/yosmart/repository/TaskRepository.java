package com.swpu.yosmart.repository;

import com.swpu.yosmart.entity.TaskEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends Neo4jRepository<TaskEntity, Long> {

	@Query("MATCH (p:Person {name: $userName})-[:ASSIGNED_TO]->(t:Task) RETURN t")
	List<TaskEntity> findByAssignedToUser(@Param("userName") String userName);

	@Query("MATCH (p:Person)-[:ASSIGNED_TO]->(m:Task)-[:DEPENDS_ON]->(s:Task)  WHERE id(s) = $taskId RETURN COUNT(p) > 0 AS isRelated")
	Boolean isTaskRelatedToPerson(@Param("taskId") Long taskId);

	@Query("MATCH (t:Task) WHERE id(t) = $taskId SET t.description = $description, t.priority = $priority, t.status = $status, t.tags = $tags")
	void updateTask(@Param("taskId") Long taskId, @Param("description") String description, @Param("priority") Integer priority, @Param("status") Integer status, @Param("tags") List<String> tags);

	@Query("MATCH (t:Task) WHERE t.startTime < $tomorrow AND t.endTime >= $today AND NOT EXISTS { MATCH (p:Person)-[:ASSIGNED_TO]->(t)-[:DEPENDS_ON]->(s:Task) } RETURN t")
	List<TaskEntity> todayTasks(@Param("today") LocalDateTime today, @Param("tomorrow") LocalDateTime tomorrow);
}
