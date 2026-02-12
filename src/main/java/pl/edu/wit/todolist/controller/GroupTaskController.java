package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.group.GroupAssignTaskRequestDto;
import pl.edu.wit.todolist.dto.group.GroupCreateTaskForAllRequestDto;
import pl.edu.wit.todolist.dto.group.GroupTaskUpdateRequestDto;
import pl.edu.wit.todolist.dto.group.GroupTaskUpdateStatusRequestDto;
import pl.edu.wit.todolist.dto.task.TaskResponseDto;
import pl.edu.wit.todolist.service.GroupTaskService;

@RestController
@RequestMapping("/api/groups/{groupId}/tasks")
@RequiredArgsConstructor
public class GroupTaskController {

    private final GroupTaskService groupTaskService;

    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto assign(Authentication auth,
                                  @PathVariable Long groupId,
                                  @RequestBody GroupAssignTaskRequestDto dto) {
        return groupTaskService.assignToUser(auth, groupId, dto);
    }

    @PostMapping("/for-all")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto createForAll(Authentication auth,
                                        @PathVariable Long groupId,
                                        @RequestBody GroupCreateTaskForAllRequestDto dto) {
        return groupTaskService.createForAll(auth, groupId, dto);
    }

    @PostMapping("/{taskId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(Authentication auth,
                             @PathVariable Long groupId,
                             @PathVariable Long taskId,
                             @RequestBody GroupTaskUpdateStatusRequestDto dto) {
        groupTaskService.updateGroupTaskStatus(auth, groupId, taskId, dto);
    }

    @GetMapping("/mine")
    public Page<TaskResponseDto> mine(Authentication auth,
                                      @PathVariable Long groupId,
                                      @ParameterObject Pageable pageable) {
        return groupTaskService.mine(auth, groupId, pageable);
    }

    @GetMapping("/for-all/list")
    public Page<TaskResponseDto> forAllList(Authentication auth,
                                            @PathVariable Long groupId,
                                            @ParameterObject Pageable pageable) {
        return groupTaskService.forAll(auth, groupId, pageable);
    }

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

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth,
                       @PathVariable Long groupId,
                       @PathVariable Long taskId) {
        groupTaskService.deleteGroupTask(auth, groupId, taskId);
    }
}