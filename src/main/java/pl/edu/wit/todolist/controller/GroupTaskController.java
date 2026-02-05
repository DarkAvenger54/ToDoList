package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.*;
import pl.edu.wit.todolist.service.GroupTaskService;

@RestController
@RequestMapping("/api/groups/{groupId}/tasks")
@RequiredArgsConstructor
public class GroupTaskController {

    private final GroupTaskService groupTaskService;

    // ADMIN/OWNER: назначить задачу участнику
    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto assign(Authentication auth,
                                  @PathVariable Long groupId,
                                  @RequestBody GroupAssignTaskRequestDto dto) {
        return groupTaskService.assignToUser(auth, groupId, dto);
    }

    // ADMIN/OWNER: создать групповую задачу "для всех"
    @PostMapping("/for-all")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto createForAll(Authentication auth,
                                        @PathVariable Long groupId,
                                        @RequestBody GroupCreateTaskForAllRequestDto dto) {
        return groupTaskService.createForAll(auth, groupId, dto);
    }

    // ADMIN/OWNER: поменять статус groupTask
    @PostMapping("/{taskId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(Authentication auth,
                             @PathVariable Long groupId,
                             @PathVariable Long taskId,
                             @RequestBody GroupTaskUpdateStatusRequestDto dto) {
        groupTaskService.updateGroupTaskStatus(auth, groupId, taskId, dto);
    }

    // MEMBER: мои назначенные задачи в группе
    @GetMapping("/mine")
    public Page<TaskResponseDto> mine(Authentication auth,
                                      @PathVariable Long groupId,
                                      @ParameterObject Pageable pageable) {
        return groupTaskService.mine(auth, groupId, pageable);
    }

    // MEMBER: список groupTask "для всех"
    @GetMapping("/for-all/list")
    public Page<TaskResponseDto> forAllList(Authentication auth,
                                            @PathVariable Long groupId,
                                            @ParameterObject Pageable pageable) {
        return groupTaskService.forAll(auth, groupId, pageable);
    }

    // MEMBER: лента visibleInGroup
    @GetMapping("/visible")
    public Page<TaskResponseDto> visible(Authentication auth,
                                         @PathVariable Long groupId,
                                         @ParameterObject Pageable pageable) {
        return groupTaskService.visible(auth, groupId, pageable);
    }
    @PutMapping("/{taskId}")
    public TaskResponseDto update(Authentication auth,
                                  @PathVariable Long groupId,
                                  @PathVariable Long taskId,
                                  @Valid @RequestBody GroupTaskUpdateRequestDto dto) {
        return groupTaskService.updateGroupTask(auth, groupId, taskId, dto);
    }

    // ADMIN/OWNER: удалить групповую задачу (назначенную или for-all)
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth,
                       @PathVariable Long groupId,
                       @PathVariable Long taskId) {
        groupTaskService.deleteGroupTask(auth, groupId, taskId);
    }
}