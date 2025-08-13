# -*- coding: utf-8 -*-
"""
关键词管理模块
实现关键词的智能管理、自学习和同步功能
支持服务器端关键词库、客户端本地关键词、智能自学习机制和关键词同步机制
"""

import json
import os
import requests
import threading
import time
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Set, Tuple
from collections import defaultdict, Counter
import re

class KeywordManager:
    """关键词管理器"""
    
    def __init__(self, server_url: str = "http://localhost:8000"):
        """
        初始化关键词管理器
        
        Args:
            server_url: 服务器地址
        """
        self.server_url = server_url
        self.session = requests.Session()
        
        # 本地缓存文件路径
        self.cache_dir = "keyword_cache"
        self.server_keywords_file = os.path.join(self.cache_dir, "server_keywords.json")
        self.local_keywords_file = os.path.join(self.cache_dir, "local_keywords.json")
        self.learned_keywords_file = os.path.join(self.cache_dir, "learned_keywords.json")
        self.keyword_stats_file = os.path.join(self.cache_dir, "keyword_stats.json")
        
        # 确保缓存目录存在
        os.makedirs(self.cache_dir, exist_ok=True)
        
        # 关键词数据结构
        self.server_keywords = {}  # 服务器端关键词（等级1，最高优先级）
        self.local_keywords = {}   # 客户端本地关键词（等级2）
        self.learned_keywords = {} # 智能学习的关键词（等级3）
        
        # 关键词统计数据
        self.keyword_stats = {
            'usage_count': defaultdict(int),
            'trigger_history': [],
            'learning_data': defaultdict(list),
            'last_update': None
        }
        
        # 智能学习配置
        self.learning_config = {
            'enabled': True,
            'min_frequency': 3,  # 最小出现频率
            'learning_window': 7,  # 学习窗口（天）
            'confidence_threshold': 0.7,  # 置信度阈值
            'max_learned_keywords': 100,  # 最大学习关键词数量
            'auto_promote': True  # 自动提升高频关键词
        }
        
        # 同步配置
        self.sync_config = {
            'auto_sync': True,
            'sync_interval': 180,  # 3分钟同步一次
            'retry_count': 3,
            'retry_delay': 5
        }
        
        # 同步状态
        self.last_sync_time = None
        self.sync_thread = None
        self.is_syncing = False
        
        # 加载本地数据
        self.load_local_data()
        
        # 启动自动同步
        if self.sync_config['auto_sync']:
            self.start_auto_sync()
    
    def load_local_data(self):
        """加载本地数据"""
        try:
            # 加载服务器关键词缓存
            if os.path.exists(self.server_keywords_file):
                with open(self.server_keywords_file, 'r', encoding='utf-8') as f:
                    self.server_keywords = json.load(f)
                    print(f"已加载 {len(self.server_keywords)} 个服务器关键词")
            
            # 加载本地关键词
            if os.path.exists(self.local_keywords_file):
                with open(self.local_keywords_file, 'r', encoding='utf-8') as f:
                    self.local_keywords = json.load(f)
                    print(f"已加载 {len(self.local_keywords)} 个本地关键词")
            
            # 加载学习关键词
            if os.path.exists(self.learned_keywords_file):
                with open(self.learned_keywords_file, 'r', encoding='utf-8') as f:
                    self.learned_keywords = json.load(f)
                    print(f"已加载 {len(self.learned_keywords)} 个学习关键词")
            
            # 加载统计数据
            if os.path.exists(self.keyword_stats_file):
                with open(self.keyword_stats_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    self.keyword_stats.update(data)
                    # 转换defaultdict
                    self.keyword_stats['usage_count'] = defaultdict(int, self.keyword_stats.get('usage_count', {}))
                    self.keyword_stats['learning_data'] = defaultdict(list, self.keyword_stats.get('learning_data', {}))
            
            # 初始化默认关键词
            if not any([self.server_keywords, self.local_keywords]):
                self._init_default_keywords()
                
        except Exception as e:
            print(f"加载本地关键词数据失败: {str(e)}")
            self._init_default_keywords()
    
    def _init_default_keywords(self):
        """初始化默认关键词"""
        # 默认本地关键词
        self.local_keywords = {
            "emergency": {
                "keywords": ["紧急", "急", "火灾", "爆炸", "事故"],
                "category": "紧急事件",
                "priority": 1,
                "action": "immediate_forward",
                "enabled": True,
                "weight": 10
            },
            "power_outage": {
                "keywords": ["停电", "断电", "没电", "跳闸", "电力故障"],
                "category": "电力故障",
                "priority": 2,
                "action": "forward_and_reply",
                "enabled": True,
                "weight": 8
            },
            "equipment_failure": {
                "keywords": ["故障", "坏了", "不工作", "损坏", "维修"],
                "category": "设备故障",
                "priority": 3,
                "action": "forward",
                "enabled": True,
                "weight": 6
            },
            "service_request": {
                "keywords": ["报修", "维护", "检查", "咨询", "申请"],
                "category": "服务请求",
                "priority": 4,
                "action": "auto_reply",
                "enabled": True,
                "weight": 4
            },
            "complaint": {
                "keywords": ["投诉", "不满", "问题", "建议", "意见"],
                "category": "投诉建议",
                "priority": 5,
                "action": "log_and_reply",
                "enabled": True,
                "weight": 3
            }
        }
        
        print("已初始化默认关键词")
    
    def save_local_data(self):
        """保存本地数据"""
        try:
            # 保存服务器关键词缓存
            with open(self.server_keywords_file, 'w', encoding='utf-8') as f:
                json.dump(self.server_keywords, f, ensure_ascii=False, indent=2)
            
            # 保存本地关键词
            with open(self.local_keywords_file, 'w', encoding='utf-8') as f:
                json.dump(self.local_keywords, f, ensure_ascii=False, indent=2)
            
            # 保存学习关键词
            with open(self.learned_keywords_file, 'w', encoding='utf-8') as f:
                json.dump(self.learned_keywords, f, ensure_ascii=False, indent=2)
            
            # 保存统计数据
            stats_to_save = {
                'usage_count': dict(self.keyword_stats['usage_count']),
                'trigger_history': self.keyword_stats['trigger_history'],
                'learning_data': dict(self.keyword_stats['learning_data']),
                'last_update': datetime.now().isoformat()
            }
            with open(self.keyword_stats_file, 'w', encoding='utf-8') as f:
                json.dump(stats_to_save, f, ensure_ascii=False, indent=2)
            
            print("关键词数据已保存到本地")
            
        except Exception as e:
            print(f"保存关键词数据失败: {str(e)}")
    
    def sync_from_server(self) -> bool:
        """从服务器同步关键词"""
        try:
            print("开始从服务器同步关键词...")
            
            response = self.session.get(
                f"{self.server_url}/api/keywords/sync",
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    server_data = data.get('data', {})
                    
                    # 更新服务器关键词
                    if 'keywords' in server_data:
                        self.server_keywords = server_data['keywords']
                        print(f"同步了 {len(self.server_keywords)} 个服务器关键词")
                    
                    # 更新配置
                    if 'config' in server_data:
                        self.learning_config.update(server_data['config'])
                        print("同步了学习配置")
                    
                    self.last_sync_time = datetime.now()
                    self.save_local_data()
                    return True
            
            print(f"同步关键词失败，状态码: {response.status_code}")
            return False
            
        except Exception as e:
            print(f"同步关键词时出错: {str(e)}")
            return False
    
    def upload_learned_keywords(self) -> bool:
        """上传学习到的关键词到服务器"""
        try:
            if not self.learned_keywords:
                return True
            
            # 准备上传数据
            upload_data = {
                'learned_keywords': self.learned_keywords,
                'usage_stats': dict(self.keyword_stats['usage_count']),
                'client_id': self._get_client_id()
            }
            
            response = self.session.post(
                f"{self.server_url}/api/keywords/upload_learned",
                json=upload_data,
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    print("学习关键词上传成功")
                    return True
            
            print(f"上传学习关键词失败，状态码: {response.status_code}")
            return False
            
        except Exception as e:
            print(f"上传学习关键词时出错: {str(e)}")
            return False
    
    def check_keyword_match(self, message: str) -> Tuple[bool, Optional[Dict]]:
        """检查消息是否匹配关键词（按优先级）"""
        message_lower = message.lower()
        
        # 等级1：服务器关键词（最高优先级）
        match_result = self._check_keywords_in_group(message_lower, self.server_keywords, "服务器")
        if match_result[0]:
            return match_result
        
        # 等级2：本地关键词
        match_result = self._check_keywords_in_group(message_lower, self.local_keywords, "本地")
        if match_result[0]:
            return match_result
        
        # 等级3：学习关键词
        match_result = self._check_keywords_in_group(message_lower, self.learned_keywords, "学习")
        if match_result[0]:
            return match_result
        
        # 智能学习：记录未匹配的消息用于学习
        if self.learning_config['enabled']:
            self._record_for_learning(message)
        
        return False, None
    
    def _check_keywords_in_group(self, message: str, keyword_group: Dict, group_type: str) -> Tuple[bool, Optional[Dict]]:
        """在指定关键词组中检查匹配"""
        for group_id, group_data in keyword_group.items():
            if not group_data.get('enabled', True):
                continue
            
            keywords = group_data.get('keywords', [])
            for keyword in keywords:
                if keyword.lower() in message:
                    # 记录使用统计
                    self._record_keyword_usage(keyword, group_type)
                    
                    # 返回匹配结果
                    return True, {
                        'keyword': keyword,
                        'group_id': group_id,
                        'group_type': group_type,
                        'category': group_data.get('category', '未分类'),
                        'priority': group_data.get('priority', 999),
                        'action': group_data.get('action', 'log'),
                        'weight': group_data.get('weight', 1)
                    }
        
        return False, None
    
    def _record_keyword_usage(self, keyword: str, group_type: str):
        """记录关键词使用统计"""
        usage_key = f"{keyword}_{group_type}"
        self.keyword_stats['usage_count'][usage_key] += 1
        
        # 记录触发历史
        self.keyword_stats['trigger_history'].append({
            'keyword': keyword,
            'group_type': group_type,
            'timestamp': datetime.now().isoformat()
        })
        
        # 限制历史记录数量
        if len(self.keyword_stats['trigger_history']) > 1000:
            self.keyword_stats['trigger_history'] = self.keyword_stats['trigger_history'][-500:]
    
    def _record_for_learning(self, message: str):
        """记录消息用于智能学习"""
        try:
            # 提取潜在关键词
            potential_keywords = self._extract_potential_keywords(message)
            
            for keyword in potential_keywords:
                self.keyword_stats['learning_data'][keyword].append({
                    'message': message,
                    'timestamp': datetime.now().isoformat()
                })
            
            # 定期分析学习数据
            self._analyze_learning_data()
            
        except Exception as e:
            print(f"记录学习数据时出错: {str(e)}")
    
    def _extract_potential_keywords(self, message: str) -> List[str]:
        """从消息中提取潜在关键词"""
        # 简单的关键词提取逻辑
        # 可以后续集成更复杂的NLP算法
        
        # 移除标点符号和数字
        cleaned_message = re.sub(r'[^\u4e00-\u9fa5a-zA-Z\s]', '', message)
        
        # 分词（简单按空格和常见分隔符）
        words = re.split(r'[\s，。！？；：]', cleaned_message)
        
        # 过滤有效词汇
        keywords = []
        for word in words:
            word = word.strip()
            if len(word) >= 2 and len(word) <= 6:  # 长度在2-6之间的词
                keywords.append(word)
        
        return keywords
    
    def _analyze_learning_data(self):
        """分析学习数据，生成新的关键词"""
        try:
            if not self.learning_config['enabled']:
                return
            
            # 计算关键词频率
            keyword_frequency = Counter()
            cutoff_time = datetime.now() - timedelta(days=self.learning_config['learning_window'])
            
            for keyword, records in self.keyword_stats['learning_data'].items():
                # 只统计学习窗口内的数据
                recent_records = [
                    r for r in records 
                    if datetime.fromisoformat(r['timestamp']) > cutoff_time
                ]
                keyword_frequency[keyword] = len(recent_records)
            
            # 识别高频关键词
            for keyword, frequency in keyword_frequency.items():
                if frequency >= self.learning_config['min_frequency']:
                    confidence = min(frequency / (self.learning_config['min_frequency'] * 2), 1.0)
                    
                    if confidence >= self.learning_config['confidence_threshold']:
                        self._promote_to_learned_keyword(keyword, confidence, frequency)
            
            # 清理过期数据
            self._cleanup_learning_data(cutoff_time)
            
        except Exception as e:
            print(f"分析学习数据时出错: {str(e)}")
    
    def _promote_to_learned_keyword(self, keyword: str, confidence: float, frequency: int):
        """将高频词提升为学习关键词"""
        try:
            # 检查是否已存在
            if any(keyword in group.get('keywords', []) 
                   for group in [self.server_keywords, self.local_keywords, self.learned_keywords].values() 
                   for group in group.values()):
                return
            
            # 检查学习关键词数量限制
            if len(self.learned_keywords) >= self.learning_config['max_learned_keywords']:
                # 移除置信度最低的关键词
                self._remove_lowest_confidence_keyword()
            
            # 生成学习关键词ID
            learned_id = f"learned_{len(self.learned_keywords) + 1}"
            
            # 添加到学习关键词
            self.learned_keywords[learned_id] = {
                'keywords': [keyword],
                'category': '智能学习',
                'priority': 6,
                'action': 'log',
                'enabled': True,
                'weight': int(confidence * 5),
                'confidence': confidence,
                'frequency': frequency,
                'learned_time': datetime.now().isoformat()
            }
            
            print(f"新学习关键词: {keyword} (置信度: {confidence:.2f}, 频率: {frequency})")
            
        except Exception as e:
            print(f"提升学习关键词时出错: {str(e)}")
    
    def _remove_lowest_confidence_keyword(self):
        """移除置信度最低的学习关键词"""
        if not self.learned_keywords:
            return
        
        lowest_confidence = float('inf')
        lowest_id = None
        
        for keyword_id, keyword_data in self.learned_keywords.items():
            confidence = keyword_data.get('confidence', 0)
            if confidence < lowest_confidence:
                lowest_confidence = confidence
                lowest_id = keyword_id
        
        if lowest_id:
            del self.learned_keywords[lowest_id]
            print(f"移除低置信度关键词: {lowest_id}")
    
    def _cleanup_learning_data(self, cutoff_time: datetime):
        """清理过期的学习数据"""
        for keyword in list(self.keyword_stats['learning_data'].keys()):
            records = self.keyword_stats['learning_data'][keyword]
            recent_records = [
                r for r in records 
                if datetime.fromisoformat(r['timestamp']) > cutoff_time
            ]
            
            if recent_records:
                self.keyword_stats['learning_data'][keyword] = recent_records
            else:
                del self.keyword_stats['learning_data'][keyword]
    
    def get_all_keywords(self) -> Dict:
        """获取所有关键词"""
        return {
            'server_keywords': self.server_keywords,
            'local_keywords': self.local_keywords,
            'learned_keywords': self.learned_keywords
        }
    
    def get_keyword_statistics(self) -> Dict:
        """获取关键词统计信息"""
        return {
            'total_server_keywords': len(self.server_keywords),
            'total_local_keywords': len(self.local_keywords),
            'total_learned_keywords': len(self.learned_keywords),
            'usage_stats': dict(self.keyword_stats['usage_count']),
            'recent_triggers': self.keyword_stats['trigger_history'][-10:],
            'learning_data_size': len(self.keyword_stats['learning_data']),
            'last_sync_time': self.last_sync_time.isoformat() if self.last_sync_time else None
        }
    
    def start_auto_sync(self):
        """启动自动同步"""
        if self.is_syncing:
            return
        
        self.is_syncing = True
        self.sync_thread = threading.Thread(
            target=self._auto_sync_loop,
            daemon=True
        )
        self.sync_thread.start()
        print("关键词自动同步已启动")
    
    def stop_auto_sync(self):
        """停止自动同步"""
        self.is_syncing = False
        if self.sync_thread and self.sync_thread.is_alive():
            self.sync_thread.join(timeout=5)
        print("关键词自动同步已停止")
    
    def _auto_sync_loop(self):
        """自动同步循环"""
        while self.is_syncing:
            try:
                # 从服务器同步
                self.sync_from_server()
                
                # 上传学习关键词
                self.upload_learned_keywords()
                
                # 保存本地数据
                self.save_local_data()
                
                # 等待下次同步
                time.sleep(self.sync_config['sync_interval'])
                
            except Exception as e:
                print(f"自动同步循环出错: {str(e)}")
                time.sleep(30)  # 出错时等待30秒
    
    def _get_client_id(self) -> str:
        """获取客户端ID"""
        # 简单的客户端ID生成，实际应用中可以使用更复杂的方法
        import socket
        import hashlib
        
        hostname = socket.gethostname()
        client_id = hashlib.md5(hostname.encode()).hexdigest()[:8]
        return client_id
    
    def cleanup(self):
        """清理资源"""
        self.stop_auto_sync()
        self.save_local_data()
        print("关键词管理器资源已清理")

# 全局实例
_keyword_manager = None

def get_keyword_manager(server_url: str = "http://localhost:8000") -> KeywordManager:
    """获取关键词管理器实例（单例模式）"""
    global _keyword_manager
    if _keyword_manager is None:
        _keyword_manager = KeywordManager(server_url)
    return _keyword_manager

def cleanup_keyword_manager():
    """清理关键词管理器实例"""
    global _keyword_manager
    if _keyword_manager:
        _keyword_manager.cleanup()
        _keyword_manager = None