
package com.myworld.app.controller;

import com.myworld.app.entity.Todo;
import com.myworld.app.entity.Todo.Priority;
import com.myworld.app.repository.MemberRepository;
import com.myworld.app.service.TodoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;
    private final MemberRepository memberRepository;

    public TodoController(
            TodoService todoService,
            MemberRepository memberRepository
    ) {
        this.todoService = todoService;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public List<TodoResponse> getMyTodos(
            HttpServletRequest request
    ) {
        Long memberId = getLoginMemberId(request);

        return todoService.getMyTodos(memberId)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoResponse createTodo(
            @RequestBody TodoRequest body,
            HttpServletRequest request
    ) {
        Long memberId = getLoginMemberId(request);

        Todo todo = todoService.create(
                memberId,
                body.title(),
                body.dueDate(),
                body.priority()
        );

        return TodoResponse.from(todo);
    }

    @PutMapping("/{todoId}")
    public TodoResponse updateTodo(
            @PathVariable Long todoId,
            @RequestBody TodoRequest body,
            HttpServletRequest request
    ) {
        Long memberId = getLoginMemberId(request);

        Todo todo = todoService.update(
                memberId,
                todoId,
                body.title(),
                body.dueDate(),
                body.priority()
        );

        return TodoResponse.from(todo);
    }

    @PatchMapping("/{todoId}/completed")
    public TodoResponse changeCompleted(
            @PathVariable Long todoId,
            @RequestBody CompletionRequest body,
            HttpServletRequest request
    ) {
        Long memberId = getLoginMemberId(request);

        if (body.completed() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "완료 여부를 입력해 주세요."
            );
        }

        Todo todo = todoService.changeCompleted(
                memberId,
                todoId,
                body.completed()
        );

        return TodoResponse.from(todo);
    }

    @DeleteMapping("/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(
            @PathVariable Long todoId,
            HttpServletRequest request
    ) {
        Long memberId = getLoginMemberId(request);

        todoService.delete(memberId, todoId);
    }

    private Long getLoginMemberId(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw unauthorized();
        }

        Object value = session.getAttribute("loginMemberId");

        if (!(value instanceof Long memberId)) {
            throw unauthorized();
        }

        if (!memberRepository.existsById(memberId)) {
            session.invalidate();
            throw unauthorized();
        }

        return memberId;
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "로그인이 필요합니다."
        );
    }

    public record TodoRequest(
            String title,
            LocalDate dueDate,
            Priority priority
    ) {
    }

    public record CompletionRequest(
            Boolean completed
    ) {
    }

    public record TodoResponse(
            Long id,
            String title,
            LocalDate dueDate,
            Priority priority,
            boolean completed,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static TodoResponse from(Todo todo) {
            return new TodoResponse(
                    todo.getId(),
                    todo.getTitle(),
                    todo.getDueDate(),
                    todo.getPriority(),
                    todo.isCompleted(),
                    todo.getCreatedAt(),
                    todo.getUpdatedAt()
            );
        }
    }
}
