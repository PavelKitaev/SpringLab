package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskDTO;
import com.example.taskmanager.model.Group;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, GroupRepository groupRepository) {
        this.taskRepository = taskRepository;
        this.groupRepository = groupRepository;
    }

    public TaskDTO createTask(CreateTaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.PENDING);

        // Если указан groupId, находим группу и устанавливаем ее
        if (taskDTO.getGroupId() != null) {
            Group group = groupRepository.findById(taskDTO.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + taskDTO.getGroupId()));
            task.setGroup(group);
        }

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, UpdateTaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());

        if (taskDTO.getStatus() != null) {
            task.setStatus(taskDTO.getStatus());
        }

        // Обновляем группу, если указан groupId
        if (taskDTO.getGroupId() != null) {
            Group group = groupRepository.findById(taskDTO.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + taskDTO.getGroupId()));
            task.setGroup(group);
        } else if (taskDTO.getGroupId() == null && task.getGroup() != null) {
            // Если groupId явно null, удаляем связь с группой
            task.setGroup(null);
        }
        // Если groupId не указан в DTO, оставляем текущую группу

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<TaskDTO> getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(this::convertToDTO);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<TaskDTO> getTasksWithoutGroup() {
        return taskRepository.findByGroupIsNull().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Метод для обновления группы у задачи
    public TaskDTO updateTaskGroup(Long taskId, Long groupId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (groupId != null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
            task.setGroup(group);
        } else {
            task.setGroup(null);
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    private TaskDTO convertToDTO(Task task) {
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