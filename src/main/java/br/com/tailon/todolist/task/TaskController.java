package br.com.tailon.todolist.task;

import br.com.tailon.todolist.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * http://localhost:8080/
 * http://localhost:8080/h2-console
 **/
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var user = this.taskRepository.findByTitle(taskModel.getTitle());
        if (user != null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task já existe!");

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/término da tarefa deve ser maior que a data atual!");

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de término da tarefa deve ser maior que a data de início!");

        taskModel.setIdUser((UUID) request.getAttribute("idUser")); // Setando idUser que veio da base no doFilter via HttpServletRequest
        var taskCreated = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        return this.taskRepository.findByIdUser((UUID) idUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada!");

        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Essa tarefa pertence a outro usuário e não pode ser alterada por este!");

        Util.copyNonNullProperties(taskModel, task);
        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }
}