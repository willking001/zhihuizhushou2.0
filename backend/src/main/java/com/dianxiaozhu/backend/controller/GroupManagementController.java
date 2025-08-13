package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.GroupDailyStatistics;
import com.dianxiaozhu.backend.entity.GroupManagementStatus;
import com.dianxiaozhu.backend.service.GroupManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组管理控制器
 */
@RestController
@RequestMapping("/api/group-management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "群组管理", description = "群组管理相关接口")
public class GroupManagementController {

    private final GroupManagementService groupManagementService;

    /**
     * 获取群组列表
     */
    @GetMapping("/groups")
    @Operation(summary = "获取群组列表", description = "根据状态和名称过滤获取群组列表")
    public ResponseEntity<Map<String, Object>> getGroups(
            @Parameter(description = "群组状态") @RequestParam(required = false) GroupManagementStatus.GroupStatus status,
            @Parameter(description = "群组名称") @RequestParam(required = false) String groupName) {
        
        try {
            List<GroupManagementStatus> groups = groupManagementService.getGroupList(status, groupName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", groups);
            response.put("total", groups.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取群组列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取群组列表失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取群组详细信息
     */
    @GetMapping("/groups/{chatRoom}")
    @Operation(summary = "获取群组详细信息", description = "根据群组标识获取详细信息")
    public ResponseEntity<Map<String, Object>> getGroupDetails(
            @Parameter(description = "群组标识") @PathVariable String chatRoom) {
        
        try {
            GroupManagementStatus group = groupManagementService.getGroupDetails(chatRoom);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", group);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取群组详细信息失败: {}", chatRoom, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取群组详细信息失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 更新群组状态
     */
    @PutMapping("/groups/{chatRoom}/status")
    @Operation(summary = "更新群组状态", description = "更新指定群组的状态")
    public ResponseEntity<Map<String, Object>> updateGroupStatus(
            @Parameter(description = "群组标识") @PathVariable String chatRoom,
            @RequestBody Map<String, Object> request) {
        
        try {
            String statusStr = (String) request.get("status");
            String operator = (String) request.get("operator");
            String reason = (String) request.get("reason");
            
            GroupManagementStatus.GroupStatus status = GroupManagementStatus.GroupStatus.valueOf(statusStr);
            GroupManagementStatus updatedGroup = groupManagementService.updateGroupStatus(chatRoom, status, operator, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedGroup);
            response.put("message", "群组状态更新成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新群组状态失败: {}", chatRoom, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新群组状态失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 批量更新群组状态
     */
    @PutMapping("/groups/batch-status")
    @Operation(summary = "批量更新群组状态", description = "批量更新多个群组的状态")
    public ResponseEntity<Map<String, Object>> batchUpdateGroupStatus(
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> chatRooms = (List<String>) request.get("chatRooms");
            String statusStr = (String) request.get("status");
            
            GroupManagementStatus.GroupStatus status = GroupManagementStatus.GroupStatus.valueOf(statusStr);
            int updatedCount = groupManagementService.batchUpdateGroupStatus(chatRooms, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            response.put("message", "批量更新成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量更新群组状态失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量更新失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取活跃群组
     */
    @GetMapping("/groups/active")
    @Operation(summary = "获取活跃群组", description = "获取指定时间范围内的活跃群组")
    public ResponseEntity<Map<String, Object>> getActiveGroups(
            @Parameter(description = "活跃时间范围（小时）") @RequestParam(defaultValue = "24") int hours) {
        
        try {
            List<GroupManagementStatus> activeGroups = groupManagementService.getActiveGroups(hours);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", activeGroups);
            response.put("total", activeGroups.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取活跃群组失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取活跃群组失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取需要关注的群组
     */
    @GetMapping("/groups/attention")
    @Operation(summary = "获取需要关注的群组", description = "获取接管次数超过阈值的群组")
    public ResponseEntity<Map<String, Object>> getGroupsNeedingAttention(
            @Parameter(description = "接管次数阈值") @RequestParam(required = false) Integer threshold) {
        
        try {
            List<GroupManagementStatus> groups = groupManagementService.getGroupsNeedingAttention(threshold);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", groups);
            response.put("total", groups.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取需要关注的群组失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取需要关注的群组失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取群组状态统计
     */
    @GetMapping("/statistics/status")
    @Operation(summary = "获取群组状态统计", description = "获取各状态群组的数量统计")
    public ResponseEntity<Map<String, Object>> getGroupStatusStatistics() {
        try {
            Map<String, Long> statistics = groupManagementService.getGroupStatusStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取群组状态统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取群组状态统计失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取总体统计数据
     */
    @GetMapping("/statistics/overall")
    @Operation(summary = "获取总体统计数据", description = "获取指定日期范围的总体统计数据")
    public ResponseEntity<Map<String, Object>> getOverallStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            Map<String, Object> statistics = groupManagementService.getOverallStatistics(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取总体统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取总体统计数据失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取群组排行榜
     */
    @GetMapping("/statistics/ranking")
    @Operation(summary = "获取群组排行榜", description = "获取群组排行榜数据")
    public ResponseEntity<Map<String, Object>> getGroupRanking(
            @Parameter(description = "排行榜类型") @RequestParam String type,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<Map<String, Object>> ranking = groupManagementService.getGroupRanking(type, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ranking);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取群组排行榜失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取群组排行榜失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取每日趋势数据
     */
    @GetMapping("/statistics/trends")
    @Operation(summary = "获取每日趋势数据", description = "获取指定日期范围的每日趋势数据")
    public ResponseEntity<Map<String, Object>> getDailyTrends(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<Map<String, Object>> trends = groupManagementService.getDailyTrends(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", trends);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取每日趋势数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取每日趋势数据失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取群组历史统计
     */
    @GetMapping("/groups/{chatRoom}/history")
    @Operation(summary = "获取群组历史统计", description = "获取指定群组的历史统计数据")
    public ResponseEntity<Map<String, Object>> getGroupHistoryStats(
            @Parameter(description = "群组标识") @PathVariable String chatRoom,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<GroupDailyStatistics> history = groupManagementService.getGroupHistoryStats(chatRoom, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取群组历史统计失败: {}", chatRoom, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取群组历史统计失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 记录消息活动（供内部调用）
     */
    @PostMapping("/groups/{chatRoom}/activity")
    @Operation(summary = "记录消息活动", description = "记录群组的消息活动")
    public ResponseEntity<Map<String, Object>> recordMessageActivity(
            @Parameter(description = "群组标识") @PathVariable String chatRoom,
            @RequestBody Map<String, Object> request) {
        
        try {
            Boolean isAutoReply = (Boolean) request.getOrDefault("isAutoReply", false);
            groupManagementService.recordMessageActivity(chatRoom, isAutoReply);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消息活动记录成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("记录消息活动失败: {}", chatRoom, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "记录消息活动失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}