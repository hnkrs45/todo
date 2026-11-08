package com.app.todo.dto.response;

import com.app.todo.model.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {
    private String id;
    private String title;
    private String description;
    private TodoStatus status;
    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}