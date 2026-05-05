package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.entity.User;
import com.atguigu.java.ai.langchain4j.mapper.UserMapper;
import com.atguigu.java.ai.langchain4j.dto.WebSocketResponseDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public WebSocketResponseDTO login(@RequestParam("username") String username, @RequestParam("password") String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getPassword, password);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            return WebSocketResponseDTO.success(user);
        }
        return WebSocketResponseDTO.error("用户名或密码错误");
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public WebSocketResponseDTO register(@RequestBody User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        return WebSocketResponseDTO.success(user.getId());
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public WebSocketResponseDTO getUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null); // 不返回密码
            return WebSocketResponseDTO.success(user);
        }
        return WebSocketResponseDTO.error("用户不存在");
    }
}