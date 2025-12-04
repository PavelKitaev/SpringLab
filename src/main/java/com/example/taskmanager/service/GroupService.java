package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateGroupDTO;
import com.example.taskmanager.dto.GroupDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Group;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.GroupRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, TaskRepository taskRepository) {
        this.groupRepository = groupRepository;
        this.taskRepository = taskRepository;
    }

    public GroupDTO createGroup(CreateGroupDTO groupDTO) {
        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());

        Group savedGroup = groupRepository.save(group);
        return convertToDTO(savedGroup);
    }

    public List<GroupDTO> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<GroupDTO> getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<GroupDTO> getGroupWithTasks(Long id) {
        return groupRepository.findByIdWithTasks(id)
                .map(this::convertToDTOWithTasks);
    }

    public GroupDTO updateGroup(Long id, CreateGroupDTO groupDTO) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());

        Group updatedGroup = groupRepository.save(group);
        return convertToDTO(updatedGroup);
    }

    @Transactional
    public void deleteGroup(Long id) {
        // Каскадное удаление настроено в сущности Group (orphanRemoval = true)
        groupRepository.deleteById(id);
    }

    public GroupDTO addTaskToGroup(Long groupId, Long taskId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setGroup(group);
        taskRepository.save(task);

        return convertToDTOWithTasks(group);
    }

    public GroupDTO removeTaskFromGroup(Long groupId, Long taskId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (task.getGroup() != null && task.getGroup().getId().equals(groupId)) {
            task.setGroup(null);
            taskRepository.save(task);
        }

        return convertToDTOWithTasks(group);
    }

    public List<TaskDTO> getTasksByGroupId(Long groupId) {
        // Используем обновленный метод репозитория
        List<Task> tasks = taskRepository.findByGroupId(groupId);
        return tasks.stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList());
    }

    // Вспомогательные методы для конвертации
    private GroupDTO convertToDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        return dto;
    }

    private GroupDTO convertToDTOWithTasks(Group group) {
        GroupDTO dto = convertToDTO(group);
        List<TaskDTO> taskDTOs = group.getTasks().stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList());
        dto.setTasks(taskDTOs);
        return dto;
    }

    private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setGroupId(task.getGroupId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}