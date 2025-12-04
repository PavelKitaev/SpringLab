package com.example.taskmanager.controller;

import com.example.taskmanager.dto.GroupStatisticsDTO;
import com.example.taskmanager.dto.StatisticsDTO;
import com.example.taskmanager.dto.UserStatisticsDTO;
import com.example.taskmanager.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics", description = "Statistics endpoints")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    @Operation(summary = "Get general statistics")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        StatisticsDTO statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/groups")
    @Operation(summary = "Get statistics by groups")
    public ResponseEntity<List<GroupStatisticsDTO>> getGroupStatistics() {
        List<GroupStatisticsDTO> statistics = statisticsService.getGroupStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics by users (admin only)")
    public ResponseEntity<List<UserStatisticsDTO>> getUserStatistics() {
        List<UserStatisticsDTO> statistics = statisticsService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/top-groups")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top 5 groups by task count (admin only)")
    public ResponseEntity<List<GroupStatisticsDTO>> getTopGroups() {
        List<GroupStatisticsDTO> topGroups = statisticsService.getTopGroups();
        return ResponseEntity.ok(topGroups);
    }

    @GetMapping("/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top 5 users by task count (admin only)")
    public ResponseEntity<List<UserStatisticsDTO>> getTopUsers() {
        List<UserStatisticsDTO> topUsers = statisticsService.getTopUsers();
        return ResponseEntity.ok(topUsers);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<StatisticsDTO> getDashboard() {
        StatisticsDTO statistics = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }
}