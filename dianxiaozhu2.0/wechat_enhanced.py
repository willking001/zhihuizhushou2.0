# -*- coding: utf-8 -*-
"""
微信增强监控模块
实现微信消息的监控、转发和自动回复功能
"""

import time
import threading
from datetime import datetime
from typing import Dict, List, Optional, Callable
import json
import os

# 导入网络管理器
from network_manager import get_network_manager

try:
    import wxauto
except ImportError:
    print("警告：wxauto模块未安装，微信功能将不可用")
    wxauto = None

class WeChatEnhanced:
    """微信增强监控类"""
    
    def __init__(self, callback_func: Optional[Callable] = None, server_url: str = "http://localhost:8000"):
        """
        初始化微信监控
        
        Args:
            callback_func: 消息回调函数
            server_url: 服务器地址
        """
        self.wx = None
        self.is_monitoring = False
        self.monitor_thread = None
        self.callback_func = callback_func
        self.last_message_time = {}
        self.message_cache = set()
        
        # 初始化网络管理器
        self.network_manager = get_network_manager(server_url)
        
        # 离线消息存储
        self.offline_messages = []
        self.max_offline_messages = 500
        
        # 配置参数
        self.config = {
            'monitor_interval': 2,  # 监控间隔（秒）
            'max_cache_size': 1000,  # 消息缓存最大数量
            'duplicate_threshold': 5,  # 重复消息阈值（秒）
        }
        
        # 统计信息
        self.stats = {
            'total_messages': 0,
            'forwarded_messages': 0,
            'auto_replies': 0,
            'start_time': None
        }
        
    def initialize(self) -> bool:
        """初始化微信连接"""
        if not wxauto:
            print("wxauto模块未安装，无法初始化微信")
            return False
            
        try:
            self.wx = wxauto.WeChat()
            print("微信连接初始化成功")
            return True
        except Exception as e:
            print(f"微信初始化失败: {str(e)}")
            return False
    
    def start_monitoring(self) -> bool:
        """开始监控微信消息"""
        if not self.wx:
            if not self.initialize():
                return False
        
        if self.is_monitoring:
            print("微信监控已在运行中")
            return True
            
        self.is_monitoring = True
        self.stats['start_time'] = datetime.now()
        
        # 启动监控线程
        self.monitor_thread = threading.Thread(
            target=self._monitor_loop,
            daemon=True
        )
        self.monitor_thread.start()
        
        print("微信消息监控已启动")
        return True
    
    def stop_monitoring(self):
        """停止监控微信消息"""
        self.is_monitoring = False
        if self.monitor_thread and self.monitor_thread.is_alive():
            self.monitor_thread.join(timeout=5)
        print("微信消息监控已停止")
    
    def _monitor_loop(self):
        """监控循环"""
        while self.is_monitoring:
            try:
                self._check_new_messages()
                time.sleep(self.config['monitor_interval'])
            except Exception as e:
                print(f"监控循环出错: {str(e)}")
                time.sleep(5)  # 出错时等待更长时间
    
    def _check_new_messages(self):
        """检查新消息"""
        if not self.wx:
            return
            
        try:
            # 获取所有聊天窗口
            chat_list = self.wx.GetSessionList()
            
            for chat in chat_list:
                chat_name = chat.get('name', '')
                if not chat_name:
                    continue
                    
                # 获取该聊天的最新消息
                messages = self._get_chat_messages(chat_name)
                
                for message in messages:
                    if self._is_new_message(message, chat_name):
                        self._process_message(message, chat_name)
                        
        except Exception as e:
            print(f"检查新消息时出错: {str(e)}")
    
    def _get_chat_messages(self, chat_name: str) -> List[Dict]:
        """获取指定聊天的消息"""
        try:
            # 切换到指定聊天
            self.wx.ChatWith(chat_name)
            time.sleep(0.5)  # 等待界面加载
            
            # 获取最新消息
            messages = self.wx.GetAllMessage()
            
            # 只返回最近的几条消息
            return messages[-5:] if messages else []
            
        except Exception as e:
            print(f"获取聊天消息失败 {chat_name}: {str(e)}")
            return []
    
    def _is_new_message(self, message: Dict, chat_name: str) -> bool:
        """判断是否为新消息"""
        try:
            # 构建消息唯一标识
            message_id = f"{chat_name}_{message.get('time', '')}_{message.get('msg', '')}"
            
            # 检查是否已处理过
            if message_id in self.message_cache:
                return False
            
            # 添加到缓存
            self.message_cache.add(message_id)
            
            # 清理过大的缓存
            if len(self.message_cache) > self.config['max_cache_size']:
                # 移除最旧的一半消息
                old_cache = list(self.message_cache)
                self.message_cache = set(old_cache[len(old_cache)//2:])
            
            return True
            
        except Exception as e:
            print(f"判断新消息时出错: {str(e)}")
            return False
    
    def _process_message(self, message: Dict, chat_name: str):
        """处理新消息"""
        try:
            msg_content = message.get('msg', '')
            msg_time = message.get('time', '')
            sender = message.get('sender', '未知')
            
            # 过滤系统消息和空消息
            if not msg_content or msg_content.startswith('[系统]'):
                return
            
            # 构建消息数据
            message_data = {
                'content': msg_content,
                'sender': sender,
                'chat_name': chat_name,
                'time': msg_time,
                'timestamp': datetime.now().isoformat()
            }
            
            # 更新统计
            self.stats['total_messages'] += 1
            
            # 如果网络不可用，保存到离线队列
            if not self.network_manager.is_online:
                self._store_offline_message(message_data)
            
            # 调用回调函数
            if self.callback_func:
                self.callback_func(message_data)
            
            print(f"新消息 [{chat_name}] {sender}: {msg_content}")
            
        except Exception as e:
            print(f"处理消息时出错: {str(e)}")
    
    def send_message(self, to: str, message: str) -> bool:
        """发送消息到指定联系人"""
        if not self.wx:
            print("微信未初始化")
            return False
            
        try:
            # 切换到指定聊天
            self.wx.ChatWith(to)
            time.sleep(0.5)
            
            # 发送消息
            self.wx.SendMsg(message)
            
            print(f"消息已发送到 {to}: {message}")
            self.stats['forwarded_messages'] += 1
            return True
            
        except Exception as e:
            print(f"发送消息失败: {str(e)}")
            return False
    
    def send_auto_reply(self, to: str, original_message: str, reply_template: str) -> bool:
        """发送自动回复"""
        try:
            # 处理回复模板中的变量
            reply_message = self._process_reply_template(reply_template, {
                'original_message': original_message,
                'time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                'sender': to
            })
            
            success = self.send_message(to, reply_message)
            if success:
                self.stats['auto_replies'] += 1
            
            return success
            
        except Exception as e:
            print(f"发送自动回复失败: {str(e)}")
            return False
    
    def _process_reply_template(self, template: str, variables: Dict) -> str:
        """处理回复模板中的变量"""
        try:
            result = template
            for key, value in variables.items():
                result = result.replace(f'{{{key}}}', str(value))
            return result
        except Exception as e:
            print(f"处理回复模板失败: {str(e)}")
            return template
    
    def get_contact_list(self) -> List[str]:
        """获取联系人列表"""
        if not self.wx:
            return []
            
        try:
            contacts = self.wx.GetSessionList()
            return [contact.get('name', '') for contact in contacts if contact.get('name')]
        except Exception as e:
            print(f"获取联系人列表失败: {str(e)}")
            return []
    
    def get_statistics(self) -> Dict:
        """获取统计信息"""
        stats = self.stats.copy()
        if stats['start_time']:
            runtime = datetime.now() - stats['start_time']
            stats['runtime_seconds'] = runtime.total_seconds()
            stats['runtime_formatted'] = str(runtime).split('.')[0]
        return stats
    
    def is_connected(self) -> bool:
        """检查微信是否连接"""
        return self.wx is not None
    
    def _store_offline_message(self, message_data: Dict):
        """存储离线消息"""
        try:
            # 添加到离线消息队列
            self.offline_messages.append(message_data)
            
            # 限制离线消息数量
            if len(self.offline_messages) > self.max_offline_messages:
                # 移除最旧的消息
                self.offline_messages = self.offline_messages[-self.max_offline_messages:]
            
            print(f"已保存离线消息: {message_data['content'][:30]}...")
            
        except Exception as e:
            print(f"保存离线消息失败: {str(e)}")
    
    def get_offline_messages(self) -> List[Dict]:
        """获取离线消息"""
        return self.offline_messages.copy()
    
    def clear_offline_messages(self):
        """清空离线消息"""
        self.offline_messages.clear()
        print("已清空离线消息")
    
    def process_offline_messages(self):
        """处理离线消息（网络恢复时调用）"""
        if not self.offline_messages:
            return
        
        print(f"开始处理 {len(self.offline_messages)} 条离线消息")
        
        for message_data in self.offline_messages:
            try:
                # 重新处理消息
                if self.callback_func:
                    self.callback_func(message_data)
                    
            except Exception as e:
                print(f"处理离线消息失败: {str(e)}")
        
        # 清空已处理的离线消息
        self.clear_offline_messages()
    
    def cleanup(self):
        """清理资源"""
        self.stop_monitoring()
        self.wx = None
        self.message_cache.clear()
        print("微信监控资源已清理")

# 全局实例
_wechat_instance = None

def get_wechat_instance(callback_func: Optional[Callable] = None, server_url: str = "http://localhost:8000") -> WeChatEnhanced:
    """获取微信实例（单例模式）"""
    global _wechat_instance
    if _wechat_instance is None:
        _wechat_instance = WeChatEnhanced(callback_func, server_url)
    return _wechat_instance

def cleanup_wechat():
    """清理微信实例"""
    global _wechat_instance
    if _wechat_instance:
        _wechat_instance.cleanup()
        _wechat_instance = None