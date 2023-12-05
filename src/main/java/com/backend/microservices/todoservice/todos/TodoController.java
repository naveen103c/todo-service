package com.backend.microservices.todoservice.todos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TodoController {

    @Autowired
    TodoService todoService;

    @GetMapping("/users/{user}/todos")
    public List<Todo> getAllTodosUser(@PathVariable String user){
        return todoService.getAllTodos(user);
    }

    @DeleteMapping("/users/{user}/todos/{id}")
    public Todo deleteTodoByUserId(@PathVariable String user, @PathVariable int id){
        return todoService.deleteTodoByUserId(user,id);
    }

    @PostMapping("/users/{user}/todos")
    public List<Todo> deleteTodoByUserId(@PathVariable String user,@RequestBody Todo todo){
        return todoService.addTodoForUser(user,todo);
    }

    @PutMapping("/users/{user}/todos/{id}")
    public List<Todo> updateTodoByUserId(@PathVariable String user, @PathVariable int id, @RequestBody Todo todo){
        return todoService.updateTodoByUserId(user,id,todo);
    }

    @GetMapping("/users/todos/{id}")
    public Todo getTodoById(@PathVariable int id){
        System.out.println("Here");
        return todoService.getTodoById(id);
    }
}
