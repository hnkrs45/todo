package com.app.todo.repository;

import com.app.todo.model.Todo;
import com.app.todo.model.TodoStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends MongoRepository<Todo, String> {
    List<Todo> findByUserId(String userId);
    List<Todo> findByUserIdAndStatus(String userId, TodoStatus status);
    Optional<Todo> findByIdAndUserId(String id, String userId);
    void deleteByIdAndUserId(String id, String userId);
}