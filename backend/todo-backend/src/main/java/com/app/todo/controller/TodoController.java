package com.app.todo.controller;

import com.app.todo.dto.request.TodoRequest;
import com.app.todo.dto.response.ApiResponse;
import com.app.todo.dto.response.TodoResponse;
import com.app.todo.model.TodoStatus;
import com.app.todo.security.CustomUserDetails;
import com.app.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TodoResponse response = todoService.createTodo(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos(
            @RequestParam(required = false) TodoStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<TodoResponse> todos = todoService.getAllTodos(currentUser.getId(), status);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TodoResponse response = todoService.getTodoById(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable String id,
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TodoResponse response = todoService.updateTodo(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTodo(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        todoService.deleteTodo(id, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Todo deleted successfully."));
    }
}