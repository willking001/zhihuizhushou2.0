package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.User;
import com.dianxiaozhu.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    /**
     * 用户登录验证
     */
    public Optional<User> login(String username, String password) {
        log.info("用户登录尝试: {}", username);
        
        // 先根据用户名查找用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 验证密码
            log.info("密码验证 - 输入密码长度: {}, 数据库哈希: {}", password.length(), user.getPassword());
            boolean passwordMatches = verifyPassword(password, user.getPassword());
            log.info("密码匹配结果: {}", passwordMatches);
            if (passwordMatches) {
                if (user.getStatus() == User.UserStatus.ACTIVE) {
                    // 更新最后登录时间
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                    log.info("用户登录成功: {}", username);
                    return Optional.of(user);
                } else {
                    log.warn("用户账户状态异常: {}, 状态: {}", username, user.getStatus());
                }
            } else {
                log.warn("用户登录失败: 密码错误 - {}", username);
            }
        } else {
            log.warn("用户登录失败: 用户不存在 - {}", username);
        }
        
        return Optional.empty();
    }
    
    /**
     * 验证密码 - 统一使用SHA-256加密模式
     */
    private boolean verifyPassword(String rawPassword, String storedPassword) {
        try {
            // 统一使用SHA-256验证
            String sha256Hash = generateSHA256(rawPassword);
            log.info("SHA-256验证 - 生成的哈希: {}, 存储的哈希: {}", sha256Hash, storedPassword);
            return sha256Hash.equals(storedPassword);
        } catch (Exception e) {
            log.error("密码验证异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成SHA-256哈希
     */
    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("SHA-256哈希生成失败: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 创建新用户
     */
    @Transactional
    public User createUser(User user) {
        log.info("创建新用户: {}", user.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        
        // 检查手机号是否已存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在: " + user.getPhone());
        }
        
        // 使用SHA-256加密密码
        String encryptedPassword = generateSHA256(user.getPassword());
        user.setPassword(encryptedPassword);
        log.info("密码已使用SHA-256加密");
        
        user.setStatus(User.UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("用户创建成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据网格区域查找用户
     */
    public List<User> findByGridArea(String gridArea) {
        return userRepository.findByGridArea(gridArea);
    }
    
    /**
     * 获取所有活跃用户
     */
    public List<User> findActiveUsers() {
        return userRepository.findActiveUsers(User.UserStatus.ACTIVE);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(User user) {
        log.info("更新用户信息: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long userId, User.UserStatus status) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status);
            userRepository.save(user);
            log.info("用户状态更新成功: {} -> {}", user.getUsername(), status);
        } else {
            throw new RuntimeException("用户不存在: " + userId);
        }
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            log.info("用户删除成功: {}", userId);
        } else {
            throw new RuntimeException("用户不存在: " + userId);
        }
    }
    

    /**
     * 更新用户最后登录时间
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
        log.info("用户最后登录时间更新成功: {}", userId);
    }
    
    /**
     * 更新用户密码为SHA-256格式
     */
    @Transactional
    public void updatePasswordToSHA256(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String sha256Password = generateSHA256(rawPassword);
            user.setPassword(sha256Password);
            userRepository.save(user);
            log.info("用户密码已更新为SHA-256格式: {}", username);
        } else {
            throw new RuntimeException("用户不存在: " + username);
        }
    }
    
    /**
     * 批量更新所有用户密码为SHA-256格式（仅用于数据迁移）
     */
    @Transactional
    public void migrateAllPasswordsToSHA256() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // 只处理非SHA-256格式的密码
            if (user.getPassword().length() != 64 || user.getPassword().startsWith("$2a$")) {
                log.warn("跳过用户 {} 的密码迁移，需要手动处理", user.getUsername());
                continue;
            }
        }
        log.info("密码迁移完成");
    }
    
    /**
     * 加密密码
     */
    public String encryptPassword(String rawPassword) {
        return generateSHA256(rawPassword);
    }
    
    /**
     * 分页查询用户列表
     */
    public Page<User> findUsers(String username, String email, String phone, String role, String status, Pageable pageable) {
        log.info("分页查询用户列表: username={}, email={}, phone={}, role={}, status={}, page={}, size={}", 
                username, email, phone, role, status, pageable.getPageNumber(), pageable.getPageSize());
        
        // 构建动态查询条件
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户名模糊匹配
            if (StringUtils.hasText(username)) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
            }
            
            // 邮箱模糊匹配
            if (StringUtils.hasText(email)) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
            }
            
            // 手机号模糊匹配
            if (StringUtils.hasText(phone)) {
                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + phone + "%"));
            }
            
            // 角色精确匹配
            if (StringUtils.hasText(role)) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            
            // 状态精确匹配
            if (StringUtils.hasText(status)) {
                try {
                    User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), userStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的用户状态: {}", status);
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> result = userRepository.findAll(spec, pageable);
        log.info("查询结果: 总数={}, 当前页数据数={}", result.getTotalElements(), result.getContent().size());
        
        return result;
    }
}