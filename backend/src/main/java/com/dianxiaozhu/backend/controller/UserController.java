package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.constants.StatusCode;
import com.dianxiaozhu.backend.dto.ApiResponse;
import com.dianxiaozhu.backend.dto.LoginRequest;
import com.dianxiaozhu.backend.dto.LoginResponse;
import com.dianxiaozhu.backend.dto.UserDTO;
import com.dianxiaozhu.backend.entity.User;
import com.dianxiaozhu.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (userOptional.isPresent()) {
            // In a real application, you would generate a proper JWT token here.
            String token = "dummy-jwt-token-for-" + userOptional.get().getUsername();
            LoginResponse loginResponse = new LoginResponse(token);
            return ResponseEntity.ok(ApiResponse.success(loginResponse));
        }
        return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.LOGIN_ERROR, "用户名或密码错误"));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Object>> getInfo(@RequestHeader("Authorization") String authHeader) {
        // Extract token from Authorization header (Bearer token)
        String token = authHeader.replace("Bearer ", "");
        
        // In a real application, you would validate the token and get actual user info
        // For now, return mock user info based on token
        String username = token.replace("dummy-jwt-token-for-", "");
        
        // Create user info response
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("roles", java.util.Arrays.asList("admin"));
        userInfo.put("name", username);
        userInfo.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestHeader("Authorization") String authHeader) {
        // Extract token from Authorization header (Bearer token)
        String token = authHeader.replace("Bearer ", "");
        // In a real application, you would invalidate the token
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @GetMapping(value = "/test-password/check", produces = "text/plain")
    public ResponseEntity<String> testPassword(@RequestParam String password) {
        return ResponseEntity.ok("Password received: " + password);
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        
        // 调用服务获取用户列表
        Page<User> userPage = userService.findUsers(username, email, phone, role, status, pageable);
        
        // 转换为DTO
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("total", userPage.getTotalElements());
        response.put("list", userDTOs);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserDetail(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            UserDTO userDTO = convertToDTO(userOpt.get());
            return ResponseEntity.ok(ApiResponse.success(userDTO));
        }
        return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.USER_NOT_FOUND, "用户不存在"));
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        try {
            User user = convertToEntity(userDTO);
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(savedUser)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.OPERATION_FAILED, "创建用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                updateUserFromDTO(user, userDTO);
                User updatedUser = userService.updateUser(user);
                return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser)));
            }
            return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.USER_NOT_FOUND, "用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.OPERATION_FAILED, "更新用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isPresent()) {
                userService.deleteUser(id);
                return ResponseEntity.ok(ApiResponse.success("删除成功"));
            }
            return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.USER_NOT_FOUND, "用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(StatusCode.Business.OPERATION_FAILED, "删除用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setWechatName(user.getWechatName());
        dto.setRealName(user.getRealName());
        dto.setGridArea(user.getGridArea());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus().name().toLowerCase());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        dto.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null);
        return dto;
    }
    
    /**
     * 将DTO转换为实体
     */
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setWechatName(dto.getWechatName());
        user.setRealName(dto.getRealName());
        user.setGridArea(dto.getGridArea());
        user.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(userService.encryptPassword(dto.getPassword()));
        }
        if (dto.getStatus() != null) {
            user.setStatus(User.UserStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        return user;
    }
    
    /**
     * 从DTO更新实体
     */
    private void updateUserFromDTO(User user, UserDTO dto) {
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getWechatName() != null) {
            user.setWechatName(dto.getWechatName());
        }
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getGridArea() != null) {
            user.setGridArea(dto.getGridArea());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(userService.encryptPassword(dto.getPassword()));
        }
        if (dto.getStatus() != null) {
            user.setStatus(User.UserStatus.valueOf(dto.getStatus().toUpperCase()));
        }
    }
}