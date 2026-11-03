
package com.myworld.app.service;

import com.myworld.app.entity.Todo;
import com.myworld.app.entity.Todo.Priority;
import com.myworld.app.repository.TodoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getMyTodos(Long memberId) {
        return todoRepository
                .findByMemberIdOrderByCreatedAtDescIdDesc(
                        memberId
                );
    }

    @Transactional
    public Todo create(
            Long memberId,
            String title,
            LocalDate dueDate,
            Priority priority
    ) {
        Todo todo = new Todo(
                memberId,
                validateTitle(title),
                dueDate,
                validatePriority(priority)
        );

        return todoRepository.save(todo);
    }

    @Transactional
    public Todo update(
            Long memberId,
            Long todoId,
            String title,
            LocalDate dueDate,
            Priority priority
    ) {
        Todo todo = findOwnedTodo(memberId, todoId);

        todo.update(
                validateTitle(title),
                dueDate,
                validatePriority(priority)
        );

        return todo;
    }

    @Transactional
    public Todo changeCompleted(
            Long memberId,
            Long todoId,
            boolean completed
    ) {
        Todo todo = findOwnedTodo(memberId, todoId);
        todo.setCompleted(completed);

        return todo;
    }

    @Transactional
    public void delete(Long memberId, Long todoId) {
        Todo todo = findOwnedTodo(memberId, todoId);
        todoRepository.delete(todo);
    }

    private Todo findOwnedTodo(
            Long memberId,
            Long todoId
    ) {
        return todoRepository
                .findByIdAndMemberId(todoId, memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "할 일을 찾을 수 없습니다."
                        )
                );
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "할 일을 입력해 주세요."
            );
        }

        String value = title.trim();

        if (value.length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "할 일은 150자 이하로 입력해 주세요."
            );
        }

        return value;
    }

    private Priority validatePriority(Priority priority) {
        return priority == null
                ? Priority.NORMAL
                : priority;
    }
}
