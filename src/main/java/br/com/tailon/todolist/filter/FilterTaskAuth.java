package br.com.tailon.todolist.filter;


import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.tailon.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getServletPath().startsWith("/tasks/")) {

            var authEncoded = request.getHeader("Authorization").substring("Basic".length()).trim(); // Pegar os dados de user e senha
            String[] authDecoded = new String(Base64.getDecoder().decode(authEncoded)).split(":");

            String userName = authDecoded[0];
            String password = authDecoded[1];

            var userFromBase = this.userRepository.findByUsername(userName);
            if (userFromBase == null) {
                response.sendError(401);

            } else {
                var isPasswordCorrect = BCrypt.verifyer().verify(password.toCharArray(), userFromBase.getPassword());

                if (isPasswordCorrect.verified) {
                    request.setAttribute("idUser", userFromBase.getId());
                    filterChain.doFilter(request, response);

                } else {
                    response.sendError(401);
                }
            }

        } else {
            filterChain.doFilter(request, response);
        }
    }
}
