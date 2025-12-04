package com.example.taskmanager.dto;

public class UserStatisticsDTO {

    private Long userId;
    private String username;
    private Long taskCount;
    private Long groupCount;
    private Long completedTasks;

    // Конструкторы
    public UserStatisticsDTO() {}

    public UserStatisticsDTO(Long userId, String username, Long taskCount,
                             Long groupCount, Long completedTasks) {
        this.userId = userId;
        this.username = username;
        this.taskCount = taskCount;
        this.groupCount = groupCount;
        this.completedTasks = completedTasks;
    }

    // Геттеры и сеттеры
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getTaskCount() { return taskCount; }
    public void setTaskCount(Long taskCount) { this.taskCount = taskCount; }

    public Long getGroupCount() { return groupCount; }
    public void setGroupCount(Long groupCount) { this.groupCount = groupCount; }

    public Long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Long completedTasks) { this.completedTasks = completedTasks; }
}