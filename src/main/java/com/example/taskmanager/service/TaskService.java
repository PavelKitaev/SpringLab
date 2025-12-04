package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskDTO;
import com.example.taskmanager.model.Group;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.GroupRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.config.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       GroupRepository groupRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    public TaskDTO createTask(CreateTaskDTO taskDTO) {
        User currentUser = getCurrentUser();
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.PENDING);
        task.setUser(currentUser);

        // Если указан groupId, находим группу и устанавливаем ее
        if (taskDTO.getGroupId() != null) {
            Group group = groupRepository.findById(taskDTO.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + taskDTO.getGroupId()));

            // Проверяем, что группа принадлежит пользователю или пользователь - ADMIN
            if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
                throw new AccessDeniedException("You don't have permission to add tasks to this group");
            }

            task.setGroup(group);
        }

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, UpdateTaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());

        if (taskDTO.getStatus() != null) {
            task.setStatus(taskDTO.getStatus());
        }

        // Обновляем группу, если указан groupId
        if (taskDTO.getGroupId() != null) {
            Group group = groupRepository.findById(taskDTO.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + taskDTO.getGroupId()));

            // Проверяем, что группа принадлежит пользователю или пользователь - ADMIN
            if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
                throw new AccessDeniedException("You don't have permission to move task to this group");
            }

            task.setGroup(group);
        } else if (taskDTO.getGroupId() == null && task.getGroup() != null) {
            task.setGroup(null);
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public List<TaskDTO> getAllTasks() {
        User currentUser = getCurrentUser();

        if (isAdmin()) {
            // Админ видит все задачи
            return taskRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            // Обычный пользователь видит только свои задачи
            return taskRepository.findByUserId(currentUser.getId()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    public Optional<TaskDTO> getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view this task");
        }

        return Optional.of(convertToDTO(task));
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to delete this task");
        }

        taskRepository.deleteById(id);
    }

    public List<TaskDTO> getTasksWithoutGroup() {
        User currentUser = getCurrentUser();

        if (isAdmin()) {
            // Админ видит все задачи без группы
            return taskRepository.findByGroupIsNull().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            // Обычный пользователь видит только свои задачи без группы
            return taskRepository.findByUserIdAndGroupIsNull(currentUser.getId()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    // Метод для обновления группы у задачи
    public TaskDTO updateTaskGroup(Long taskId, Long groupId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User currentUser = getCurrentUser();

        // Проверяем права на задачу
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }

        if (groupId != null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

            // Проверяем права на группу
            if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
                throw new AccessDeniedException("You don't have permission to use this group");
            }

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
        dto.setUserId(task.getUserId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}