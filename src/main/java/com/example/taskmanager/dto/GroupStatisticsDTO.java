package com.example.taskmanager.dto;

public class GroupStatisticsDTO {

    private Long groupId;
    private String groupName;
    private String ownerUsername;
    private Long taskCount;
    private Long completedTasks;
    private Long pendingTasks;

    // Конструкторы
    public GroupStatisticsDTO() {}

    public GroupStatisticsDTO(Long groupId, String groupName, String ownerUsername,
                              Long taskCount, Long completedTasks, Long pendingTasks) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.ownerUsername = ownerUsername;
        this.taskCount = taskCount;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
    }

    // Геттеры и сеттеры
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Long getTaskCount() { return taskCount; }
    public void setTaskCount(Long taskCount) { this.taskCount = taskCount; }

    public Long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Long completedTasks) { this.completedTasks = completedTasks; }

    public Long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(Long pendingTasks) { this.pendingTasks = pendingTasks; }
}