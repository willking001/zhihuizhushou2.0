# -*- coding: utf-8 -*-
"""
消息模板管理模块
实现转发消息模板和回复消息模板的管理功能
按照服务器设置的内容进行模板配置和同步
"""

import json
import os
import requests
from datetime import datetime
from typing import Dict, List, Optional, Any
import threading
import time

class MessageTemplateManager:
    """消息模板管理器"""
    
    def __init__(self, server_url: str = "http://localhost:8000"):
        """
        初始化消息模板管理器
        
        Args:
            server_url: 服务器地址
        """
        self.server_url = server_url
        self.session = requests.Session()
        
        # 本地缓存文件路径
        self.cache_dir = "template_cache"
        self.forward_template_file = os.path.join(self.cache_dir, "forward_templates.json")
        self.reply_template_file = os.path.join(self.cache_dir, "reply_templates.json")
        
        # 确保缓存目录存在
        os.makedirs(self.cache_dir, exist_ok=True)
        
        # 模板数据
        self.forward_templates = {}
        self.reply_templates = {}
        
        # 同步配置
        self.sync_config = {
            'auto_sync': True,
            'sync_interval': 300,  # 5分钟同步一次
            'retry_count': 3,
            'retry_delay': 5
        }
        
        # 同步状态
        self.last_sync_time = None
        self.sync_thread = None
        self.is_syncing = False
        
        # 加载本地缓存
        self.load_local_cache()
        
        # 启动自动同步
        if self.sync_config['auto_sync']:
            self.start_auto_sync()
    
    def load_local_cache(self):
        """加载本地缓存的模板"""
        try:
            # 加载转发模板
            if os.path.exists(self.forward_template_file):
                with open(self.forward_template_file, 'r', encoding='utf-8') as f:
                    self.forward_templates = json.load(f)
                    print(f"已加载 {len(self.forward_templates)} 个转发模板")
            
            # 加载回复模板
            if os.path.exists(self.reply_template_file):
                with open(self.reply_template_file, 'r', encoding='utf-8') as f:
                    self.reply_templates = json.load(f)
                    print(f"已加载 {len(self.reply_templates)} 个回复模板")
                    
        except Exception as e:
            print(f"加载本地模板缓存失败: {str(e)}")
            # 初始化默认模板
            self._init_default_templates()
    
    def _init_default_templates(self):
        """初始化默认模板"""
        # 默认转发模板
        self.forward_templates = {
            "urgent": {
                "name": "紧急事件转发",
                "template": "🚨 紧急通知\n\n原始消息：{original_message}\n发送人：{sender}\n时间：{time}\n\n请及时处理！",
                "keywords": ["紧急", "故障", "停电", "事故"],
                "priority": 1,
                "enabled": True,
                "header": "紧急通知",
                "attachmentRule": "include",
                "dataMasking": False,
                "maskingRules": []
            },
            "normal": {
                "name": "普通消息转发",
                "template": "📢 消息转发\n\n{original_message}\n\n来源：{sender}\n时间：{time}",
                "keywords": ["报修", "咨询", "投诉"],
                "priority": 2,
                "enabled": True,
                "header": "消息转发",
                "attachmentRule": "include",
                "dataMasking": False,
                "maskingRules": []
            }
        }
        
        # 默认回复模板
        self.reply_templates = {
            "auto_reply": {
                "name": "自动回复",
                "template": "您好！我们已收到您的消息：\n\n{original_message}\n\n我们会尽快处理，感谢您的耐心等待。\n\n回复时间：{time}",
                "conditions": ["关键词匹配"],
                "enabled": True
            },
            "working_hours": {
                "name": "工作时间回复",
                "template": "您好！我们的工作时间是周一至周五 8:00-18:00。\n\n您的消息：{original_message}\n\n我们会在工作时间内及时回复您。",
                "conditions": ["非工作时间"],
                "enabled": True
            }
        }
    
    def save_local_cache(self):
        """保存模板到本地缓存"""
        try:
            # 保存转发模板
            with open(self.forward_template_file, 'w', encoding='utf-8') as f:
                json.dump(self.forward_templates, f, ensure_ascii=False, indent=2)
            
            # 保存回复模板
            with open(self.reply_template_file, 'w', encoding='utf-8') as f:
                json.dump(self.reply_templates, f, ensure_ascii=False, indent=2)
                
            print("模板已保存到本地缓存")
            
        except Exception as e:
            print(f"保存本地模板缓存失败: {str(e)}")
    
    def sync_from_server(self) -> bool:
        """从服务器同步模板"""
        try:
            print("开始从服务器同步模板...")
            
            # 同步转发模板
            forward_success = self._sync_forward_templates()
            
            # 同步回复模板
            reply_success = self._sync_reply_templates()
            
            if forward_success and reply_success:
                self.last_sync_time = datetime.now()
                self.save_local_cache()
                print("模板同步成功")
                return True
            else:
                print("模板同步部分失败")
                return False
                
        except Exception as e:
            print(f"模板同步失败: {str(e)}")
            return False
    
    def _sync_forward_templates(self) -> bool:
        """同步转发模板"""
        try:
            response = self.session.get(
                f"{self.server_url}/api/system/templates?type=forward",
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    server_templates = data.get('data', {})
                    self.forward_templates.update(server_templates)
                    print(f"同步了 {len(server_templates)} 个转发模板")
                    return True
            
            print(f"同步转发模板失败，状态码: {response.status_code}")
            return False
            
        except Exception as e:
            print(f"同步转发模板时出错: {str(e)}")
            return False
    
    def _sync_reply_templates(self) -> bool:
        """同步回复模板"""
        try:
            response = self.session.get(
                f"{self.server_url}/api/system/templates?type=reply",
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    server_templates = data.get('data', {})
                    self.reply_templates.update(server_templates)
                    print(f"同步了 {len(server_templates)} 个回复模板")
                    return True
            
            print(f"同步回复模板失败，状态码: {response.status_code}")
            return False
            
        except Exception as e:
            print(f"同步回复模板时出错: {str(e)}")
            return False
    
    def get_forward_template(self, keyword: str = None, priority: int = None) -> Optional[Dict]:
        """获取转发模板"""
        try:
            # 如果指定了关键词，查找匹配的模板
            if keyword:
                for template_id, template in self.forward_templates.items():
                    if not template.get('enabled', True):
                        continue
                    
                    keywords = template.get('keywords', [])
                    if any(kw.lower() in keyword.lower() for kw in keywords):
                        return template
            
            # 如果指定了优先级，查找对应优先级的模板
            if priority:
                for template_id, template in self.forward_templates.items():
                    if template.get('priority') == priority and template.get('enabled', True):
                        return template
            
            # 返回默认模板
            for template_id, template in self.forward_templates.items():
                if template.get('enabled', True):
                    return template
            
            return None
            
        except Exception as e:
            print(f"获取转发模板失败: {str(e)}")
            return None
    
    def get_reply_template(self, condition: str = None) -> Optional[Dict]:
        """获取回复模板"""
        try:
            # 如果指定了条件，查找匹配的模板
            if condition:
                for template_id, template in self.reply_templates.items():
                    if not template.get('enabled', True):
                        continue
                    
                    conditions = template.get('conditions', [])
                    if condition in conditions:
                        return template
            
            # 返回默认模板
            for template_id, template in self.reply_templates.items():
                if template.get('enabled', True):
                    return template
            
            return None
            
        except Exception as e:
            print(f"获取回复模板失败: {str(e)}")
            return None
    
    def format_message(self, template: Dict, variables: Dict) -> Dict:
        """格式化消息模板
        
        Args:
            template: 消息模板
            variables: 变量字典
            
        Returns:
            Dict: 包含格式化后的消息内容、处理后的附件列表和其他元数据
        """
        try:
            template_text = template.get('template', '')
            
            # 添加默认变量
            default_vars = {
                'time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                'date': datetime.now().strftime('%Y-%m-%d'),
                'sender': variables.get('sender', '未知'),
                'original_message': variables.get('original_message', '')
            }
            
            # 合并变量
            all_vars = {**default_vars, **variables}
            
            # 替换模板变量
            formatted_message = template_text
            for key, value in all_vars.items():
                formatted_message = formatted_message.replace(f'{{{key}}}', str(value))
            
            # 处理附件
            attachments = variables.get('attachments', [])
            processed_attachments = self._process_attachments(attachments, template)
            
            # 应用数据脱敏
            if template.get('dataMasking', False):
                formatted_message = self._apply_data_masking(formatted_message, template.get('maskingRules', []))
            
            # 构建消息头
            header = template.get('header', '')
            if header:
                header_text = f"{header}\n\n"
            else:
                header_text = ""
            
            # 返回完整的格式化结果
            return {
                'content': header_text + formatted_message,
                'attachments': processed_attachments,
                'header': header,
                'template_id': next((tid for tid, tmpl in self.forward_templates.items() if tmpl == template), None),
                'template_name': template.get('name', ''),
                'formatted_time': default_vars['time']
            }
            
        except Exception as e:
            print(f"格式化消息模板失败: {str(e)}")
            return {
                'content': template.get('template', ''),
                'attachments': variables.get('attachments', []),
                'error': str(e)
            }
    
    def _process_attachments(self, attachments: List[str], template: Dict) -> List[str]:
        """处理附件
        
        Args:
            attachments: 原始附件列表
            template: 消息模板
            
        Returns:
            List[str]: 处理后的附件列表
        """
        if not attachments:
            return []
            
        attachment_rule = template.get('attachmentRule', 'include')
        
        if attachment_rule == 'exclude':
            # 不包含附件
            return []
        elif attachment_rule == 'first':
            # 仅包含第一个附件
            return attachments[:1] if attachments else []
        elif attachment_rule == 'custom':
            # 自定义规则，可以在这里实现更复杂的附件处理逻辑
            # 例如：按照文件类型过滤、大小限制等
            # 目前简单实现为包含所有附件
            return attachments
        else:
            # 默认包含所有附件
            return attachments
    
    def _apply_data_masking(self, text: str, masking_rules: List[Dict]) -> str:
        """应用数据脱敏
        
        Args:
            text: 原始文本
            masking_rules: 脱敏规则列表，每个规则包含pattern和replacement
            
        Returns:
            str: 脱敏后的文本
        """
        import re
        
        if not masking_rules:
            return text
            
        masked_text = text
        
        for rule in masking_rules:
            pattern = rule.get('pattern', '')
            replacement = rule.get('replacement', '***')
            
            if pattern:
                try:
                    masked_text = re.sub(pattern, replacement, masked_text)
                except Exception as e:
                    print(f"应用脱敏规则失败: {str(e)}")
        
        return masked_text
    
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
        print("自动同步已启动")
    
    def stop_auto_sync(self):
        """停止自动同步"""
        self.is_syncing = False
        if self.sync_thread and self.sync_thread.is_alive():
            self.sync_thread.join(timeout=5)
        print("自动同步已停止")
    
    def _auto_sync_loop(self):
        """自动同步循环"""
        while self.is_syncing:
            try:
                # 执行同步
                self.sync_from_server()
                
                # 等待下次同步
                time.sleep(self.sync_config['sync_interval'])
                
            except Exception as e:
                print(f"自动同步循环出错: {str(e)}")
                time.sleep(30)  # 出错时等待30秒
    
    def get_template_list(self, template_type: str = 'all') -> Dict:
        """获取模板列表"""
        result = {}
        
        if template_type in ['all', 'forward']:
            result['forward'] = self.forward_templates
        
        if template_type in ['all', 'reply']:
            result['reply'] = self.reply_templates
        
        return result
    
    def get_sync_status(self) -> Dict:
        """获取同步状态"""
        return {
            'last_sync_time': self.last_sync_time.isoformat() if self.last_sync_time else None,
            'is_syncing': self.is_syncing,
            'auto_sync_enabled': self.sync_config['auto_sync'],
            'sync_interval': self.sync_config['sync_interval'],
            'forward_template_count': len(self.forward_templates),
            'reply_template_count': len(self.reply_templates)
        }
    
    def cleanup(self):
        """清理资源"""
        self.stop_auto_sync()
        self.save_local_cache()
        print("消息模板管理器资源已清理")

# 全局实例
_template_manager = None

def get_template_manager(server_url: str = "http://localhost:8000") -> MessageTemplateManager:
    """获取模板管理器实例（单例模式）"""
    global _template_manager
    if _template_manager is None:
        _template_manager = MessageTemplateManager(server_url)
    return _template_manager

def cleanup_template_manager():
    """清理模板管理器实例"""
    global _template_manager
    if _template_manager:
        _template_manager.cleanup()
        _template_manager = None