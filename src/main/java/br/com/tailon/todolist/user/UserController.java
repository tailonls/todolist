package br.com.tailon.todolist.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Métodos de acesso do HTTP
 * GET - Busca uma info
 * POST - Adiciona um dado/info
 * PUT - Altera um dado/info
 * PATCH - Altera somente uma parte da info
 * DELETE - Apaga uma info
 * <p>
 * http://localhost:8080/
 * http://localhost:8080/h2-console
 **/
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity criarUser(@RequestBody UserModel userModel) {
        var user = this.userRepository.findByUsername(userModel.getUsername());
        if (user != null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existe!");

        var encryptedPassword = BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray());
        userModel.setPassword(encryptedPassword);

        var userCreated = this.userRepository.save(userModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
    }
}