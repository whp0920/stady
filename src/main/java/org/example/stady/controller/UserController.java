package org.example.stady.controller;

import org.example.stady.common.Result;
import org.example.stady.entity.User;
import org.example.stady.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) {
        this.service = service;
    }
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        return service.register(user);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        return service.login(user);
    }
}
