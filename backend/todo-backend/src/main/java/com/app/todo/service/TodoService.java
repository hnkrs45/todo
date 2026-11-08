package com.app.todo.service;

import com.app.todo.dto.request.TodoRequest;
import com.app.todo.dto.response.TodoResponse;
import com.app.todo.model.Todo;
import com.app.todo.model.TodoStatus;
import com.app.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoResponse createTodo(TodoRequest request, String userId) {
        Todo todo = Todo.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TodoStatus.PENDING)
                .dueDate(request.getDueDate())
                .build();

        Todo saved = todoRepository.save(todo);
        return mapToResponse(saved);
    }

    public List<TodoResponse> getAllTodos(String userId, TodoStatus status) {
        List<Todo> todos = (status != null)
                ? todoRepository.findByUserIdAndStatus(userId, status)
                : todoRepository.findByUserId(userId);

        return todos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TodoResponse getTodoById(String id, String userId) {
        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        return mapToResponse(todo);
    }

    public TodoResponse updateTodo(String id, TodoRequest request, String userId) {
        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Todo not found or unauthorized"));

        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            todo.setDueDate(request.getDueDate());
        }

        Todo updated = todoRepository.save(todo);
        return mapToResponse(updated);
    }

    public void deleteTodo(String id, String userId) {
        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Todo not found or unauthorized"));
        todoRepository.delete(todo);
    }

    private TodoResponse mapToResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .status(todo.getStatus())
                .dueDate(todo.getDueDate())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}