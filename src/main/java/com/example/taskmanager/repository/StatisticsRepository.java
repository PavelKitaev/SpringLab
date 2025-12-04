package com.example.taskmanager.repository;

import com.example.taskmanager.dto.GroupStatisticsDTO;
import com.example.taskmanager.dto.StatisticsDTO;
import com.example.taskmanager.dto.UserStatisticsDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository {

    /**
     * Получение общей статистики (используем JPQL)
     */
    @Query("SELECT NEW com.example.taskmanager.dto.StatisticsDTO(" +
            "(SELECT COUNT(u) FROM User u), " +
            "(SELECT COUNT(t) FROM Task t), " +
            "(SELECT COUNT(g) FROM Group g), " +
            "(SELECT COUNT(t) FROM Task t WHERE t.status = 'PENDING'), " +
            "(SELECT COUNT(t) FROM Task t WHERE t.status = 'IN_PROGRESS'), " +
            "(SELECT COUNT(t) FROM Task t WHERE t.status = 'COMPLETED'), " +
            "(SELECT COUNT(t) FROM Task t WHERE t.group IS NOT NULL), " +
            "(SELECT COUNT(t) FROM Task t WHERE t.group IS NULL)" +
            ")")
    StatisticsDTO getGeneralStatistics();

    /**
     * Получение статистики по группам с количеством задач (JPQL)
     * Группировка по группам с подсчетом задач
     */
    @Query("SELECT NEW com.example.taskmanager.dto.GroupStatisticsDTO(" +
            "g.id, g.name, u.username, " +
            "COUNT(t), " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.status = 'PENDING' THEN 1 ELSE 0 END)" +
            ") " +
            "FROM Group g " +
            "LEFT JOIN g.tasks t " +
            "JOIN g.user u " +
            "GROUP BY g.id, g.name, u.username " +
            "ORDER BY COUNT(t) DESC")
    List<GroupStatisticsDTO> getGroupStatistics();

    /**
     * Получение статистики по пользователям (JPQL)
     * Группировка по пользователям с подсчетом задач и групп
     */
    @Query("SELECT NEW com.example.taskmanager.dto.UserStatisticsDTO(" +
            "u.id, u.username, " +
            "COUNT(t), " +
            "COUNT(DISTINCT g), " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END)" +
            ") " +
            "FROM User u " +
            "LEFT JOIN Task t ON t.user.id = u.id " +
            "LEFT JOIN Group g ON g.user.id = u.id " +
            "GROUP BY u.id, u.username " +
            "ORDER BY COUNT(t) DESC")
    List<UserStatisticsDTO> getUserStatistics();

    /**
     * Получение топ-5 групп по количеству задач (Native Query)
     */
    @Query(value =
            "SELECT g.id, g.name, u.username, COUNT(t.id) as task_count " +
                    "FROM task_groups g " +
                    "LEFT JOIN tasks t ON t.group_id = g.id " +
                    "JOIN users u ON g.user_id = u.id " +
                    "GROUP BY g.id, g.name, u.username " +
                    "ORDER BY task_count DESC " +
                    "LIMIT 5",
            nativeQuery = true)
    List<Object[]> getTopGroupsNative();

    /**
     * Получение топ-5 пользователей по количеству задач (Native Query)
     */
    @Query(value =
            "SELECT u.id, u.username, COUNT(t.id) as task_count, " +
                    "COUNT(DISTINCT g.id) as group_count " +
                    "FROM users u " +
                    "LEFT JOIN tasks t ON t.user_id = u.id " +
                    "LEFT JOIN task_groups g ON g.user_id = u.id " +
                    "GROUP BY u.id, u.username " +
                    "ORDER BY task_count DESC " +
                    "LIMIT 5",
            nativeQuery = true)
    List<Object[]> getTopUsersNative();

    /**
     * Получение статистики для администратора (все данные)
     * или для обычного пользователя (только его данные) - JPQL с параметром
     */
    @Query("SELECT NEW com.example.taskmanager.dto.StatisticsDTO(" +
            "COUNT(DISTINCT u), " +
            "COUNT(t), " +
            "COUNT(DISTINCT g), " +
            "SUM(CASE WHEN t.status = 'PENDING' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.group IS NOT NULL THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.group IS NULL THEN 1 ELSE 0 END)" +
            ") " +
            "FROM User u " +
            "LEFT JOIN Task t ON (:isAdmin = true OR t.user.id = :userId) " +
            "LEFT JOIN Group g ON (:isAdmin = true OR g.user.id = :userId) " +
            "WHERE (:isAdmin = true OR u.id = :userId)")
    StatisticsDTO getPersonalizedStatistics(@Param("userId") Long userId,
                                            @Param("isAdmin") boolean isAdmin);
}