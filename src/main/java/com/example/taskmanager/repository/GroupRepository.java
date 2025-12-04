package com.example.taskmanager.repository;

import com.example.taskmanager.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByName(String name);

    List<Group> findByNameContainingIgnoreCase(String name);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.tasks WHERE g.id = :id")
    Optional<Group> findByIdWithTasks(@Param("id") Long id);

    @Query("SELECT g FROM Group g WHERE g.user.id = :userId")
    List<Group> findByUserId(@Param("userId") Long userId);
}