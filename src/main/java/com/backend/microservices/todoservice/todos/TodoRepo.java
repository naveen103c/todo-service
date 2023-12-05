package com.backend.microservices.todoservice.todos;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TodoRepo {
    static List<Todo> todoList=new ArrayList<>();
    static {
        Todo todo1 = new Todo(1,"rashmi", "Visit India", false, new Date());
        Todo todo2 = new Todo(2, "naveen","Play with Navya", false, new Date());
        Todo todo3 = new Todo(3, "naveen","Learn ANgular", false, new Date());
        todoList.add(todo1);todoList.add(todo2);todoList.add(todo3);
    }
    public List<Todo> findAll() {
        return todoList;
    }
    public Todo findById(int id) {
        Optional<Todo> first = todoList.stream().filter(t -> t.getId() == id).findFirst();
        if(first.isPresent()){
            return first.get();
        }
        return null;
    }

    public List<Todo> findAllUser(String user) {
        return todoList.stream().filter(t->t.getUser().equals(user)).collect(Collectors.toList());
    }

    public Todo deleteByIdAndUser(String user, int id) {
        Optional<Todo> todo = todoList.stream().filter(t -> t.getUser().equals((user))).filter(t -> t.getId() == id).findFirst();
        if(todo.isPresent()){
            todoList.remove(todo);
            return todo.get();
        }
        return null;
    }

    public List<Todo> addTodoForUser(String user, Todo todo) {
        todo.setUser(user);
        todoList.add(todo);
        return todoList;
    }

    public List<Todo> updateTodoByUserId(String user, int id, Todo todo) {
        Optional<Todo> oldTodo = todoList.stream().filter(t -> t.getUser().equals((user))).filter(t -> t.getId() == id).findFirst();
        if(oldTodo.isPresent()){
            todoList.remove(oldTodo.get());
            oldTodo.get().setUser(user);
            oldTodo.get().setDescription(todo.getDescription());
            oldTodo.get().setTargetDate(todo.getTargetDate());
            oldTodo.get().setCompleted(todo.isCompleted());
            todoList.add(oldTodo.get());
        }
        return todoList;
    }
}
