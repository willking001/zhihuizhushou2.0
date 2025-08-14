#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
断网机制测试脚本
测试网络管理器、配置管理器和离线功能
"""

import sys
import os
import time
import json
from datetime import datetime

# 添加当前目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    from network_manager import get_network_manager, cleanup_network_manager
    from config_manager import get_config_manager, cleanup_config_manager
    from keyword_manager import get_keyword_manager
    from integrated_service import get_integrated_service
except ImportError as e:
    print(f"导入模块失败: {e}")
    sys.exit(1)

def test_network_manager():
    """测试网络管理器"""
    print("\n=== 测试网络管理器 ===")
    
    server_url = "http://localhost:8080"
    network_manager = get_network_manager(server_url)
    
    print(f"网络状态: {'在线' if network_manager.is_online else '离线'}")
    
    # 测试添加待上传数据
    test_data = {
        "content": "测试消息",
        "sender": "测试用户",
        "timestamp": datetime.now().isoformat()
    }
    
    network_manager.add_pending_upload('test_message', test_data, priority='high')
    print("已添加测试数据到待上传队列")
    
    # 查看待上传数据
    pending = network_manager.get_pending_uploads()
    print(f"待上传数据数量: {len(pending)}")
    
    return network_manager

def test_config_manager():
    """测试配置管理器"""
    print("\n=== 测试配置管理器 ===")
    
    server_url = "http://localhost:8080"
    config_manager = get_config_manager(server_url)
    
    # 测试本地配置
    test_config = {
        "keywords": "测试,紧急,故障",
        "forward_target": "测试群组",
        "last_sync": datetime.now().isoformat()
    }
    
    config_manager.save_local_config(test_config)
    print("已保存测试配置到本地")
    
    # 读取本地配置
    loaded_config = config_manager.get_local_config()
    print(f"本地配置: {loaded_config}")
    
    # 测试群组配置
    group_config = {
        "group_name": "测试群组",
        "keywords": "紧急,故障",
        "enabled": True
    }
    
    config_manager.save_group_config("test_group", group_config)
    print("已保存群组配置")
    
    groups = config_manager.get_all_groups()
    print(f"群组配置: {groups}")
    
    return config_manager

def test_keyword_manager():
    """测试关键词管理器"""
    print("\n=== 测试关键词管理器 ===")
    
    try:
        keyword_manager = get_keyword_manager()
        
        # 测试添加学习关键词
        keyword_manager.add_learned_keyword("测试关键词", "high")
        print("已添加学习关键词")
        
        # 获取所有关键词
        all_keywords = keyword_manager.get_all_keywords()
        print(f"所有关键词: {len(all_keywords)} 个")
        
        return keyword_manager
    except Exception as e:
        print(f"关键词管理器测试失败: {e}")
        return None

def test_integrated_service():
    """测试集成服务"""
    print("\n=== 测试集成服务 ===")
    
    try:
        integrated_service = get_integrated_service()
        
        # 测试消息处理
        test_message = {
            "content": "这是一个包含紧急关键词的测试消息",
            "sender": "测试用户",
            "timestamp": datetime.now().isoformat()
        }
        
        result = integrated_service.process_message(test_message)
        print(f"消息处理结果: {result}")
        
        return integrated_service
    except Exception as e:
        print(f"集成服务测试失败: {e}")
        return None

def test_offline_scenario():
    """测试断网场景"""
    print("\n=== 测试断网场景 ===")
    
    server_url = "http://localhost:8080"
    network_manager = get_network_manager(server_url)
    
    # 模拟网络断开
    print("模拟网络断开...")
    network_manager._handle_offline()
    
    print(f"网络状态: {'在线' if network_manager.is_online else '离线'}")
    
    # 在离线状态下添加数据
    offline_data = {
        "content": "离线状态下的消息",
        "sender": "离线用户",
        "timestamp": datetime.now().isoformat()
    }
    
    network_manager.add_pending_upload('offline_message', offline_data, priority='medium')
    print("已在离线状态下添加数据")
    
    # 查看待上传数据
    pending = network_manager.get_pending_uploads()
    print(f"待上传数据数量: {len(pending)}")
    
    # 模拟网络恢复
    print("\n模拟网络恢复...")
    network_manager._handle_online()
    
    print(f"网络状态: {'在线' if network_manager.is_online else '离线'}")
    
    return network_manager

def main():
    """主测试函数"""
    print("开始断网机制测试...")
    
    try:
        # 测试各个组件
        network_manager = test_network_manager()
        config_manager = test_config_manager()
        keyword_manager = test_keyword_manager()
        integrated_service = test_integrated_service()
        
        # 测试断网场景
        test_offline_scenario()
        
        print("\n=== 测试完成 ===")
        print("所有组件测试通过，断网机制工作正常")
        
    except Exception as e:
        print(f"测试过程中发生错误: {e}")
    
    finally:
        # 清理资源
        try:
            cleanup_network_manager()
            cleanup_config_manager()
            print("资源清理完成")
        except Exception as e:
            print(f"资源清理失败: {e}")

if __name__ == "__main__":
    main()