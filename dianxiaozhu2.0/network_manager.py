# -*- coding: utf-8 -*-
"""
网络连接管理模块
实现断网检测、离线数据存储和自动上传功能
"""

import json
import os
import time
import threading
import requests
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Any, Callable
from collections import deque
import socket
import hashlib

class NetworkManager:
    """网络连接管理器"""
    
    def __init__(self, server_url: str = "http://localhost:8000"):
        """
        初始化网络管理器
        
        Args:
            server_url: 服务器地址
        """
        self.server_url = server_url
        self.session = requests.Session()
        
        # 网络状态
        self.is_online = False
        self.last_check_time = None
        self.connection_check_interval = 30  # 30秒检查一次网络
        
        # 离线数据存储
        self.offline_dir = "offline_data"
        self.pending_uploads_file = os.path.join(self.offline_dir, "pending_uploads.json")
        self.network_status_file = os.path.join(self.offline_dir, "network_status.json")
        
        # 确保离线数据目录存在
        os.makedirs(self.offline_dir, exist_ok=True)
        
        # 待上传数据队列
        self.pending_uploads = deque(maxlen=1000)  # 最多保存1000条待上传数据
        
        # 网络状态监控
        self.monitor_thread = None
        self.is_monitoring = False
        
        # 回调函数
        self.online_callbacks = []  # 网络恢复时的回调
        self.offline_callbacks = []  # 网络断开时的回调
        
        # 统计信息
        self.stats = {
            'total_checks': 0,
            'online_count': 0,
            'offline_count': 0,
            'upload_attempts': 0,
            'upload_success': 0,
            'upload_failed': 0,
            'pending_data_count': 0,
            'last_online_time': None,
            'last_offline_time': None
        }
        
        # 加载本地数据
        self.load_offline_data()
        
        # 启动网络监控
        self.start_monitoring()
    
    def load_offline_data(self):
        """加载离线数据"""
        try:
            # 加载待上传数据
            if os.path.exists(self.pending_uploads_file):
                with open(self.pending_uploads_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    self.pending_uploads = deque(data.get('uploads', []), maxlen=1000)
                    print(f"已加载 {len(self.pending_uploads)} 条待上传数据")
            
            # 加载网络状态统计
            if os.path.exists(self.network_status_file):
                with open(self.network_status_file, 'r', encoding='utf-8') as f:
                    self.stats.update(json.load(f))
                    print("已加载网络状态统计数据")
                    
        except Exception as e:
            print(f"加载离线数据失败: {str(e)}")
    
    def save_offline_data(self):
        """保存离线数据"""
        try:
            # 保存待上传数据
            upload_data = {
                'uploads': list(self.pending_uploads),
                'last_save_time': datetime.now().isoformat()
            }
            
            with open(self.pending_uploads_file, 'w', encoding='utf-8') as f:
                json.dump(upload_data, f, ensure_ascii=False, indent=2)
            
            # 保存网络状态统计
            self.stats['pending_data_count'] = len(self.pending_uploads)
            with open(self.network_status_file, 'w', encoding='utf-8') as f:
                json.dump(self.stats, f, ensure_ascii=False, indent=2)
                
            print("离线数据已保存")
            
        except Exception as e:
            print(f"保存离线数据失败: {str(e)}")
    
    def check_network_connection(self) -> bool:
        """检查网络连接状态"""
        try:
            self.stats['total_checks'] += 1
            
            # 尝试连接服务器
            response = self.session.get(
                f"{self.server_url}/api/health",
                timeout=5
            )
            
            if response.status_code == 200:
                if not self.is_online:
                    # 网络恢复
                    self.is_online = True
                    self.stats['last_online_time'] = datetime.now().isoformat()
                    self.stats['online_count'] += 1
                    print("网络连接已恢复")
                    self._trigger_online_callbacks()
                    
                    # 尝试上传待上传数据
                    self._process_pending_uploads()
                
                self.last_check_time = datetime.now()
                return True
            else:
                self._handle_offline()
                return False
                
        except Exception as e:
            self._handle_offline()
            return False
    
    def _handle_offline(self):
        """处理离线状态"""
        if self.is_online:
            # 网络断开
            self.is_online = False
            self.stats['last_offline_time'] = datetime.now().isoformat()
            self.stats['offline_count'] += 1
            print("网络连接已断开")
            self._trigger_offline_callbacks()
        
        self.last_check_time = datetime.now()
    
    def _handle_online(self):
        """处理网络恢复事件"""
        print("网络连接已恢复，开始同步离线数据")
        
        # 处理待上传数据
        self._process_pending_uploads()
        
        # 通知其他组件网络状态变化
        if hasattr(self, 'on_network_change'):
            self.on_network_change(True)
    
    def add_pending_upload(self, data_type: str, data: Dict, priority = 1):
        """添加待上传数据"""
        # 处理字符串优先级
        if isinstance(priority, str):
            priority_map = {'low': 1, 'medium': 2, 'high': 3}
            priority = priority_map.get(priority.lower(), 1)
        
        upload_item = {
            'id': self._generate_upload_id(),
            'type': data_type,
            'data': data,
            'priority': priority,
            'timestamp': datetime.now().isoformat(),
            'retry_count': 0,
            'max_retries': 3
        }
        
        # 按优先级插入（高优先级在前）
        if priority >= 3:  # 高优先级
            self.pending_uploads.appendleft(upload_item)
        else:
            self.pending_uploads.append(upload_item)
        
        print(f"已添加待上传数据: {data_type}, 优先级: {priority}")
        
        # 如果网络可用，立即尝试上传
        if self.is_online:
            self._process_pending_uploads()
        
        # 保存到本地
        self.save_offline_data()
    
    def _process_pending_uploads(self):
        """处理待上传数据"""
        if not self.is_online or not self.pending_uploads:
            return
        
        print(f"开始处理 {len(self.pending_uploads)} 条待上传数据")
        
        # 创建副本以避免在迭代时修改
        uploads_to_process = list(self.pending_uploads)
        
        for upload_item in uploads_to_process:
            try:
                self.stats['upload_attempts'] += 1
                
                # 根据数据类型选择上传接口
                success = self._upload_data_item(upload_item)
                
                if success:
                    self.stats['upload_success'] += 1
                    # 从队列中移除成功上传的数据
                    if upload_item in self.pending_uploads:
                        self.pending_uploads.remove(upload_item)
                    print(f"数据上传成功: {upload_item['type']}")
                else:
                    self.stats['upload_failed'] += 1
                    upload_item['retry_count'] += 1
                    
                    # 如果重试次数超过限制，移除该数据
                    if upload_item['retry_count'] >= upload_item['max_retries']:
                        if upload_item in self.pending_uploads:
                            self.pending_uploads.remove(upload_item)
                        print(f"数据上传失败，已达到最大重试次数: {upload_item['type']}")
                    else:
                        print(f"数据上传失败，将重试: {upload_item['type']} (重试次数: {upload_item['retry_count']})")
                
            except Exception as e:
                print(f"处理上传数据时出错: {str(e)}")
                upload_item['retry_count'] += 1
        
        # 保存更新后的数据
        self.save_offline_data()
    
    def _upload_data_item(self, upload_item: Dict) -> bool:
        """上传单个数据项"""
        try:
            data_type = upload_item['type']
            data = upload_item['data']
            
            # 根据数据类型选择不同的上传接口
            if data_type == 'message':
                endpoint = '/api/grid/messages'
            elif data_type == 'keyword_stats':
                endpoint = '/api/keywords/upload_stats'
            elif data_type == 'learned_keywords':
                endpoint = '/api/keywords/upload_learned'
            elif data_type == 'group_config':
                endpoint = '/api/group/config_update'
            else:
                endpoint = '/api/data/upload'
            
            response = self.session.post(
                f"{self.server_url}{endpoint}",
                json=data,
                timeout=10
            )
            
            return response.status_code == 200
            
        except Exception as e:
            print(f"上传数据项失败: {str(e)}")
            return False
    
    def _generate_upload_id(self) -> str:
        """生成上传ID"""
        timestamp = str(int(time.time() * 1000))
        client_id = self._get_client_id()
        return hashlib.md5(f"{client_id}_{timestamp}".encode()).hexdigest()[:12]
    
    def _get_client_id(self) -> str:
        """获取客户端ID"""
        hostname = socket.gethostname()
        return hashlib.md5(hostname.encode()).hexdigest()[:8]
    
    def start_monitoring(self):
        """启动网络监控"""
        if self.is_monitoring:
            return
        
        self.is_monitoring = True
        self.monitor_thread = threading.Thread(
            target=self._monitoring_loop,
            daemon=True
        )
        self.monitor_thread.start()
        print("网络监控已启动")
    
    def stop_monitoring(self):
        """停止网络监控"""
        self.is_monitoring = False
        if self.monitor_thread and self.monitor_thread.is_alive():
            self.monitor_thread.join(timeout=5)
        print("网络监控已停止")
    
    def _monitoring_loop(self):
        """网络监控循环"""
        while self.is_monitoring:
            try:
                self.check_network_connection()
                time.sleep(self.connection_check_interval)
            except Exception as e:
                print(f"网络监控循环出错: {str(e)}")
                time.sleep(10)
    
    def add_online_callback(self, callback: Callable):
        """添加网络恢复回调函数"""
        self.online_callbacks.append(callback)
    
    def add_offline_callback(self, callback: Callable):
        """添加网络断开回调函数"""
        self.offline_callbacks.append(callback)
    
    def _trigger_online_callbacks(self):
        """触发网络恢复回调"""
        for callback in self.online_callbacks:
            try:
                callback()
            except Exception as e:
                print(f"执行网络恢复回调时出错: {str(e)}")
    
    def _trigger_offline_callbacks(self):
        """触发网络断开回调"""
        for callback in self.offline_callbacks:
            try:
                callback()
            except Exception as e:
                print(f"执行网络断开回调时出错: {str(e)}")
    
    def get_network_status(self) -> Dict:
        """获取网络状态信息"""
        return {
            'is_online': self.is_online,
            'last_check_time': self.last_check_time.isoformat() if self.last_check_time else None,
            'pending_uploads_count': len(self.pending_uploads),
            'stats': self.stats.copy()
        }
    
    def force_upload_all(self) -> bool:
        """强制上传所有待上传数据"""
        if not self.is_online:
            print("网络不可用，无法强制上传")
            return False
        
        print("开始强制上传所有待上传数据")
        self._process_pending_uploads()
        return len(self.pending_uploads) == 0
    
    def get_pending_uploads(self) -> List[Dict]:
        """获取待上传数据列表"""
        return list(self.pending_uploads)
    
    def clear_pending_uploads(self):
        """清空待上传数据"""
        self.pending_uploads.clear()
        self.save_offline_data()
        print("已清空所有待上传数据")
    
    def cleanup(self):
        """清理资源"""
        self.stop_monitoring()
        self.save_offline_data()
        print("网络管理器资源已清理")

# 全局实例
_network_manager = None

def get_network_manager(server_url: str = "http://localhost:8000") -> NetworkManager:
    """获取网络管理器实例（单例模式）"""
    global _network_manager
    if _network_manager is None:
        _network_manager = NetworkManager(server_url)
    return _network_manager

def cleanup_network_manager():
    """清理网络管理器实例"""
    global _network_manager
    if _network_manager:
        _network_manager.cleanup()
        _network_manager = None