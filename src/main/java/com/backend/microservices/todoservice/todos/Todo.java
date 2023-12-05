package com.backend.microservices.todoservice.todos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {
    private int id;
    private String user;
    private String description;
    private boolean isCompleted;
    private Date targetDate;
}
