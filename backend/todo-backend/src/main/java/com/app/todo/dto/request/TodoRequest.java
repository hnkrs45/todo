package com.app.todo.dto.request;

import com.app.todo.model.TodoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class TodoRequest {

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    private String description;

    private TodoStatus status;

    private Instant dueDate;
}