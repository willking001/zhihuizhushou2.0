#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json
import time

# 后端服务地址
BASE_URL = "http://localhost:8080"

def insert_business_rules():
    """插入业务规则"""
    print("开始插入业务规则...")
    
    # 1. 插入转发智能业务规则
    forward_rule = {
        "ruleName": "智能消息转发规则",
        "ruleDescription": "检测到重要关键词时自动转发给相关负责人",
        "ruleType": "MESSAGE_FORWARD",
        "priority": 1,
        "enabled": True,
        "effectiveDays": "1,2,3,4,5,6,7",
        "creatorId": 1
    }
    
    forward_rule_id = create_rule_with_conditions_and_actions(
        forward_rule,
        [
            {
                "conditionType": "KEYWORD_MATCH",
                "conditionValue": "紧急,故障,停电,断网,系统异常",
                "matchMode": "CONTAINS",
                "caseSensitive": False
            }
        ],
        [
            {
                "actionType": "MESSAGE_FORWARD",
                "actionConfig": '{"targetUsers":["admin","manager"],"message":"检测到重要消息，请及时处理","includeOriginal":true,"notificationMethod":"email"}',
                "executionOrder": 1
            }
        ]
    )
    
    if forward_rule_id:
        print(f"✅ 转发智能业务规则创建成功，ID: {forward_rule_id}")
    
    time.sleep(1)
    
    # 2. 插入回复业务规则
    reply_rule = {
        "ruleName": "智能自动回复规则",
        "ruleDescription": "用户咨询常见问题时自动回复标准答案",
        "ruleType": "AUTO_REPLY",
        "priority": 2,
        "enabled": True,
        "effectiveDays": "1,2,3,4,5,6,7",
        "creatorId": 1
    }
    
    reply_rule_id = create_rule_with_conditions_and_actions(
        reply_rule,
        [
            {
                "conditionType": "KEYWORD_MATCH",
                "conditionValue": "如何,怎么,什么时候,费用,价格,收费标准",
                "matchMode": "CONTAINS",
                "caseSensitive": False
            }
        ],
        [
            {
                "actionType": "AUTO_REPLY",
                "actionConfig": '{"replyMessage":"您好！感谢您的咨询。我们已收到您的问题，客服人员会在24小时内回复您。如有紧急情况，请拨打客服热线：400-123-4567。","delay":0,"autoClose":false}',
                "executionOrder": 1
            }
        ]
    )
    
    if reply_rule_id:
        print(f"✅ 回复业务规则创建成功，ID: {reply_rule_id}")

def create_rule_with_conditions_and_actions(rule_data, conditions, actions):
    """创建业务规则并添加条件和动作"""
    try:
        # 1. 创建业务规则
        response = requests.post(f"{BASE_URL}/api/business-rules", 
                               json=rule_data,
                               headers={'Content-Type': 'application/json'})
        if response.status_code != 200:
            print(f"❌ 规则创建失败: {response.status_code} - {response.text}")
            return None
        
        rule_result = response.json()
        rule_id = rule_result.get('id')
        print(f"📝 规则创建成功: {rule_data['ruleName']} (ID: {rule_id})")
        
        # 2. 添加条件
        for condition in conditions:
            try:
                response = requests.post(f"{BASE_URL}/api/business-rules/{rule_id}/conditions", 
                                       json=condition,
                                       headers={'Content-Type': 'application/json'})
                if response.status_code == 200:
                    print(f"  ✅ 条件添加成功: {condition['conditionType']}")
                else:
                    print(f"  ❌ 条件添加失败: {response.status_code} - {response.text}")
            except Exception as e:
                print(f"  ❌ 条件添加异常: {e}")
        
        # 3. 添加动作
        for action in actions:
            try:
                response = requests.post(f"{BASE_URL}/api/business-rules/{rule_id}/actions", 
                                       json=action,
                                       headers={'Content-Type': 'application/json'})
                if response.status_code == 200:
                    print(f"  ✅ 动作添加成功: {action['actionType']}")
                else:
                    print(f"  ❌ 动作添加失败: {response.status_code} - {response.text}")
            except Exception as e:
                print(f"  ❌ 动作添加异常: {e}")
        
        return rule_id
        
    except Exception as e:
        print(f"❌ 规则创建异常: {e}")
        return None

def insert_message_template():
    """插入消息模板"""
    print("\n开始插入消息模板...")
    
    template_data = {
        "name": "智能转发消息模板",
        "template": "🔔 重要消息转发\n\n原始消息：{{original_message}}\n发送人：{{sender_name}}\n发送时间：{{send_time}}\n消息类型：{{message_type}}\n\n⚠️ 此消息已被系统识别为重要消息，请及时处理！\n\n处理建议：\n1. 立即查看消息内容\n2. 评估紧急程度\n3. 采取相应措施\n4. 及时反馈处理结果",
        "type": "forward",
        "priority": 1,
        "enabled": True,
        "header": "【系统智能转发】",
        "attachmentRule": "转发所有附件",
        "dataMasking": False,
        "keywords": ["紧急", "故障", "停电", "断网", "系统异常", "重要", "urgent"],
        "conditions": [
            "contains(message, '紧急')",
            "contains(message, '故障')",
            "contains(message, '停电')",
            "priority >= 'high'"
        ]
    }
    
    try:
        response = requests.post(f"{BASE_URL}/api/system/templates", 
                               json=template_data,
                               headers={'Content-Type': 'application/json'})
        if response.status_code == 201:
            result = response.json()
            print("✅ 转发消息模板插入成功")
            print(f"  模板ID: {result.get('data', {}).get('id')}")
            print(f"  模板名称: {result.get('data', {}).get('name')}")
        else:
            print(f"❌ 转发消息模板插入失败: {response.status_code}")
            print(f"  错误信息: {response.text}")
    except Exception as e:
        print(f"❌ 插入转发消息模板时发生错误: {str(e)}")

def main():
    print("=== 智慧助手数据插入工具 ===")
    print(f"后端服务地址: {BASE_URL}")
    
    # 检查后端服务是否可用
    try:
        response = requests.get(f"{BASE_URL}/api/system/health", timeout=5)
        if response.status_code == 200:
            print("✅ 后端服务连接正常")
        else:
            print("⚠️ 后端服务响应异常")
    except Exception as e:
        print(f"❌ 无法连接到后端服务: {e}")
        print("请确保后端服务正在运行...")
        return
    
    # 插入业务规则
    insert_business_rules()
    
    # 插入消息模板
    insert_message_template()
    
    print("\n=== 数据插入完成 ===")

if __name__ == "__main__":
    main()