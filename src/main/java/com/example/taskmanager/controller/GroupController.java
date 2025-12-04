package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CreateGroupDTO;
import com.example.taskmanager.dto.GroupDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Group Management", description = "Operations pertaining to task groups")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @Operation(summary = "Create a new task group")
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody CreateGroupDTO groupDTO) {
        GroupDTO createdGroup = groupService.createGroup(groupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @GetMapping
    @Operation(summary = "Get all task groups")
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a group by ID")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-tasks")
    @Operation(summary = "Get a group with all its tasks")
    public ResponseEntity<GroupDTO> getGroupWithTasks(@PathVariable Long id) {
        return groupService.getGroupWithTasks(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get all tasks in a group")
    public ResponseEntity<List<TaskDTO>> getTasksByGroup(@PathVariable Long id) {
        List<TaskDTO> tasks = groupService.getTasksByGroupId(id);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task group")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable Long id,
                                                @Valid @RequestBody CreateGroupDTO groupDTO) {
        GroupDTO updatedGroup = groupService.updateGroup(id, groupDTO);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task group (with all its tasks)")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/tasks/{taskId}")
    @Operation(summary = "Add a task to a group")
    public ResponseEntity<GroupDTO> addTaskToGroup(@PathVariable Long groupId,
                                                   @PathVariable Long taskId) {
        GroupDTO updatedGroup = groupService.addTaskToGroup(groupId, taskId);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}/tasks/{taskId}")
    @Operation(summary = "Remove a task from a group")
    public ResponseEntity<GroupDTO> removeTaskFromGroup(@PathVariable Long groupId,
                                                        @PathVariable Long taskId) {
        GroupDTO updatedGroup = groupService.removeTaskFromGroup(groupId, taskId);
        return ResponseEntity.ok(updatedGroup);
    }
}