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

    @Value("${app.notifications.due-soon.minutes-secondary:5}")
    private long dueSoonMinutesSecondary;

    @Scheduled(fixedDelayString = "${app.notifications.due-soon.poll-ms:60000}")
    @Transactional
    public void notifyDueSoonTasks() {
        LocalDateTime now = LocalDateTime.now();

        notifyDueSoonWindow(now, dueSoonMinutes, NotificationType.TASK_DUE_SOON);
        notifyDueSoonWindow(now, dueSoonMinutesSecondary, NotificationType.TASK_DUE_SOON_5);
    }

    private void notifyDueSoonWindow(LocalDateTime now, long minutes, NotificationType type) {
        if (minutes <= 0) {
            return;
        }
        LocalDateTime to = now.plusMinutes(minutes);

        List<TaskEntity> tasks = taskRepository.findTasksDueSoonNotNotified(
                now, to, type
        );

        for (TaskEntity t : tasks) {
            String when = t.getDueAt().toString();

            notificationService.notifyUser(
                    t.getOwner(),
                    type,
                    "Task due soon",
                    "Task \"" + t.getTitle() + "\" is due in " + minutes + " minutes at " + when,
                    t.getId(),
                    true
            );
        }
    }
}
