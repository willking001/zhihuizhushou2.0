# -*- coding: utf-8 -*-
"""
配置管理模块
实现本地配置存储、群组设置同步和断网机制
"""

import json
import os
import time
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Any
from network_manager import get_network_manager

class ConfigManager:
    """配置管理器"""
    
    def __init__(self, server_url: str = "http://localhost:8000"):
        """
        初始化配置管理器
        
        Args:
            server_url: 服务器地址
        """
        self.server_url = server_url
        self.network_manager = get_network_manager(server_url)
        
        # 配置文件路径
        self.config_dir = "config_cache"
        self.group_config_file = os.path.join(self.config_dir, "group_configs.json")
        self.local_config_file = os.path.join(self.config_dir, "local_config.json")
        self.sync_status_file = os.path.join(self.config_dir, "sync_status.json")
        
        # 确保配置目录存在
        os.makedirs(self.config_dir, exist_ok=True)
        
        # 配置数据
        self.group_configs = {}  # 群组配置
        self.local_config = {}   # 本地配置
        self.sync_status = {     # 同步状态
            'last_sync_time': None,
            'last_sync_version': None,
            'pending_updates': [],
            'sync_errors': []
        }
        
        # 默认配置
        self.default_config = {
            'auto_reply_enabled': True,
            'forward_enabled': True,
            'monitor_enabled': True,
            'sync_interval': 300,  # 5分钟
            'max_offline_messages': 500,
            'log_level': 'INFO'
        }
        
        # 加载本地配置
        self.load_local_configs()
        
        # 注册网络状态回调
        self.network_manager.add_online_callback(self._on_network_online)
        self.network_manager.add_offline_callback(self._on_network_offline)
    
    def load_local_configs(self):
        """加载本地配置"""
        try:
            # 加载群组配置
            if os.path.exists(self.group_config_file):
                with open(self.group_config_file, 'r', encoding='utf-8') as f:
                    self.group_configs = json.load(f)
                    print(f"已加载 {len(self.group_configs)} 个群组配置")
            
            # 加载本地配置
            if os.path.exists(self.local_config_file):
                with open(self.local_config_file, 'r', encoding='utf-8') as f:
                    loaded_config = json.load(f)
                    self.local_config = {**self.default_config, **loaded_config}
                    print("已加载本地配置")
            else:
                self.local_config = self.default_config.copy()
            
            # 加载同步状态
            if os.path.exists(self.sync_status_file):
                with open(self.sync_status_file, 'r', encoding='utf-8') as f:
                    self.sync_status.update(json.load(f))
                    print("已加载同步状态")
                    
        except Exception as e:
            print(f"加载本地配置失败: {str(e)}")
            self.local_config = self.default_config.copy()
    
    def save_local_configs(self):
        """保存本地配置"""
        try:
            # 保存群组配置
            with open(self.group_config_file, 'w', encoding='utf-8') as f:
                json.dump(self.group_configs, f, ensure_ascii=False, indent=2)
            
            # 保存本地配置
            with open(self.local_config_file, 'w', encoding='utf-8') as f:
                json.dump(self.local_config, f, ensure_ascii=False, indent=2)
            
            # 保存同步状态
            with open(self.sync_status_file, 'w', encoding='utf-8') as f:
                json.dump(self.sync_status, f, ensure_ascii=False, indent=2)
                
            print("本地配置已保存")
            
        except Exception as e:
            print(f"保存本地配置失败: {str(e)}")
    
    def get_group_config(self, group_id: str) -> Dict:
        """获取群组配置"""
        return self.group_configs.get(group_id, {
            'auto_reply_enabled': True,
            'forward_enabled': True,
            'monitor_enabled': True,
            'priority': 1,
            'keywords': [],
            'grid_officer_id': None
        })
    
    def update_group_config(self, group_id: str, config: Dict):
        """更新群组配置"""
        if group_id not in self.group_configs:
            self.group_configs[group_id] = {}
        
        self.group_configs[group_id].update(config)
        self.group_configs[group_id]['last_update'] = datetime.now().isoformat()
        
        # 如果网络可用，立即同步到服务器
        if self.network_manager.is_online:
            self._upload_group_config(group_id, config)
        else:
            # 添加到待同步队列
            self.sync_status['pending_updates'].append({
                'type': 'group_config',
                'group_id': group_id,
                'config': config,
                'timestamp': datetime.now().isoformat()
            })
        
        self.save_local_configs()
        print(f"群组配置已更新: {group_id}")
    
    def save_group_config(self, group_id: str, config: Dict):
        """保存群组配置"""
        self.group_configs[group_id] = config
        self.save_local_configs()
        print(f"已保存群组配置: {group_id}")
    
    def get_all_groups(self) -> Dict:
        """获取所有群组配置"""
        return self.group_configs.copy()
    
    def get_local_config(self, key: Optional[str] = None):
        """获取本地配置"""
        if key:
            return self.local_config.get(key)
        return self.local_config.copy()
    
    def update_local_config(self, key: str, value: Any):
        """更新本地配置"""
        self.local_config[key] = value
        self.local_config['last_update'] = datetime.now().isoformat()
        self.save_local_configs()
        print(f"本地配置已更新: {key} = {value}")
    
    def save_local_config(self, config: Dict):
        """保存本地配置"""
        self.local_config.update(config)
        self.save_local_configs()
        print("本地配置已更新并保存")
    
    def sync_from_server(self) -> bool:
        """从服务器同步配置"""
        if not self.network_manager.is_online:
            print("网络不可用，无法同步配置")
            return False
        
        try:
            import requests
            
            # 获取群组配置
            response = requests.get(
                f"{self.server_url}/api/grid/group-configs",
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    server_configs = data.get('data', {})
                    
                    # 更新群组配置
                    for group_id, config in server_configs.items():
                        # 只更新服务器版本更新的配置
                        local_update_time = self.group_configs.get(group_id, {}).get('last_update')
                        server_update_time = config.get('last_update')
                        
                        if not local_update_time or (server_update_time and server_update_time > local_update_time):
                            self.group_configs[group_id] = config
                            print(f"已同步群组配置: {group_id}")
                    
                    self.sync_status['last_sync_time'] = datetime.now().isoformat()
                    self.sync_status['last_sync_version'] = data.get('version')
                    self.save_local_configs()
                    
                    print(f"配置同步成功，共同步 {len(server_configs)} 个群组配置")
                    return True
            
            print(f"同步配置失败，状态码: {response.status_code}")
            return False
            
        except Exception as e:
            print(f"同步配置时出错: {str(e)}")
            self.sync_status['sync_errors'].append({
                'error': str(e),
                'timestamp': datetime.now().isoformat()
            })
            return False
    
    def _upload_group_config(self, group_id: str, config: Dict) -> bool:
        """上传群组配置到服务器"""
        try:
            import requests
            
            upload_data = {
                'group_id': group_id,
                'config': config,
                'client_id': self._get_client_id()
            }
            
            response = requests.post(
                f"{self.server_url}/api/grid/group-config",
                json=upload_data,
                timeout=10
            )
            
            if response.status_code == 200:
                print(f"群组配置上传成功: {group_id}")
                return True
            else:
                print(f"群组配置上传失败: {group_id}, 状态码: {response.status_code}")
                return False
                
        except Exception as e:
            print(f"上传群组配置时出错: {str(e)}")
            return False
    
    def _process_pending_updates(self):
        """处理待同步的更新"""
        if not self.sync_status['pending_updates']:
            return
        
        print(f"开始处理 {len(self.sync_status['pending_updates'])} 个待同步更新")
        
        processed_updates = []
        
        for update in self.sync_status['pending_updates']:
            try:
                if update['type'] == 'group_config':
                    success = self._upload_group_config(
                        update['group_id'], 
                        update['config']
                    )
                    
                    if success:
                        processed_updates.append(update)
                        
            except Exception as e:
                print(f"处理待同步更新失败: {str(e)}")
        
        # 移除已处理的更新
        for update in processed_updates:
            self.sync_status['pending_updates'].remove(update)
        
        self.save_local_configs()
    
    def _get_client_id(self) -> str:
        """获取客户端ID"""
        import socket
        import hashlib
        
        hostname = socket.gethostname()
        return hashlib.md5(hostname.encode()).hexdigest()[:8]
    
    def _on_network_online(self):
        """网络恢复时的回调"""
        print("网络已恢复，开始同步配置")
        # 同步服务器配置
        self.sync_from_server()
        # 处理待同步的更新
        self._process_pending_updates()
    
    def _on_network_offline(self):
        """网络断开时的回调"""
        print("网络已断开，配置将保存到本地")
        self.save_local_configs()
    
    def get_monitored_groups(self) -> List[str]:
        """获取需要监控的群组列表"""
        monitored_groups = []
        
        for group_id, config in self.group_configs.items():
            if config.get('monitor_enabled', True):
                monitored_groups.append(group_id)
        
        return monitored_groups
    
    def is_group_auto_reply_enabled(self, group_id: str) -> bool:
        """检查群组是否启用自动回复"""
        config = self.get_group_config(group_id)
        return config.get('auto_reply_enabled', True)
    
    def is_group_forward_enabled(self, group_id: str) -> bool:
        """检查群组是否启用转发"""
        config = self.get_group_config(group_id)
        return config.get('forward_enabled', True)
    
    def get_group_priority(self, group_id: str) -> int:
        """获取群组优先级"""
        config = self.get_group_config(group_id)
        return config.get('priority', 1)
    
    def get_sync_status(self) -> Dict:
        """获取同步状态"""
        return {
            'last_sync_time': self.sync_status['last_sync_time'],
            'pending_updates_count': len(self.sync_status['pending_updates']),
            'sync_errors_count': len(self.sync_status['sync_errors']),
            'total_groups': len(self.group_configs),
            'monitored_groups': len(self.get_monitored_groups())
        }
    
    def cleanup(self):
        """清理资源"""
        self.save_local_configs()
        print("配置管理器资源已清理")

# 全局实例
_config_manager = None

def get_config_manager(server_url: str = "http://localhost:8000") -> ConfigManager:
    """获取配置管理器实例（单例模式）"""
    global _config_manager
    if _config_manager is None:
        _config_manager = ConfigManager(server_url)
    return _config_manager

def cleanup_config_manager():
    """清理配置管理器实例"""
    global _config_manager
    if _config_manager:
        _config_manager.cleanup()
        _config_manager = None