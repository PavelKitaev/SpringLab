package com.example.taskmanager;

import com.example.taskmanager.model.*;
import com.example.taskmanager.repository.GroupRepository;
import com.example.taskmanager.repository.RoleRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           GroupRepository groupRepository,
                           TaskRepository taskRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting data initialization...");

        try {
            initializeRoles();
            initializeUsers();
            logger.info("Data initialization completed successfully!");
        } catch (Exception e) {
            logger.error("Error during data initialization: ", e);
        }
    }

    private void initializeRoles() {
        logger.info("Initializing roles...");

        if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty()) {
            Role userRole = new Role(RoleName.ROLE_USER, "Regular user");
            roleRepository.save(userRole);
            logger.info("Created USER role");
        }

        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role(RoleName.ROLE_ADMIN, "Administrator");
            roleRepository.save(adminRole);
            logger.info("Created ADMIN role");
        }
    }

    private void initializeUsers() {
        logger.info("Initializing users...");

        if (userRepository.count() == 0) {
            // Создаем администратора (createdAt установится автоматически через @PrePersist)
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found")));
            adminRoles.add(roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User role not found")));

            admin.setRoles(adminRoles);
            userRepository.save(admin);
            logger.info("Created admin user: admin / admin123");

            // Создаем обычного пользователя
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEnabled(true);

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User role not found")));

            user.setRoles(userRoles);
            userRepository.save(user);
            logger.info("Created regular user: user / user123");

            // Создаем тестовые данные для пользователей
            initializeTestData(admin, user);
        } else {
            logger.info("Users already exist, skipping creation");
        }
    }

    private void initializeTestData(User admin, User user) {
        logger.info("Initializing test data...");

        // Создаем группы
        Group workGroup = new Group("Рабочие задачи", "Задачи связанные с работой", admin);
        groupRepository.save(workGroup);

        Group personalGroup = new Group("Личные задачи", "Личные дела и планы", user);
        groupRepository.save(personalGroup);

        // Создаем задачи для администратора
        Task task1 = new Task("Подготовить отчет", "Ежеквартальный отчет для руководства", admin, workGroup);
        task1.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task1);

        Task task2 = new Task("Совещание с командой", "Обсуждение планов на следующую неделю", admin, workGroup);
        taskRepository.save(task2);

        // Создаем задачи для обычного пользователя
        Task task3 = new Task("Купить продукты", "Список продуктов на неделю", user, personalGroup);
        taskRepository.save(task3);

        Task task4 = new Task("Заняться спортом", "Тренировка в спортзале", user, personalGroup);
        task4.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task4);

        // Задачи без групп
        Task task5 = new Task("Изучить Spring Security", "Разобраться с аутентификацией и авторизацией", admin, null);
        taskRepository.save(task5);

        Task task6 = new Task("Задача без группы", "Эта задача не принадлежит ни к одной группе", user, null);
        taskRepository.save(task6);

        logger.info("Created 2 groups and 6 tasks for testing");
    }
}