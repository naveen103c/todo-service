package com.backend.microservices.todoservice.todos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    TodoRepo todoRepo;

    public List<Todo> getAllTodos(String user) {
        return todoRepo.findAllUser(user);
    }

    public Todo getTodoById(int id) {
        return todoRepo.findById(id);
    }

    public Todo deleteTodoByUserId(String user, int id) {
        return todoRepo.deleteByIdAndUser(user,id);
    }

    public List<Todo> addTodoForUser(String user, Todo todo) {
        return todoRepo.addTodoForUser(user,todo);
    }

    public List<Todo> updateTodoByUserId(String user, int id, Todo todo) {
        return todoRepo.updateTodoByUserId(user,id,todo);
    }
}
