package com.example.taskmanager.service;

import com.example.taskmanager.dto.GroupStatisticsDTO;
import com.example.taskmanager.dto.StatisticsDTO;
import com.example.taskmanager.dto.UserStatisticsDTO;
import com.example.taskmanager.repository.StatisticsRepository;
import com.example.taskmanager.config.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    @Autowired
    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Получение общей статистики
     */
    public StatisticsDTO getStatistics() {
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdmin();

        if (isAdmin) {
            // Админ получает полную статистику
            StatisticsDTO stats = statisticsRepository.getGeneralStatistics();
            enrichStatisticsWithTopData(stats);
            return stats;
        } else {
            // Обычный пользователь получает только свою статистику
            StatisticsDTO stats = statisticsRepository.getPersonalizedStatistics(userId, false);
            if (stats != null) {
                stats.setTopGroups("Доступно только для администратора");
                stats.setTopUsers("Доступно только для администратора");
            }
            return stats;
        }
    }

    /**
     * Получение статистики по группам
     */
    public List<GroupStatisticsDTO> getGroupStatistics() {
        boolean isAdmin = isAdmin();
        List<GroupStatisticsDTO> groupStats = statisticsRepository.getGroupStatistics();

        if (!isAdmin) {
            // Фильтруем только группы текущего пользователя
            Long userId = getCurrentUserId();
            // В реальном приложении нужно добавить фильтрацию по владельцу группы
            // Пока оставляем как есть
            return groupStats;
        }

        return groupStats;
    }

    /**
     * Получение статистики по пользователям (только для администратора)
     */
    public List<UserStatisticsDTO> getUserStatistics() {
        if (!isAdmin()) {
            throw new SecurityException("Доступно только для администратора");
        }

        return statisticsRepository.getUserStatistics();
    }

    /**
     * Получение топ-5 групп (только для администратора)
     */
    public List<GroupStatisticsDTO> getTopGroups() {
        if (!isAdmin()) {
            throw new SecurityException("Доступно только для администратора");
        }

        List<Object[]> results = statisticsRepository.getTopGroupsNative();
        return results.stream()
                .map(row -> new GroupStatisticsDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        0L,  // completedTasks - можно добавить отдельный запрос
                        0L   // pendingTasks - можно добавить отдельный запрос
                ))
                .collect(Collectors.toList());
    }

    /**
     * Получение топ-5 пользователей (только для администратора)
     */
    public List<UserStatisticsDTO> getTopUsers() {
        if (!isAdmin()) {
            throw new SecurityException("Доступно только для администратора");
        }

        List<Object[]> results = statisticsRepository.getTopUsersNative();
        return results.stream()
                .map(row -> new UserStatisticsDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        0L  // completedTasks - можно добавить отдельный запрос
                ))
                .collect(Collectors.toList());
    }

    /**
     * Обогащение статистики топ-данными
     */
    private void enrichStatisticsWithTopData(StatisticsDTO stats) {
        if (stats == null) return;

        try {
            // Получаем топ-5 групп
            List<GroupStatisticsDTO> topGroups = getTopGroups();
            String groupsStr = topGroups.stream()
                    .limit(5)  // Берем только топ-5
                    .map(g -> String.format("%s (%d задач)", g.getGroupName(), g.getTaskCount()))
                    .collect(Collectors.joining(", "));
            stats.setTopGroups(groupsStr.isEmpty() ? "Нет данных" : groupsStr);

            // Получаем топ-5 пользователей
            List<UserStatisticsDTO> topUsers = getTopUsers();
            String usersStr = topUsers.stream()
                    .limit(5)  // Берем только топ-5
                    .map(u -> String.format("%s (%d задач)", u.getUsername(), u.getTaskCount()))
                    .collect(Collectors.joining(", "));
            stats.setTopUsers(usersStr.isEmpty() ? "Нет данных" : usersStr);

        } catch (Exception e) {
            stats.setTopGroups("Ошибка получения данных");
            stats.setTopUsers("Ошибка получения данных");
        }
    }

    /**
     * Получение расширенной статистики (все данные в одном объекте)
     */
    public StatisticsDTO getExtendedStatistics() {
        StatisticsDTO stats = getStatistics();

        if (isAdmin()) {
            // Для администратора добавляем дополнительные данные
            // Можно добавить дополнительные вычисления
        }

        return stats;
    }

    /**
     * Простая статистика для дашборда
     */
    public StatisticsDTO getDashboardStatistics() {
        StatisticsDTO stats = getStatistics();

        // Добавляем дополнительную информацию для дашборда
        if (stats.getTotalTasks() > 0) {
            double completionRate = stats.getCompletedTasks().doubleValue() / stats.getTotalTasks().doubleValue() * 100;
            // Можно добавить это поле в DTO или использовать существующие
        }

        return stats;
    }
}