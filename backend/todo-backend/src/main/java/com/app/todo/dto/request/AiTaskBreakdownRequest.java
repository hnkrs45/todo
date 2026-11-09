package com.app.todo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiTaskBreakdownRequest {

    @NotBlank(message = "Prompt/goal cannot be blank")
    private String goal;

    private int numberOfTasks = 5;
}