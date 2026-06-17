package org.example.stady.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.stady.common.Result;
import org.example.stady.entity.User;
import org.example.stady.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class UserService {

    private final UserMapper mapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // JWT 印章（你自己的密码，别人不知道）
    private static final SecretKey KEY = Keys.hmacShaKeyFor(
        "this-is-my-secret-key-for-jwt-1234567890".getBytes()
    );

    public UserService(UserMapper mapper) {
        this.mapper = mapper;
    }

    // ============ 注册 ============
    public Result<Void> register(User user) {
        // 0. 空值校验
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Result.fail("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.fail("密码不能为空");
        }

        // 1. 查数据库：用户名有没有被占用？
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (mapper.selectCount(wrapper) > 0) {
            return Result.fail("用户名已存在");
        }

        // 2. 加密密码，存进去
        user.setPassword(encoder.encode(user.getPassword()));
        mapper.insert(user);
        return Result.ok();
    }

    // ============ 登录 ============
    public Result<String> login(User user) {
        // 0. 空值校验
        if (user.getUsername() == null || user.getUsername().isBlank()
            || user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.fail("用户名或密码错误");
        }

        // 1. 查用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        User dbUser = mapper.selectOne(wrapper);
        if (dbUser == null) {
            return Result.fail("用户名或密码错误");
        }

        // 2. 比对密码
        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        // 3. 发 Token（电子学生卡）
        String token = Jwts.builder()
            .subject(dbUser.getId().toString())
            .claim("username", dbUser.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 7200000)) // 2小时
            .signWith(KEY)
            .compact();

        return Result.ok(token);
    }
}
