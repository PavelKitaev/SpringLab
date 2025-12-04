package com.example.taskmanager.dto;

public class StatisticsDTO {

    // Общая статистика
    private Long totalUsers;
    private Long totalTasks;
    private Long totalGroups;

    // Статистика по статусам задач
    private Long pendingTasks;
    private Long inProgressTasks;
    private Long completedTasks;

    // Статистика по задачам с группами и без
    private Long tasksWithGroup;
    private Long tasksWithoutGroup;

    // Статистика по группам (топ 5 групп по количеству задач)
    private String topGroups;

    // Статистика по пользователям (топ 5 пользователей по количеству задач)
    private String topUsers;

    // Конструкторы
    public StatisticsDTO() {}

    public StatisticsDTO(Long totalUsers, Long totalTasks, Long totalGroups,
                         Long pendingTasks, Long inProgressTasks, Long completedTasks,
                         Long tasksWithGroup, Long tasksWithoutGroup) {
        this.totalUsers = totalUsers;
        this.totalTasks = totalTasks;
        this.totalGroups = totalGroups;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.tasksWithGroup = tasksWithGroup;
        this.tasksWithoutGroup = tasksWithoutGroup;
    }

    // Геттеры и сеттеры
    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Long totalTasks) { this.totalTasks = totalTasks; }

    public Long getTotalGroups() { return totalGroups; }
    public void setTotalGroups(Long totalGroups) { this.totalGroups = totalGroups; }

    public Long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(Long pendingTasks) { this.pendingTasks = pendingTasks; }

    public Long getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(Long inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public Long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Long completedTasks) { this.completedTasks = completedTasks; }

    public Long getTasksWithGroup() { return tasksWithGroup; }
    public void setTasksWithGroup(Long tasksWithGroup) { this.tasksWithGroup = tasksWithGroup; }

    public Long getTasksWithoutGroup() { return tasksWithoutGroup; }
    public void setTasksWithoutGroup(Long tasksWithoutGroup) { this.tasksWithoutGroup = tasksWithoutGroup; }

    public String getTopGroups() { return topGroups; }
    public void setTopGroups(String topGroups) { this.topGroups = topGroups; }

    public String getTopUsers() { return topUsers; }
    public void setTopUsers(String topUsers) { this.topUsers = topUsers; }
}