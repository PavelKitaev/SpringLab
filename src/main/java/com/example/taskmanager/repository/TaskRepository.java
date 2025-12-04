package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.group.id = :groupId")
    List<Task> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT t FROM Task t WHERE t.group IS NULL")
    List<Task> findByGroupIsNull();
}