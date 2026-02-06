package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.entity.TaskEntity;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskDueSoonNotifierService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Value("${app.notifications.due-soon.minutes:60}")
    private long dueSoonMinutes;

    @Scheduled(fixedDelayString = "${app.notifications.due-soon.poll-ms:60000}")
    @Transactional
    public void notifyDueSoonTasks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plusMinutes(dueSoonMinutes);

        List<TaskEntity> tasks = taskRepository.findTasksDueSoonNotNotified(
                now, to, NotificationType.TASK_DUE_SOON
        );

        for (TaskEntity t : tasks) {
            String when = t.getDueAt().toString();

            notificationService.notifyUser(
                    t.getOwner(),
                    NotificationType.TASK_DUE_SOON,
                    "Task due soon",
                    "Task \"" + t.getTitle() + "\" is due at " + when,
                    t.getId(),
                    true
            );
        }
    }
}
