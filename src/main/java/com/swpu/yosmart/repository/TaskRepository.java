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

	/**
	 * 判断某个任务是否和某个用户关联
	 *
	 * @param taskId
	 * @return
	 */
	@Query("MATCH (p:Person {name: $userName}) MATCH (t:Task) WHERE id(t) = $taskId RETURN EXISTS { MATCH (p)-[:ASSIGNED_TO]->(:Task)-[:DEPENDS_ON*0..]->(t) } AS isRelated")
	Boolean isTaskRelatedToPerson(@Param("userName") String userName, @Param("taskId") Long taskId);

	/**
	 * 修改任务的一些属性
	 *
	 * @param taskId
	 * @param description
	 * @param priority
	 * @param repeat
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @param tags
	 * @param updatedAt
	 */
	@Query("MATCH (t:Task) WHERE id(t) = $taskId SET t.description = $description, t.priority = $priority, t.repeat = $repeat, t.startTime = $startTime, t.endTime = $endTime, t.status = $status, t.tags = $tags, t.updatedAt = $updatedAt")
	void updateTask(@Param("taskId") Long taskId, @Param("description") String description, @Param("priority") Integer priority, @Param("repeat") Boolean repeat, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("status") Integer status, @Param("tags") List<String> tags, @Param("updatedAt") LocalDateTime updatedAt);

	/**
	 * 修改任务的状态
	 * @param taskId
	 * @param status
	 */
	@Query("MATCH (t:Task) WHERE id(t) = $taskId SET t.status = $status")
	void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") Integer status);

	/**
	 * 查询某段时间中某用户的全部任务
	 *
	 * <p> 示例： </p>
	 * <ul>
	 *     开始时间为当前日期00：00，结束时间为当前日期之后一天的00：00
	 *     <br>
	 *     结果为返回当前日期内的所有任务
	 * </ul>
	 *
	 * @param userName
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@Query("MATCH (t:Task) WHERE t.startTime < $endTime AND t.endTime >= $startTime AND NOT EXISTS { MATCH (t)-[:DEPENDS_ON]->(:Task) } AND ( EXISTS { MATCH (:Person {name: $userName})-[:ASSIGNED_TO]->(t) } OR EXISTS { MATCH (:Person {name: $userName})-[:ASSIGNED_TO]->(:Task)-[:DEPENDS_ON]->(t) } ) RETURN t")
	List<TaskEntity> getTasksFromTo(@Param("userName") String userName, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

	/**
	 * 删除数据库中所有的孤立节点
	 */
	@Query("MATCH (t:Task) WHERE NOT (t)--() DELETE t")
	void clearTask();
}
