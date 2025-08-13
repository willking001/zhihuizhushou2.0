# -*- coding: utf-8 -*-
"""
智能问答服务模块
实现基于知识库的智能问答功能
支持与关键词管理和消息模板的联动
"""

import json
import os
import requests
import threading
import time
from datetime import datetime
from typing import Dict, List, Optional, Any, Tuple
from collections import defaultdict
import re

# 导入关键词管理和消息模板模块
from keyword_manager import get_keyword_manager
from message_templates import get_template_manager

class QAService:
    """智能问答服务"""
    
    def __init__(self, server_url: str = "http://localhost:8000"):
        """
        初始化智能问答服务
        
        Args:
            server_url: 服务器地址
        """
        self.server_url = server_url
        self.session = requests.Session()
        
        # 本地缓存文件路径
        self.cache_dir = "qa_cache"
        self.sessions_file = os.path.join(self.cache_dir, "sessions.json")
        self.qa_stats_file = os.path.join(self.cache_dir, "qa_stats.json")
        
        # 确保缓存目录存在
        os.makedirs(self.cache_dir, exist_ok=True)
        
        # 会话数据
        self.sessions = {}
        
        # 统计数据
        self.qa_stats = {
            'total_questions': 0,
            'answered_questions': 0,
            'unanswered_questions': 0,
            'avg_confidence': 0.0,
            'question_categories': defaultdict(int),
            'last_update': None
        }
        
        # 关键词管理器和消息模板管理器
        self.keyword_manager = get_keyword_manager(server_url)
        self.template_manager = get_template_manager(server_url)
        
        # 加载本地数据
        self.load_local_data()
    
    def load_local_data(self):
        """加载本地缓存的数据"""
        try:
            # 加载会话数据
            if os.path.exists(self.sessions_file):
                with open(self.sessions_file, 'r', encoding='utf-8') as f:
                    self.sessions = json.load(f)
                    print(f"已加载 {len(self.sessions)} 个会话")
            
            # 加载统计数据
            if os.path.exists(self.qa_stats_file):
                with open(self.qa_stats_file, 'r', encoding='utf-8') as f:
                    stats = json.load(f)
                    # 转换defaultdict
                    stats['question_categories'] = defaultdict(int, stats['question_categories'])
                    self.qa_stats = stats
                    print(f"已加载问答统计数据")
                    
        except Exception as e:
            print(f"加载本地问答数据失败: {str(e)}")
    
    def save_local_data(self):
        """保存数据到本地缓存"""
        try:
            # 保存会话数据
            with open(self.sessions_file, 'w', encoding='utf-8') as f:
                json.dump(self.sessions, f, ensure_ascii=False, indent=2)
            
            # 保存统计数据 (转换defaultdict为普通dict)
            stats_copy = self.qa_stats.copy()
            stats_copy['question_categories'] = dict(stats_copy['question_categories'])
            with open(self.qa_stats_file, 'w', encoding='utf-8') as f:
                json.dump(stats_copy, f, ensure_ascii=False, indent=2)
                
            print("问答数据已保存到本地缓存")
            
        except Exception as e:
            print(f"保存本地问答数据失败: {str(e)}")
    
    def ask(self, question: str, session_id: Optional[str] = None, context: Optional[Dict] = None) -> Dict:
        """
        提问并获取回答
        
        Args:
            question: 问题内容
            session_id: 会话ID，用于多轮对话
            context: 上下文信息
            
        Returns:
            Dict: 包含回答内容、置信度、来源和会话ID
        """
        try:
            # 更新统计
            self.qa_stats['total_questions'] += 1
            
            # 创建或获取会话
            if not session_id:
                session_id = f"session_{int(time.time())}_{hash(question) % 10000}"
                self.sessions[session_id] = {
                    'created_at': datetime.now().isoformat(),
                    'last_active': datetime.now().isoformat(),
                    'questions': [],
                    'context': context or {}
                }
            elif session_id not in self.sessions:
                self.sessions[session_id] = {
                    'created_at': datetime.now().isoformat(),
                    'last_active': datetime.now().isoformat(),
                    'questions': [],
                    'context': context or {}
                }
            else:
                # 更新现有会话
                self.sessions[session_id]['last_active'] = datetime.now().isoformat()
                self.sessions[session_id]['context'].update(context or {})
            
            # 检查关键词匹配
            keyword_match, keyword_info = self.keyword_manager.check_keyword_match(question)
            
            # 准备请求数据
            request_data = {
                'question': question,
                'sessionId': session_id,
                'context': self.sessions[session_id]['context']
            }
            
            # 如果匹配到关键词，添加到上下文
            if keyword_match and keyword_info:
                request_data['context']['keyword_match'] = {
                    'keyword': keyword_info.get('keyword', ''),
                    'category': keyword_info.get('category', ''),
                    'priority': keyword_info.get('priority', 0)
                }
                
                # 更新问题分类统计
                category = keyword_info.get('category', '未分类')
                self.qa_stats['question_categories'][category] += 1
            
            # 尝试从服务器获取回答
            server_answer = self._get_answer_from_server(request_data)
            
            if server_answer and server_answer.get('answer'):
                # 服务器回答成功
                answer_data = {
                    'answer': server_answer.get('answer', ''),
                    'confidence': server_answer.get('confidence', 0.0),
                    'sources': server_answer.get('sources', []),
                    'sessionId': session_id
                }
                
                # 更新统计
                self.qa_stats['answered_questions'] += 1
                self.qa_stats['avg_confidence'] = ((self.qa_stats['avg_confidence'] * (self.qa_stats['answered_questions'] - 1)) + 
                                                 answer_data['confidence']) / self.qa_stats['answered_questions']
                
                # 记录问题和回答
                self.sessions[session_id]['questions'].append({
                    'question': question,
                    'answer': answer_data['answer'],
                    'confidence': answer_data['confidence'],
                    'timestamp': datetime.now().isoformat(),
                    'keyword_match': keyword_match
                })
                
                # 保存数据
                self.save_local_data()
                
                return answer_data
            else:
                # 服务器回答失败，使用模板回复
                self.qa_stats['unanswered_questions'] += 1
                
                # 根据关键词匹配选择合适的回复模板
                template_condition = "关键词匹配" if keyword_match else "未知问题"
                template = self.template_manager.get_reply_template(template_condition)
                
                if template:
                    # 使用模板格式化回复
                    formatted = self.template_manager.format_message(template, {
                        'original_message': question,
                        'keyword': keyword_info.get('keyword', '') if keyword_match else '',
                        'category': keyword_info.get('category', '') if keyword_match else ''
                    })
                    
                    answer_data = {
                        'answer': formatted.get('content', '抱歉，我无法回答这个问题。'),
                        'confidence': 0.5,  # 模板回复的默认置信度
                        'sources': ['本地模板'],
                        'sessionId': session_id
                    }
                else:
                    # 默认回复
                    answer_data = {
                        'answer': '抱歉，我无法回答这个问题。请尝试重新表述或联系人工客服。',
                        'confidence': 0.3,
                        'sources': [],
                        'sessionId': session_id
                    }
                
                # 记录问题和回答
                self.sessions[session_id]['questions'].append({
                    'question': question,
                    'answer': answer_data['answer'],
                    'confidence': answer_data['confidence'],
                    'timestamp': datetime.now().isoformat(),
                    'keyword_match': keyword_match
                })
                
                # 保存数据
                self.save_local_data()
                
                return answer_data
                
        except Exception as e:
            print(f"问答处理失败: {str(e)}")
            return {
                'answer': f"抱歉，处理您的问题时出现错误。请稍后再试。",
                'confidence': 0.0,
                'sources': [],
                'sessionId': session_id or f"error_{int(time.time())}"
            }
    
    def _get_answer_from_server(self, request_data: Dict) -> Optional[Dict]:
        """从服务器获取回答"""
        try:
            response = self.session.post(
                f"{self.server_url}/api/qa/ask",
                json=request_data,
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 200 and data.get('data'):
                    return data.get('data')
            
            print(f"从服务器获取回答失败，状态码: {response.status_code}")
            return None
            
        except Exception as e:
            print(f"从服务器获取回答时出错: {str(e)}")
            return None
    
    def get_session_history(self, session_id: str) -> List[Dict]:
        """获取会话历史"""
        if session_id in self.sessions:
            return self.sessions[session_id].get('questions', [])
        return []
    
    def clear_session(self, session_id: str) -> bool:
        """清除会话上下文"""
        if session_id in self.sessions:
            # 保留会话但清除问题历史和上下文
            self.sessions[session_id]['questions'] = []
            self.sessions[session_id]['context'] = {}
            self.sessions[session_id]['last_active'] = datetime.now().isoformat()
            self.save_local_data()
            return True
        return False
    
    def delete_session(self, session_id: str) -> bool:
        """删除会话"""
        if session_id in self.sessions:
            del self.sessions[session_id]
            self.save_local_data()
            return True
        return False
    
    def get_qa_stats(self) -> Dict:
        """获取问答统计信息"""
        # 更新最后更新时间
        self.qa_stats['last_update'] = datetime.now().isoformat()
        return self.qa_stats
    
    def cleanup(self):
        """清理资源"""
        self.save_local_data()
        print("智能问答服务资源已清理")

# 全局实例
_qa_service = None

def get_qa_service(server_url: str = "http://localhost:8000") -> QAService:
    """获取问答服务实例（单例模式）"""
    global _qa_service
    if _qa_service is None:
        _qa_service = QAService(server_url)
    return _qa_service

def cleanup_qa_service():
    """清理问答服务实例"""
    global _qa_service
    if _qa_service:
        _qa_service.cleanup()
        _qa_service = None