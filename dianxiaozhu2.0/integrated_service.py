# -*- coding: utf-8 -*-
"""
集成服务模块
实现关键词管理、智能问答和消息模板配置的联动功能
"""

import json
import os
import time
from datetime import datetime
from typing import Dict, List, Optional, Any, Tuple

# 导入相关模块
from keyword_manager import get_keyword_manager
from message_templates import get_template_manager
from qa_service import get_qa_service
from network_manager import get_network_manager

class IntegratedService:
    """集成服务，联动关键词管理、智能问答和消息模板配置"""
    
    def __init__(self, server_url: str = "http://localhost:8000"):
        """
        初始化集成服务
        
        Args:
            server_url: 服务器地址
        """
        self.server_url = server_url
        
        # 初始化各个服务模块
        self.keyword_manager = get_keyword_manager(server_url)
        self.template_manager = get_template_manager(server_url)
        self.qa_service = get_qa_service(server_url)
        self.network_manager = get_network_manager(server_url)
        
        # 本地缓存目录
        self.cache_dir = "integrated_cache"
        self.stats_file = os.path.join(self.cache_dir, "integrated_stats.json")
        
        # 确保缓存目录存在
        os.makedirs(self.cache_dir, exist_ok=True)
        
        # 统计数据
        self.stats = {
            'processed_messages': 0,
            'keyword_matches': 0,
            'qa_responses': 0,
            'template_usages': 0,
            'category_stats': {},
            'last_update': None
        }
        
        # 加载本地数据
        self.load_local_data()
    
    def load_local_data(self):
        """加载本地缓存的数据"""
        try:
            if os.path.exists(self.stats_file):
                with open(self.stats_file, 'r', encoding='utf-8') as f:
                    self.stats = json.load(f)
                    print(f"已加载集成服务统计数据")
        except Exception as e:
            print(f"加载集成服务数据失败: {str(e)}")
    
    def save_local_data(self):
        """保存数据到本地缓存"""
        try:
            # 更新最后更新时间
            self.stats['last_update'] = datetime.now().isoformat()
            
            with open(self.stats_file, 'w', encoding='utf-8') as f:
                json.dump(self.stats, f, ensure_ascii=False, indent=2)
                
            print("集成服务数据已保存到本地缓存")
            
        except Exception as e:
            print(f"保存集成服务数据失败: {str(e)}")
    
    def process_message(self, message: str, context: Optional[Dict] = None, session_id: Optional[str] = None) -> Dict:
        """
        处理消息，集成关键词匹配、智能问答和消息模板
        
        Args:
            message: 消息内容
            context: 上下文信息
            session_id: 会话ID
            
        Returns:
            Dict: 处理结果，包含回复内容、关键词匹配信息、转发信息等
        """
        try:
            # 更新统计
            self.stats['processed_messages'] += 1
            
            # 步骤1: 关键词匹配
            keyword_match, keyword_info = self.keyword_manager.check_keyword_match(message)
            
            # 更新关键词匹配统计
            if keyword_match:
                self.stats['keyword_matches'] += 1
                
                # 更新分类统计
                category = keyword_info.get('category', '未分类')
                if category not in self.stats['category_stats']:
                    self.stats['category_stats'][category] = 0
                self.stats['category_stats'][category] += 1
            
            # 步骤2: 获取转发模板
            forward_template = None
            if keyword_match:
                # 根据关键词或优先级获取转发模板
                forward_template = self.template_manager.get_forward_template(
                    keyword=keyword_info.get('keyword'),
                    priority=keyword_info.get('priority')
                )
            
            # 步骤3: 智能问答处理
            qa_context = context or {}
            if keyword_match:
                # 将关键词信息添加到问答上下文
                qa_context.update({
                    'keyword_match': True,
                    'keyword': keyword_info.get('keyword', ''),
                    'category': keyword_info.get('category', ''),
                    'priority': keyword_info.get('priority', 0)
                })
            
            # 获取智能问答回复
            qa_response = self.qa_service.ask(message, session_id, qa_context)
            self.stats['qa_responses'] += 1
            
            # 步骤4: 准备回复内容
            result = {
                'original_message': message,
                'keyword_match': keyword_match,
                'keyword_info': keyword_info if keyword_match else None,
                'qa_response': qa_response,
                'forward_required': False,
                'forward_content': None,
                'reply_content': qa_response.get('answer', '')
            }
            
            # 步骤5: 如果需要转发，格式化转发内容
            if keyword_match and forward_template:
                self.stats['template_usages'] += 1
                
                # 准备变量替换数据
                template_data = {
                    'original_message': message,
                    'keyword': keyword_info.get('keyword', ''),
                    'category': keyword_info.get('category', ''),
                    'priority': str(keyword_info.get('priority', 0)),
                    'qa_answer': qa_response.get('answer', ''),
                    'qa_confidence': str(qa_response.get('confidence', 0)),
                    'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                }
                
                # 格式化转发模板
                formatted = self.template_manager.format_message(forward_template, template_data)
                
                result['forward_required'] = True
                result['forward_content'] = formatted
            
            # 如果需要转发且网络不可用，保存到离线队列
            if result['forward_required'] and not self.network_manager.is_online:
                message_data = {
                    'message': message,
                    'context': context,
                    'session_id': session_id,
                    'result': result,
                    'timestamp': datetime.now().isoformat()
                }
                
                self.network_manager.add_pending_upload(
                    'message',
                    message_data,
                    priority=3 if keyword_info and keyword_info.get('priority', 0) >= 3 else 1
                )
                
                print(f"网络不可用，消息已保存到离线队列: {message[:50]}...")
            
            # 保存统计数据
            self.save_local_data()
            
            return result
            
        except Exception as e:
            print(f"集成处理消息失败: {str(e)}")
            return {
                'original_message': message,
                'keyword_match': False,
                'keyword_info': None,
                'qa_response': {
                    'answer': '处理消息时发生错误，请稍后再试。',
                    'confidence': 0.0,
                    'sources': [],
                    'sessionId': session_id
                },
                'forward_required': False,
                'forward_content': None,
                'reply_content': '处理消息时发生错误，请稍后再试。'
            }
    
    def get_stats(self) -> Dict:
        """获取集成服务统计信息"""
        # 更新最后更新时间
        self.stats['last_update'] = datetime.now().isoformat()
        return self.stats
    
    def sync_all(self) -> Dict:
        """
        同步所有模块数据
        
        Returns:
            Dict: 同步结果
        """
        results = {
            'keyword_sync': False,
            'template_sync': False,
            'errors': []
        }
        
        try:
            # 同步关键词
            keyword_result = self.keyword_manager.sync_from_server()
            results['keyword_sync'] = keyword_result.get('success', False)
            if not results['keyword_sync']:
                results['errors'].append(f"关键词同步失败: {keyword_result.get('message', '')}")
                
            # 同步模板
            template_result = self.template_manager.sync_from_server()
            results['template_sync'] = template_result.get('success', False)
            if not results['template_sync']:
                results['errors'].append(f"模板同步失败: {template_result.get('message', '')}")
                
            return results
            
        except Exception as e:
            results['errors'].append(f"同步过程发生错误: {str(e)}")
            return results
    
    def cleanup(self):
        """清理资源"""
        self.save_local_data()
        print("集成服务资源已清理")

# 全局实例
_integrated_service = None

def get_integrated_service(server_url: str = "http://localhost:8000") -> IntegratedService:
    """获取集成服务实例（单例模式）"""
    global _integrated_service
    if _integrated_service is None:
        _integrated_service = IntegratedService(server_url)
    return _integrated_service

def cleanup_integrated_service():
    """清理集成服务实例"""
    global _integrated_service
    if _integrated_service:
        _integrated_service.cleanup()
        _integrated_service = None