package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateGroupDTO;
import com.example.taskmanager.dto.GroupDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Group;
import com.example.taskmanager.model.Task;
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
public class GroupService {

    private final GroupRepository groupRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        TaskRepository taskRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.taskRepository = taskRepository;
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

    public GroupDTO createGroup(CreateGroupDTO groupDTO) {
        User currentUser = getCurrentUser();

        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        group.setUser(currentUser);

        Group savedGroup = groupRepository.save(group);
        return convertToDTO(savedGroup);
    }

    public List<GroupDTO> getAllGroups() {
        User currentUser = getCurrentUser();

        if (isAdmin()) {
            // Админ видит все группы
            return groupRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            // Обычный пользователь видит только свои группы
            return groupRepository.findByUserId(currentUser.getId()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    public Optional<GroupDTO> getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view this group");
        }

        return Optional.of(convertToDTO(group));
    }

    public Optional<GroupDTO> getGroupWithTasks(Long id) {
        Group group = groupRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view this group");
        }

        return Optional.of(convertToDTOWithTasks(group));
    }

    public GroupDTO updateGroup(Long id, CreateGroupDTO groupDTO) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to update this group");
        }

        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());

        Group updatedGroup = groupRepository.save(group);
        return convertToDTO(updatedGroup);
    }

    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to delete this group");
        }

        groupRepository.deleteById(id);
    }

    public GroupDTO addTaskToGroup(Long groupId, Long taskId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User currentUser = getCurrentUser();

        // Проверяем права на группу
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to modify this group");
        }

        // Проверяем права на задачу
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }

        task.setGroup(group);
        taskRepository.save(task);

        return convertToDTOWithTasks(group);
    }

    public GroupDTO removeTaskFromGroup(Long groupId, Long taskId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User currentUser = getCurrentUser();

        // Проверяем права на группу
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to modify this group");
        }

        // Проверяем права на задачу
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }

        if (task.getGroup() != null && task.getGroup().getId().equals(groupId)) {
            task.setGroup(null);
            taskRepository.save(task);
        }

        return convertToDTOWithTasks(group);
    }

    public List<TaskDTO> getTasksByGroupId(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        User currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!group.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view this group's tasks");
        }

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
        dto.setUserId(group.getUserId());
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
        dto.setUserId(task.getUserId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}