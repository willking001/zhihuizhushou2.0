#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
消息提示程序（微信联网版）
功能：登录验证、微信消息监控、消息转发、智能问答
使用CustomTkinter实现现代化UI设计
"""

import customtkinter as ctk
from tkinter import messagebox, scrolledtext
import tkinter as tk
import requests
import json
import threading
import time
from datetime import datetime, timedelta
import os
import sys
from typing import Dict, List, Optional
from PIL import Image, ImageTk

# 设置CustomTkinter外观
ctk.set_appearance_mode("dark")  # 深色模式
ctk.set_default_color_theme("blue")  # 蓝色主题

# 添加当前目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    from wechat_enhanced import WeChatEnhanced
except ImportError:
    print("警告：无法导入微信监控模块，部分功能可能不可用")
    WeChatEnhanced = None

# 导入智能问答和集成服务模块
try:
    from integrated_service import get_integrated_service, cleanup_integrated_service
    from qa_service import get_qa_service, cleanup_qa_service
    from keyword_manager import get_keyword_manager
    from message_templates import get_template_manager
    from network_manager import get_network_manager, cleanup_network_manager
    from config_manager import get_config_manager, cleanup_config_manager
except ImportError as e:
    print(f"警告：无法导入智能服务模块，部分功能可能不可用: {e}")

forward_manager = None
integrated_service = None

class ModernGridDesktopApp:
    def __init__(self):
        # 创建主窗口
        self.root = ctk.CTk()
        self.root.title("消息提示程序（微信联网版）")
        self.root.geometry("1200x800")
        self.root.minsize(1000, 700)
        
        # 现代化配色方案
        self.colors = {
            'primary': '#1f538d',
            'secondary': '#14375e', 
            'success': '#2fa572',
            'warning': '#f39c12',
            'danger': '#e74c3c',
            'info': '#3498db',
            'light': '#ecf0f1',
            'dark': '#2c3e50',
            'accent': '#9b59b6'
        }
        
        # 应用状态
        self.is_logged_in = False
        self.current_user = None
        self.monitoring_active = False
        self.wechat_monitor = None
        self.monitor_thread = None
        
        # 服务器配置
        self.server_host = "localhost"
        self.server_port = "8080"
        self.session = requests.Session()
        
        # 微信转发配置（简化版）
        self.forward_wechat_name = ""
        self.forward_keywords = "紧急,故障,停电,事故,报修"  # 本地关键词（等级2）
        self.server_keywords = ""  # 服务器关键词（等级1）
        
        # 初始化网络管理器和配置管理器
        try:
            self.network_manager = get_network_manager(self.server_url)
            self.config_manager = get_config_manager(self.server_url)
            print("网络管理器和配置管理器初始化成功")
        except Exception as e:
            print(f"初始化管理器失败: {str(e)}")
            self.network_manager = None
            self.config_manager = None
        
        # 加载简化转发配置
        self.load_simple_forward_config()
        
        # 获取服务器关键词
        self.fetch_server_keywords()
        
        # 消息统计
        self.message_stats = {
            "total_messages": 0,
            "today_messages": 0,
            "forwarded_count": 0
        }
        
        self.setup_ui()
        self.check_server_connection()
        
    def setup_ui(self):
        """设置现代化用户界面"""
        # 配置网格权重
        self.root.grid_columnconfigure(0, weight=1)
        self.root.grid_rowconfigure(0, weight=1)
        
        # 创建主容器
        self.main_container = ctk.CTkFrame(self.root, corner_radius=0)
        self.main_container.grid(row=0, column=0, sticky="nsew", padx=0, pady=0)
        self.main_container.grid_columnconfigure(1, weight=1)
        self.main_container.grid_rowconfigure(1, weight=1)
        
        # 创建侧边栏
        self.create_sidebar()
        
        # 创建主内容区域
        self.create_main_content()
        
        # 创建状态栏
        self.create_status_bar()
        
        # 初始显示登录界面
        self.show_login_view()
        
    def create_sidebar(self):
        """创建现代化侧边栏"""
        self.sidebar = ctk.CTkFrame(self.main_container, width=280, corner_radius=0)
        self.sidebar.grid(row=0, column=0, rowspan=3, sticky="nsew")
        self.sidebar.grid_propagate(False)
        
        # 应用标题
        self.logo_label = ctk.CTkLabel(
            self.sidebar, 
            text="⚡ 微信消息系统",
            font=ctk.CTkFont(size=24, weight="bold")
        )
        self.logo_label.grid(row=0, column=0, padx=20, pady=(30, 10))
        
        self.subtitle_label = ctk.CTkLabel(
            self.sidebar,
            text="智能服务 · 消息提示及转发",
            font=ctk.CTkFont(size=12),
            text_color=("gray70", "gray30")
        )
        self.subtitle_label.grid(row=1, column=0, padx=20, pady=(0, 30))
        
        # 导航按钮
        self.nav_frame = ctk.CTkFrame(self.sidebar, fg_color="transparent")
        self.nav_frame.grid(row=2, column=0, sticky="ew", padx=20, pady=10)
        
        # 登录/主页按钮
        self.home_btn = ctk.CTkButton(
            self.nav_frame,
            text="🏠 主页",
            font=ctk.CTkFont(size=14, weight="bold"),
            height=40,
            command=self.show_main_view
        )
        self.home_btn.grid(row=0, column=0, sticky="ew", pady=5)
        
        # 隐藏监控控制、统计分析和系统设置按钮
        # 这些功能已集成到主页面中
        
        self.nav_frame.grid_columnconfigure(0, weight=1)
        
        # 用户信息区域（登录后显示）
        self.user_info_frame = ctk.CTkFrame(self.sidebar)
        self.user_info_frame.grid(row=3, column=0, sticky="ew", padx=20, pady=20)
        
        self.user_avatar_label = ctk.CTkLabel(
            self.user_info_frame,
            text="👤",
            font=ctk.CTkFont(size=32)
        )
        self.user_avatar_label.grid(row=0, column=0, padx=15, pady=15)
        
        self.user_name_label = ctk.CTkLabel(
            self.user_info_frame,
            text="未登录",
            font=ctk.CTkFont(size=14, weight="bold")
        )
        self.user_name_label.grid(row=1, column=0, padx=15, pady=(0, 5))
        
        self.user_status_label = ctk.CTkLabel(
            self.user_info_frame,
            text="请先登录系统",
            font=ctk.CTkFont(size=11),
            text_color=("gray60", "gray40")
        )
        self.user_status_label.grid(row=2, column=0, padx=15, pady=(0, 15))
        
        # 退出按钮
        self.logout_btn = ctk.CTkButton(
            self.sidebar,
            text="🚪 退出登录",
            font=ctk.CTkFont(size=12),
            height=35,
            fg_color="#e74c3c",
            hover_color="#c0392b",
            command=self.logout
        )
        self.logout_btn.grid(row=4, column=0, sticky="ew", padx=20, pady=(10, 30))
        
        # 初始隐藏用户信息和退出按钮
        self.user_info_frame.grid_remove()
        self.logout_btn.grid_remove()
        
    def create_main_content(self):
        """创建主内容区域"""
        self.content_frame = ctk.CTkFrame(self.main_container, corner_radius=0)
        self.content_frame.grid(row=0, column=1, rowspan=2, sticky="nsew", padx=(10, 0))
        self.content_frame.grid_columnconfigure(0, weight=1)
        self.content_frame.grid_rowconfigure(0, weight=1)
        
        # 创建不同的视图容器
        self.views = {}
        
    def create_status_bar(self):
        """创建现代化状态栏"""
        self.status_bar = ctk.CTkFrame(self.main_container, height=40, corner_radius=0)
        self.status_bar.grid(row=2, column=1, sticky="ew", padx=(10, 0))
        self.status_bar.grid_propagate(False)
        self.status_bar.grid_columnconfigure(1, weight=1)
        
        # 状态指示器
        self.status_indicator = ctk.CTkLabel(
            self.status_bar,
            text="🔌",
            font=ctk.CTkFont(size=16)
        )
        self.status_indicator.grid(row=0, column=0, padx=(15, 5), pady=10)
        
        self.status_text = ctk.CTkLabel(
            self.status_bar,
            text="系统就绪",
            font=ctk.CTkFont(size=12)
        )
        self.status_text.grid(row=0, column=1, sticky="w", pady=10)
        
        # 服务器状态
        self.server_status = ctk.CTkLabel(
            self.status_bar,
            text="服务器: 未连接",
            font=ctk.CTkFont(size=11),
            text_color=("gray60", "gray40")
        )
        self.server_status.grid(row=0, column=2, padx=15, pady=10)
        
        # 时间显示
        self.time_label = ctk.CTkLabel(
            self.status_bar,
            text="",
            font=ctk.CTkFont(size=11),
            text_color=("gray60", "gray40")
        )
        self.time_label.grid(row=0, column=3, padx=(15, 20), pady=10)
        
        # 启动时间更新
        self.update_time()
        
    def show_login_view(self):
        """显示登录界面"""
        # 清除现有内容
        for widget in self.content_frame.winfo_children():
            widget.destroy()
            
        # 创建登录容器
        login_container = ctk.CTkFrame(self.content_frame)
        login_container.grid(row=0, column=0, sticky="nsew", padx=40, pady=40)
        login_container.grid_columnconfigure(0, weight=1)
        login_container.grid_rowconfigure(0, weight=1)
        
        # 登录卡片
        login_card = ctk.CTkFrame(login_container, width=500, height=600)
        login_card.grid(row=0, column=0, padx=20, pady=20)
        login_card.grid_propagate(False)
        
        # 登录标题
        title_label = ctk.CTkLabel(
            login_card,
            text="系统登录",
            font=ctk.CTkFont(size=28, weight="bold")
        )
        title_label.grid(row=0, column=0, pady=(40, 30))
        
        # 服务器配置区域
        server_frame = ctk.CTkFrame(login_card)
        server_frame.grid(row=1, column=0, sticky="ew", padx=40, pady=10)
        server_frame.grid_columnconfigure(1, weight=1)
        
        ctk.CTkLabel(
            server_frame,
            text="服务器配置",
            font=ctk.CTkFont(size=16, weight="bold")
        ).grid(row=0, column=0, columnspan=3, pady=(15, 10))
        
        # 服务器地址
        ctk.CTkLabel(server_frame, text="地址:").grid(row=1, column=0, sticky="w", padx=(15, 5), pady=5)
        self.server_host_var = tk.StringVar(value="localhost")
        self.server_host_entry = ctk.CTkEntry(
            server_frame,
            textvariable=self.server_host_var,
            width=200
        )
        self.server_host_entry.grid(row=1, column=1, sticky="ew", padx=5, pady=5)
        
        # 端口
        ctk.CTkLabel(server_frame, text="端口:").grid(row=1, column=2, sticky="w", padx=(15, 5), pady=5)
        self.server_port_var = tk.StringVar(value="8000")
        self.server_port_entry = ctk.CTkEntry(
            server_frame,
            textvariable=self.server_port_var,
            width=80
        )
        self.server_port_entry.grid(row=1, column=3, padx=(5, 15), pady=5)
        
        # 测试连接按钮
        test_btn = ctk.CTkButton(
            server_frame,
            text="测试连接",
            command=self.test_server_connection,
            height=32
        )
        test_btn.grid(row=2, column=0, columnspan=4, pady=(10, 15), padx=15, sticky="ew")
        
        # 用户信息区域
        user_frame = ctk.CTkFrame(login_card)
        user_frame.grid(row=2, column=0, sticky="ew", padx=40, pady=10)
        user_frame.grid_columnconfigure(1, weight=1)
        
        ctk.CTkLabel(
            user_frame,
            text="用户信息",
            font=ctk.CTkFont(size=16, weight="bold")
        ).grid(row=0, column=0, columnspan=2, pady=(15, 10))
        
        # 用户名
        ctk.CTkLabel(user_frame, text="用户名:").grid(row=1, column=0, sticky="w", padx=(15, 10), pady=8)
        self.username_var = tk.StringVar(value="grid_001")
        self.username_entry = ctk.CTkEntry(
            user_frame,
            textvariable=self.username_var,
            width=250
        )
        self.username_entry.grid(row=1, column=1, sticky="ew", padx=(0, 15), pady=8)
        
        # 密码
        ctk.CTkLabel(user_frame, text="密码:").grid(row=2, column=0, sticky="w", padx=(15, 10), pady=8)
        self.password_var = tk.StringVar(value="grid123")
        self.password_entry = ctk.CTkEntry(
            user_frame,
            textvariable=self.password_var,
            show="*",
            width=250
        )
        self.password_entry.grid(row=2, column=1, sticky="ew", padx=(0, 15), pady=8)
        
        # 网格区域
        ctk.CTkLabel(user_frame, text="网格区域:").grid(row=3, column=0, sticky="w", padx=(15, 10), pady=8)
        self.grid_area_var = tk.StringVar(value="测试网格区域")
        self.grid_area_entry = ctk.CTkEntry(
            user_frame,
            textvariable=self.grid_area_var,
            width=250
        )
        self.grid_area_entry.grid(row=3, column=1, sticky="ew", padx=(0, 15), pady=(8, 15))
        
        # 登录按钮
        self.login_btn = ctk.CTkButton(
            login_card,
            text="登录系统",
            command=self.login,
            font=ctk.CTkFont(size=16, weight="bold"),
            height=45,
            width=200
        )
        self.login_btn.grid(row=3, column=0, pady=(30, 40))
        
    def show_main_view(self):
        """显示主界面"""
        if not self.is_logged_in:
            self.show_login_view()
            return
            
        # 清除现有内容
        for widget in self.content_frame.winfo_children():
            widget.destroy()
            
        # 创建主界面容器
        main_container = ctk.CTkFrame(self.content_frame)
        main_container.grid(row=0, column=0, sticky="nsew", padx=20, pady=20)
        main_container.grid_columnconfigure(1, weight=3)  # 进一步增加右侧权重
        main_container.grid_rowconfigure(1, weight=1)
        
        # 欢迎标题
        welcome_label = ctk.CTkLabel(
            main_container,
            text=f"欢迎回来，{self.current_user}！",
            font=ctk.CTkFont(size=24, weight="bold")
        )
        welcome_label.grid(row=0, column=0, columnspan=2, pady=(20, 30), sticky="w", padx=20)
        
        # 快速操作卡片
        self.create_quick_actions(main_container)
        
        # 实时监控面板
        self.create_monitor_panel(main_container)
        
    def create_quick_actions(self, parent):
        """创建快速操作卡片"""
        actions_frame = ctk.CTkFrame(parent)
        actions_frame.grid(row=1, column=0, sticky="nsew", padx=(20, 10), pady=10)
        actions_frame.grid_columnconfigure(0, weight=1)
        
        ctk.CTkLabel(
            actions_frame,
            text="快速操作",
            font=ctk.CTkFont(size=18, weight="bold")
        ).grid(row=0, column=0, pady=(20, 15), padx=20, sticky="w")
        
        # 监控控制按钮
        self.start_monitor_btn = ctk.CTkButton(
            actions_frame,
            text="🚀 开始监控",
            command=self.start_monitoring,
            font=ctk.CTkFont(size=14, weight="bold"),
            height=50,
            fg_color="#2fa572",
            hover_color="#27ae60"
        )
        self.start_monitor_btn.grid(row=1, column=0, sticky="ew", padx=20, pady=5)
        
        self.stop_monitor_btn = ctk.CTkButton(
            actions_frame,
            text="⏹️ 停止监控",
            command=self.stop_monitoring,
            font=ctk.CTkFont(size=14, weight="bold"),
            height=50,
            fg_color="#e74c3c",
            hover_color="#c0392b",
            state="disabled"
        )
        self.stop_monitor_btn.grid(row=2, column=0, sticky="ew", padx=20, pady=5)
        
        # 微信配置按钮
        wechat_config_btn = ctk.CTkButton(
            actions_frame,
            text="📱 微信转发配置",
            command=self.open_simple_wechat_config,
            font=ctk.CTkFont(size=14),
            height=45,
            fg_color="#3498db",
            hover_color="#2980b9"
        )
        wechat_config_btn.grid(row=3, column=0, sticky="ew", padx=20, pady=5)
        
        # 刷新统计按钮
        refresh_btn = ctk.CTkButton(
            actions_frame,
            text="🔄 刷新统计",
            command=self.refresh_statistics,
            font=ctk.CTkFont(size=14),
            height=45
        )
        refresh_btn.grid(row=4, column=0, sticky="ew", padx=20, pady=(5, 20))
        
    def create_monitor_panel(self, parent):
        """创建监控面板"""
        monitor_frame = ctk.CTkFrame(parent)
        monitor_frame.grid(row=1, column=1, sticky="nsew", padx=(5, 20), pady=10)  # 增加右边距
        monitor_frame.grid_columnconfigure(0, weight=1)
        monitor_frame.grid_rowconfigure(2, weight=1)
        
        ctk.CTkLabel(
            monitor_frame,
            text="实时监控",
            font=ctk.CTkFont(size=18, weight="bold")
        ).grid(row=0, column=0, pady=(20, 15), padx=20, sticky="w")
        
        # 统计卡片
        stats_container = ctk.CTkFrame(monitor_frame)
        stats_container.grid(row=1, column=0, sticky="ew", padx=20, pady=10)
        stats_container.grid_columnconfigure((0, 1, 2), weight=1)
        
        # 总消息数卡片
        total_card = ctk.CTkFrame(stats_container)
        total_card.grid(row=0, column=0, sticky="ew", padx=(0, 5), pady=10)
        
        ctk.CTkLabel(
            total_card,
            text="📊",
            font=ctk.CTkFont(size=24)
        ).grid(row=0, column=0, pady=(15, 5))
        
        self.total_msg_label = ctk.CTkLabel(
            total_card,
            text="0",
            font=ctk.CTkFont(size=20, weight="bold")
        )
        self.total_msg_label.grid(row=1, column=0, pady=2)
        
        ctk.CTkLabel(
            total_card,
            text="总消息数",
            font=ctk.CTkFont(size=12),
            text_color=("gray60", "gray40")
        ).grid(row=2, column=0, pady=(2, 15))
        
        # 今日消息卡片
        today_card = ctk.CTkFrame(stats_container)
        today_card.grid(row=0, column=1, sticky="ew", padx=5, pady=10)
        
        ctk.CTkLabel(
            today_card,
            text="📅",
            font=ctk.CTkFont(size=24)
        ).grid(row=0, column=0, pady=(15, 5))
        
        self.today_msg_label = ctk.CTkLabel(
            today_card,
            text="0",
            font=ctk.CTkFont(size=20, weight="bold")
        )
        self.today_msg_label.grid(row=1, column=0, pady=2)
        
        ctk.CTkLabel(
            today_card,
            text="今日消息",
            font=ctk.CTkFont(size=12),
            text_color=("gray60", "gray40")
        ).grid(row=2, column=0, pady=(2, 15))
        
        # 转发次数卡片
        forward_card = ctk.CTkFrame(stats_container)
        forward_card.grid(row=0, column=2, sticky="ew", padx=(5, 0), pady=10)
        
        ctk.CTkLabel(
            forward_card,
            text="📤",
            font=ctk.CTkFont(size=24)
        ).grid(row=0, column=0, pady=(15, 5))
        
        self.forwarded_label = ctk.CTkLabel(
            forward_card,
            text="0",
            font=ctk.CTkFont(size=20, weight="bold")
        )
        self.forwarded_label.grid(row=1, column=0, pady=2)
        
        ctk.CTkLabel(
            forward_card,
            text="转发次数",
            font=ctk.CTkFont(size=12),
            text_color=("gray60", "gray40")
        ).grid(row=2, column=0, pady=(2, 15))
        
        # 监控状态
        status_frame = ctk.CTkFrame(monitor_frame)
        status_frame.grid(row=2, column=0, sticky="nsew", padx=20, pady=(10, 20))
        status_frame.grid_columnconfigure(0, weight=1)
        status_frame.grid_rowconfigure(2, weight=1)
        
        ctk.CTkLabel(
            status_frame,
            text="监控状态",
            font=ctk.CTkFont(size=16, weight="bold")
        ).grid(row=0, column=0, pady=(15, 10), padx=15, sticky="w")
        
        self.monitor_status_label = ctk.CTkLabel(
            status_frame,
            text="🔴 监控未启动",
            font=ctk.CTkFont(size=14, weight="bold"),
            text_color="#e74c3c"
        )
        self.monitor_status_label.grid(row=1, column=0, pady=5, padx=15, sticky="w")
        
        # 消息日志区域
        log_frame = ctk.CTkFrame(status_frame)
        log_frame.grid(row=2, column=0, sticky="nsew", padx=15, pady=15)
        log_frame.grid_columnconfigure(0, weight=1)
        log_frame.grid_rowconfigure(1, weight=1)
        
        ctk.CTkLabel(
            log_frame,
            text="📝 消息日志",
            font=ctk.CTkFont(size=14, weight="bold")
        ).grid(row=0, column=0, pady=(10, 5), padx=10, sticky="w")
        
        # 使用CustomTkinter的CTkTextbox替代传统ScrolledText
        self.log_text = ctk.CTkTextbox(
            log_frame,
            height=200,  # 恢复到合适的高度
            font=ctk.CTkFont(family="Consolas", size=10),
            fg_color="#2b2b2b",
            text_color="#ffffff",
            scrollbar_button_color="#404040",
            scrollbar_button_hover_color="#505050",
            corner_radius=5,
            border_width=0
        )
        self.log_text.grid(row=1, column=0, sticky="nsew", padx=(10, 10), pady=5)

        
        # 清空日志按钮
        clear_btn = ctk.CTkButton(
            log_frame,
            text="🗑️ 清空日志",
            command=self.clear_log,
            height=30,
            fg_color="#e74c3c",
            hover_color="#c0392b"
        )
        clear_btn.grid(row=2, column=0, pady=(5, 10), padx=10, sticky="w")
        
    def show_monitor_view(self):
        """显示监控控制界面"""
        messagebox.showinfo("提示", "监控控制界面开发中...")
        
    def show_stats_view(self):
        """显示统计分析界面"""
        messagebox.showinfo("提示", "统计分析界面开发中...")
        
    def show_settings_view(self):
        """显示系统设置界面"""
        messagebox.showinfo("提示", "系统设置界面开发中...")
        
    @property
    def server_url(self):
        """动态获取服务器URL"""
        host = self.server_host_var.get() if hasattr(self, 'server_host_var') else self.server_host
        port = self.server_port_var.get() if hasattr(self, 'server_port_var') else self.server_port
        return f"http://{host}:{port}"
        
    def check_server_connection(self):
        """检查服务器连接"""
        try:
            response = self.session.get(f"{self.server_url}/api/system/health", timeout=5)
            if response.status_code == 200:
                self.server_status.configure(text="服务器: 已连接", text_color="#2fa572")
                return True
        except Exception as e:
            self.server_status.configure(text="服务器: 连接失败", text_color="#e74c3c")
            if hasattr(self, 'log_message'):
                self.log_message(f"服务器连接失败: {str(e)}")
        return False
        
    def test_server_connection(self):
        """测试服务器连接"""
        server_host = self.server_host_var.get().strip()
        server_port = self.server_port_var.get().strip()
        
        if not server_host or not server_port:
            messagebox.showerror("错误", "请先输入服务器地址和端口")
            return
            
        try:
            test_url = f"http://{server_host}:{server_port}/system/health"
            response = self.session.get(test_url, timeout=5)
            if response.status_code == 200:
                health_data = response.json()
                status = health_data.get('status', 'unknown')
                if status == 'healthy':
                    messagebox.showinfo("连接测试", "✅ 服务器连接成功！系统状态正常")
                    self.server_status.configure(text="服务器: 已连接", text_color="#2fa572")
                else:
                    messagebox.showwarning("连接测试", f"⚠️ 服务器连接成功，但系统状态异常: {status}")
                    self.server_status.configure(text="服务器: 状态异常", text_color="#f39c12")
            else:
                messagebox.showerror("连接测试", f"❌ 服务器响应异常，状态码: {response.status_code}")
                self.server_status.configure(text="服务器: 连接异常", text_color="#e74c3c")
        except requests.exceptions.Timeout:
            messagebox.showerror("连接测试", "❌ 连接超时，请检查服务器地址和端口")
            self.server_status.configure(text="服务器: 连接超时", text_color="#e74c3c")
        except requests.exceptions.ConnectionError:
            messagebox.showerror("连接测试", "❌ 无法连接到服务器，请检查服务器是否启动")
            self.server_status.configure(text="服务器: 连接失败", text_color="#e74c3c")
        except Exception as e:
            messagebox.showerror("连接测试", f"❌ 连接测试失败: {str(e)}")
            self.server_status.configure(text="服务器: 连接失败", text_color="#e74c3c")
    
    def login(self):
        """用户登录"""
        username = self.username_var.get().strip()
        password = self.password_var.get().strip()
        grid_area = self.grid_area_var.get().strip()
        
        if not username or not password:
            messagebox.showerror("错误", "请输入用户名和密码")
            return
            
        try:
            login_data = {
                "username": username,
                "password": password,
                "grid_area": grid_area
            }
            
            response = self.session.post(f"{self.server_url}/api/grid/login", 
                                       json=login_data, timeout=10)
            
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    self.is_logged_in = True
                    self.current_user = username
                    
                    # 更新用户信息显示
                    self.user_name_label.configure(text=username)
                    self.user_status_label.configure(text=f"网格区域: {grid_area}")
                    self.user_info_frame.grid()
                    self.logout_btn.grid()
                    
                    # 更新状态
                    self.status_text.configure(text="登录成功")
                    
                    messagebox.showinfo("登录成功", f"欢迎，{username}！")
                    self.show_main_view()
                else:
                    messagebox.showerror("登录失败", result.get('message', '登录失败'))
            else:
                messagebox.showerror("登录失败", f"服务器响应错误: {response.status_code}")
                
        except Exception as e:
            messagebox.showerror("登录失败", f"登录过程中发生错误: {str(e)}")
    
    def logout(self):
        """用户退出登录"""
        if messagebox.askyesno("确认退出", "确定要退出登录吗？"):
            self.is_logged_in = False
            self.current_user = None
            
            # 停止监控
            if self.monitoring_active:
                self.stop_monitoring()
            
            # 隐藏用户信息
            self.user_info_frame.grid_remove()
            self.logout_btn.grid_remove()
            
            # 重置用户信息显示
            self.user_name_label.configure(text="未登录")
            self.user_status_label.configure(text="请先登录系统")
            
            # 更新状态
            self.status_text.configure(text="已退出登录")
            
            # 显示登录界面
            self.show_login_view()
    
    def start_monitoring(self):
        """开始监控"""
        if not self.is_logged_in:
            messagebox.showerror("错误", "请先登录系统")
            return
            
        if self.monitoring_active:
            messagebox.showwarning("警告", "监控已经在运行中")
            return
            
        try:
            self.monitoring_active = True
            self.start_monitor_btn.configure(state="disabled")
            self.stop_monitor_btn.configure(state="normal")
            
            # 更新监控状态显示
            self.monitor_status_label.configure(
                text="🟢 监控运行中",
                text_color="#2fa572"
            )
            
            # 初始化微信监控（传递server_url）
            if WeChatEnhanced and not self.wechat_monitor:
                try:
                    from wechat_enhanced import get_wechat_instance
                    self.wechat_monitor = get_wechat_instance(self.server_url)
                    self.log_message("微信监控模块已初始化（支持断网机制）")
                except Exception as e:
                    self.log_message(f"微信监控模块初始化失败: {str(e)}")
            
            # 同步配置管理器的设置
            if self.config_manager:
                try:
                    # 同步群组配置
                    self.config_manager.sync_from_server()
                    self.log_message("配置同步完成")
                except Exception as e:
                    self.log_message(f"配置同步失败，使用本地缓存: {str(e)}")
            
            # 启动监控线程
            self.monitor_thread = threading.Thread(target=self.monitor_loop, daemon=True)
            self.monitor_thread.start()
            
            self.log_message("监控已启动，正在监控中...")
            self.status_text.configure(text="监控运行中")
            
        except Exception as e:
            self.monitoring_active = False
            self.start_monitor_btn.configure(state="normal")
            self.stop_monitor_btn.configure(state="disabled")
            messagebox.showerror("启动失败", f"启动监控失败: {str(e)}")
    
    def stop_monitoring(self):
        """停止监控"""
        if not self.monitoring_active:
            return
            
        self.monitoring_active = False
        self.start_monitor_btn.configure(state="normal")
        self.stop_monitor_btn.configure(state="disabled")
        
        # 更新监控状态显示
        self.monitor_status_label.configure(
            text="🔴 监控未启动",
            text_color="#e74c3c"
        )
        
        self.log_message("监控已停止")
        self.status_text.configure(text="监控已停止")
    
    def monitor_loop(self):
        """监控循环"""
        monitor_count = 0
        while self.monitoring_active:
            try:
                # 模拟监控逻辑
                time.sleep(2)
                if self.monitoring_active:
                    monitor_count += 1
                    # 每15次循环更新一次监控状态（30秒）
                    if monitor_count % 15 == 1:
                        self.log_message("正在监控中，等待关键词触发...")
                    self.simulate_message_received()
            except Exception as e:
                self.log_message(f"监控错误: {str(e)}")
                time.sleep(1)
    
    def simulate_message_received(self):
        """模拟接收到消息"""
        import random
        
        messages = [
            "检测到异常情况，请及时处理",
            "设备运行正常",
            "发现新的问题需要关注",
            "系统状态良好",
            "有新的工单需要处理"
        ]
        
        message = random.choice(messages)
        
        # 检查是否包含关键词
        should_forward, triggered_keyword = self.check_keyword_trigger(message)
        
        # 只有触发关键词时才记录具体消息
        if should_forward and triggered_keyword:
            self.log_message(f"⚠️ 关键词触发: {message} (关键词: {triggered_keyword})")
            
            # 更新统计数据
            self.message_stats["total_messages"] += 1
            self.message_stats["today_messages"] += 1
            self.update_stats_display()
            
            # 如果配置了微信转发，执行转发
            if self.forward_wechat_name:
                self.sync_forward_message_to_wechat(message, "系统监控", triggered_keyword)
        # 否则不记录任何消息，保持静默监控
    
    def send_message_to_backend(self, message):
        """发送消息到后端"""
        try:
            message_data = {
                "content": message,
                "sender": self.current_user,
                "grid_area": self.grid_area_var.get(),
                "timestamp": datetime.now().isoformat()
            }
            
            # 检查网络状态
            if self.network_manager and not self.network_manager.is_online:
                # 网络不可用，保存到本地待上传
                self.network_manager.add_pending_upload(
                    'message', message_data, priority='high'
                )
                self.log_message(f"网络不可用，消息已保存到本地: {message}")
                return
            
            response = self.session.post(f"{self.server_url}/api/grid/messages", 
                                       json=message_data, timeout=10)
            
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    self.message_stats["total_messages"] += 1
                    self.message_stats["today_messages"] += 1
                    self.update_stats_display()
                    self.log_message(f"消息已发送: {message}")
                    
                    # 检查关键词触发转发
                    should_forward = False
                    triggered_keyword = None
                    forward_target = None
                    
                    # 检查是否包含触发关键词
                    should_forward, triggered_keyword = self.check_keyword_trigger(message)
                    
                    if should_forward:
                        # 优先使用本地配置的转发目标
                        if self.forward_wechat_name:
                            forward_target = self.forward_wechat_name
                            self.log_message(f"🎯 使用本地配置转发目标: {forward_target}")
                        else:
                            # 如果没有本地配置，检查后端返回的网格员信息
                            forward_info = result.get('data', {}).get('forward_info')
                            if forward_info and forward_info.get('target_wechat_name'):
                                forward_target = forward_info.get('target_wechat_name')
                                grid_officer_name = forward_info.get('grid_officer_name', '未知网格员')
                                self.log_message(f"🎯 使用网格员转发目标: {forward_target} (网格员: {grid_officer_name})")
                            else:
                                self.log_message(f"⚠️ 关键词[{triggered_keyword}]触发转发，但未配置转发目标")
                    
                    # 如果需要转发到微信且有转发目标
                    if should_forward and forward_target:
                        # 使用异步转发（在线程中运行）
                        import asyncio
                        try:
                            # 在新的事件循环中运行异步转发
                            loop = asyncio.new_event_loop()
                            asyncio.set_event_loop(loop)
                            loop.run_until_complete(self.forward_message_to_wechat_target(message, self.current_user, triggered_keyword, forward_target))
                            loop.close()
                        except Exception as e:
                            self.log_message(f"❌ 异步转发失败: {str(e)}")
                            # 回退到同步转发
                            self.sync_forward_message_to_wechat_target(message, self.current_user, triggered_keyword, forward_target)
                else:
                    self.log_message(f"消息发送失败: {result.get('message')}")
            else:
                self.log_message(f"消息发送失败，状态码: {response.status_code}")
                
        except Exception as e:
            self.log_message(f"发送消息时发生错误: {str(e)}")
            # 网络异常时也保存到本地
            if self.network_manager:
                message_data = {
                    "content": message,
                    "sender": self.current_user,
                    "grid_area": self.grid_area_var.get(),
                    "timestamp": datetime.now().isoformat()
                }
                self.network_manager.add_pending_upload(
                    'message', message_data, priority='high'
                )
                self.log_message(f"网络异常，消息已保存到本地: {message}")
    
    async def forward_message_to_wechat(self, message, sender="系统", triggered_keyword=None):
        """转发消息到微信（实际发送版本）"""
        try:
            # 如果没有配置转发目标，直接返回
            if not self.forward_wechat_name:
                self.log_message("⚠️ 未配置转发目标")
                return
            
            # 格式化转发消息：消息来源 + 消息内容
            # 修复区域信息获取
            area_name = "未知区域"
            if hasattr(self, 'grid_area_var') and self.grid_area_var:
                area_name = self.grid_area_var.get()
            elif hasattr(self, 'current_user') and self.current_user:
                area_name = "网格区域"
            
            source_info = f"{area_name} - {sender}"
            formatted_message = f"📢 消息来源: {source_info}\n📝 消息内容: {message}"
            
            # 添加关键词信息到转发消息
            if triggered_keyword:
                formatted_message += f"\n🔑 触发关键词: {triggered_keyword}"
                self.log_message(f"🔑 关键词[{triggered_keyword}]触发转发到微信[{self.forward_wechat_name}]")
            else:
                self.log_message(f"📤 转发到微信[{self.forward_wechat_name}]")
            
            # 实际发送微信消息
            success = await self._send_wechat_message(formatted_message, self.forward_wechat_name)
            
            if success:
                self.log_message(f"✅ 微信转发成功: {message[:30]}...")
                self.message_stats["forwarded_count"] += 1
                self.update_stats_display()
            else:
                self.log_message(f"❌ 微信转发失败")
                    
        except Exception as e:
            self.log_message(f"❌ 微信转发失败: {str(e)}")
    
    async def forward_message_to_wechat_target(self, message, sender="系统", triggered_keyword=None, target=None):
        """转发消息到指定微信目标"""
        try:
            # 如果没有指定转发目标，直接返回
            if not target:
                self.log_message("⚠️ 未指定转发目标")
                return
            
            # 格式化转发消息：消息来源 + 消息内容
            area_name = "未知区域"
            if hasattr(self, 'grid_area_var') and self.grid_area_var:
                area_name = self.grid_area_var.get()
            elif hasattr(self, 'current_user') and self.current_user:
                area_name = "网格区域"
            
            source_info = f"{area_name} - {sender}"
            formatted_message = f"📢 消息来源: {source_info}\n📝 消息内容: {message}"
            
            # 添加关键词信息到转发消息
            if triggered_keyword:
                formatted_message += f"\n🔑 触发关键词: {triggered_keyword}"
                self.log_message(f"🔑 关键词[{triggered_keyword}]触发转发到微信[{target}]")
            else:
                self.log_message(f"📤 转发到微信[{target}]")
            
            # 实际发送微信消息
            success = await self._send_wechat_message(formatted_message, target)
            
            if success:
                self.log_message(f"✅ 微信转发成功: {message[:30]}...")
                self.message_stats["forwarded_count"] += 1
                self.update_stats_display()
            else:
                self.log_message(f"❌ 微信转发失败")
                    
        except Exception as e:
            self.log_message(f"❌ 微信转发失败: {str(e)}")
    
    async def _send_wechat_message(self, message: str, to: str) -> bool:
        """发送微信消息的实际实现"""
        try:
            # 方法1: 调用后端API发送微信消息
            response = self.session.post(
                f"{self.server_url}/api/wx/send",
                params={"message": message, "to": to},
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                self.log_message(f"📤 API发送成功: {result.get('message', '')}")
                return True
            else:
                self.log_message(f"❌ API发送失败，状态码: {response.status_code}")
                # 方法2: 回退到本地wxauto发送
                return await self._send_wechat_local(message, to)
                
        except Exception as e:
            self.log_message(f"❌ API发送异常: {str(e)}")
            # 方法2: 回退到本地wxauto发送
            return await self._send_wechat_local(message, to)
    
    async def _send_wechat_local(self, message: str, to: str) -> bool:
        """本地wxauto发送微信消息"""
        try:
            # 尝试导入wxauto
            try:
                from wxauto import WeChat
            except ImportError:
                self.log_message("❌ wxauto模块未安装，无法发送微信消息")
                return False
            
            # 初始化微信客户端
            wx = WeChat()
            
            # 发送消息
            wx.SendMsg(message, who=to)
            self.log_message(f"📱 本地发送成功: 消息已发送到 {to}")
            return True
            
        except Exception as e:
            self.log_message(f"❌ 本地发送失败: {str(e)}")
            return False
    
    def sync_forward_message_to_wechat(self, message, sender="系统", triggered_keyword=None):
        """同步转发消息到微信"""
        try:
            # 如果没有配置转发目标，直接返回
            if not self.forward_wechat_name:
                self.log_message("⚠️ 未配置转发目标")
                return False, "未配置转发目标"
            
            # 格式化转发消息
            area_name = "未知区域"
            if hasattr(self, 'grid_area_var') and self.grid_area_var:
                area_name = self.grid_area_var.get()
            elif hasattr(self, 'current_user') and self.current_user:
                area_name = "网格区域"
            
            source_info = f"{area_name} - {sender}"
            formatted_message = f"📢 消息来源: {source_info}\n📝 消息内容: {message}"
            
            if triggered_keyword:
                formatted_message += f"\n🔑 触发关键词: {triggered_keyword}"
                self.log_message(f"🔑 关键词[{triggered_keyword}]触发同步转发到微信[{self.forward_wechat_name}]")
            else:
                self.log_message(f"📤 同步转发到微信[{self.forward_wechat_name}]")
            
            # 同步发送微信消息
            success, error_msg = self._send_wechat_message_sync(formatted_message, self.forward_wechat_name)
            
            if success:
                self.log_message(f"✅ 同步微信转发成功: {message[:30]}...")
                self.message_stats["forwarded_count"] += 1
                self.update_stats_display()
                return True, "发送成功"
            else:
                self.log_message(f"❌ 同步微信转发失败: {error_msg}")
                return False, error_msg
                
        except Exception as e:
            error_msg = f"转发过程中出现异常: {str(e)}"
            self.log_message(f"❌ 同步微信转发失败: {error_msg}")
            return False, error_msg
    
    def sync_forward_message_to_wechat_target(self, message, sender="系统", triggered_keyword=None, target=None):
        """同步转发消息到指定微信目标"""
        try:
            # 如果没有指定转发目标，直接返回
            if not target:
                self.log_message("⚠️ 未指定转发目标")
                return False, "未指定转发目标"
            
            # 格式化转发消息
            area_name = "未知区域"
            if hasattr(self, 'grid_area_var') and self.grid_area_var:
                area_name = self.grid_area_var.get()
            elif hasattr(self, 'current_user') and self.current_user:
                area_name = "网格区域"
            
            source_info = f"{area_name} - {sender}"
            formatted_message = f"📢 消息来源: {source_info}\n📝 消息内容: {message}"
            
            if triggered_keyword:
                formatted_message += f"\n🔑 触发关键词: {triggered_keyword}"
                self.log_message(f"🔑 关键词[{triggered_keyword}]触发同步转发到微信[{target}]")
            else:
                self.log_message(f"📤 同步转发到微信[{target}]")
            
            # 同步发送微信消息
            success, error_msg = self._send_wechat_message_sync(formatted_message, target)
            
            if success:
                self.log_message(f"✅ 同步微信转发成功: {message[:30]}...")
                self.message_stats["forwarded_count"] += 1
                self.update_stats_display()
                return True, "发送成功"
            else:
                self.log_message(f"❌ 同步微信转发失败: {error_msg}")
                return False, error_msg
                
        except Exception as e:
            error_msg = f"转发过程中出现异常: {str(e)}"
            self.log_message(f"❌ 同步微信转发失败: {error_msg}")
            return False, error_msg
    
    def _send_wechat_message_sync(self, message: str, to: str) -> tuple:
        """同步发送微信消息"""
        try:
            # 方法1: 调用后端API发送微信消息
            response = self.session.post(
                f"{self.server_url}/api/wx/send",
                params={"message": message, "to": to},
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                self.log_message(f"📤 API同步发送成功: {result.get('message', '')}")
                return True, "API发送成功"
            else:
                # 解析API错误信息
                try:
                    error_data = response.json()
                    api_error = error_data.get('detail', f'HTTP {response.status_code}')
                except:
                    api_error = f'HTTP {response.status_code}'
                
                self.log_message(f"❌ API同步发送失败: {api_error}")
                # 方法2: 回退到本地wxauto发送
                return self._send_wechat_local_sync(message, to)
                
        except Exception as e:
            self.log_message(f"❌ API同步发送异常: {str(e)}")
            # 方法2: 回退到本地wxauto发送
            return self._send_wechat_local_sync(message, to)
    
    def _send_wechat_local_sync(self, message: str, to: str) -> tuple:
        """本地同步发送微信消息"""
        try:
            # 尝试导入wxauto
            try:
                from wxauto import WeChat
            except ImportError:
                error_msg = "wxauto模块未安装，无法发送微信消息"
                self.log_message(f"❌ {error_msg}")
                return False, error_msg
            
            # 初始化微信客户端
            try:
                wx = WeChat()
            except Exception as e:
                error_msg = f"微信客户端初始化失败: {str(e)}"
                self.log_message(f"❌ {error_msg}")
                return False, error_msg
            
            # 发送消息
            try:
                wx.SendMsg(message, who=to)
                self.log_message(f"📱 本地同步发送成功: 消息已发送到 {to}")
                return True, "本地发送成功"
            except Exception as e:
                # 检查是否是联系人未找到的错误
                error_str = str(e).lower()
                if "not found" in error_str or "未找到" in error_str or "找不到" in error_str:
                    error_msg = f"联系人'{to}'未找到，请检查联系人名称是否正确"
                else:
                    error_msg = f"发送消息失败: {str(e)}"
                
                self.log_message(f"❌ 本地发送失败: {error_msg}")
                return False, error_msg
            
        except Exception as e:
            error_msg = f"本地发送过程中出现异常: {str(e)}"
            self.log_message(f"❌ 本地同步发送失败: {error_msg}")
            return False, error_msg
    
    def refresh_statistics(self):
        """刷新统计信息"""
        try:
            # 获取网格ID（这里简化处理）
            grid_id = 1  # 实际应该从用户信息中获取
            
            response = self.session.get(
                f"{self.server_url}/api/grid/message_statistics",
                params={"grid_id": grid_id, "days": 7},
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    stats = data.get('data', {})
                    self.message_stats["total_messages"] = stats.get('total_messages', 0)
                    
                    # 计算今日消息数
                    daily_stats = stats.get('daily_stats', [])
                    today = datetime.now().strftime('%Y-%m-%d')
                    today_count = 0
                    for day_stat in daily_stats:
                        if day_stat.get('date') == today:
                            today_count = day_stat.get('message_count', 0)
                            break
                    
                    self.message_stats["today_messages"] = today_count
                    self.update_stats_display()
                    self.log_message("统计信息已刷新")
                else:
                    self.log_message(f"获取统计信息失败: {data.get('message')}")
            else:
                self.log_message(f"获取统计信息失败，状态码: {response.status_code}")
                
        except Exception as e:
            self.log_message(f"刷新统计信息时发生错误: {str(e)}")
    
    def update_stats_display(self):
        """更新统计显示"""
        if hasattr(self, 'total_msg_label'):
            self.total_msg_label.configure(text=str(self.message_stats["total_messages"]))
        if hasattr(self, 'today_msg_label'):
            self.today_msg_label.configure(text=str(self.message_stats["today_messages"]))
        if hasattr(self, 'forwarded_label'):
            self.forwarded_label.configure(text=str(self.message_stats["forwarded_count"]))
    
    def log_message(self, message):
        """记录日志消息"""
        if hasattr(self, 'log_text'):
            timestamp = datetime.now().strftime("%H:%M:%S")
            log_entry = f"[{timestamp}] {message}\n"
            
            # CustomTkinter CTkTextbox API - 在末尾插入并自动滚动
            self.log_text.insert("end", log_entry)
            
            # 限制日志长度
            current_text = self.log_text.get("1.0", "end")
            lines = current_text.split("\n")
            if len(lines) > 1000:
                # 保留最后900行
                recent_lines = lines[-900:]
                self.log_text.delete("1.0", "end")
                self.log_text.insert("1.0", "\n".join(recent_lines))
            
            # 自动滚动到最后一行
            self.log_text.see("end")
    
    def clear_log(self):
        """清空日志"""
        if hasattr(self, 'log_text'):
            self.log_text.delete("1.0", "end")
            timestamp = datetime.now().strftime("%H:%M:%S")
            self.log_text.insert("end", f"[{timestamp}] 日志已清空\n")
            self.log_text.see("end")
    

    
    def load_simple_forward_config(self):
        """加载简化转发配置"""
        try:
            if os.path.exists("simple_forward_config.json"):
                with open("simple_forward_config.json", "r", encoding="utf-8") as f:
                    config_data = json.load(f)
                    self.forward_wechat_name = config_data.get("forward_wechat_name", "")
                    self.forward_keywords = config_data.get("forward_keywords", "紧急,故障,停电,事故,报修")
        except Exception as e:
            print(f"加载转发配置失败: {str(e)}")
    
    def fetch_server_keywords(self):
        """从服务器获取关键词配置（等级1）"""
        try:
            # 优先使用配置管理器获取关键词
            if self.config_manager:
                try:
                    config = self.config_manager.get_local_config()
                    if config and 'keywords' in config:
                        self.server_keywords = config['keywords']
                        print(f"从配置管理器获取关键词成功: {self.server_keywords}")
                        return
                except Exception as e:
                    print(f"从配置管理器获取关键词失败: {str(e)}")
            
            # 如果配置管理器不可用，使用传统方式
            response = self.session.get(
                f"{self.server_url}/api/grid/forward_keywords",
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    keywords_data = data.get('data', {})
                    self.server_keywords = keywords_data.get('keywords', '')
                    print(f"服务器关键词获取成功: {self.server_keywords}")
                else:
                    print(f"获取服务器关键词失败: {data.get('message')}")
            else:
                print(f"获取服务器关键词失败，状态码: {response.status_code}")
                
        except Exception as e:
            print(f"获取服务器关键词时发生错误: {str(e)}")
            # 服务器不可用时使用本地关键词
    
    def check_keyword_trigger(self, message):
        """检查消息是否包含触发关键词（支持优先级）"""
        message_lower = message.lower()
        
        # 优先检查服务器关键词（等级1）
        if self.server_keywords:
            server_keywords = [k.strip() for k in self.server_keywords.split(",") if k.strip()]
            for keyword in server_keywords:
                if keyword.lower() in message_lower:
                    return True, f"{keyword}(服务器)"
        
        # 如果服务器关键词未匹配，检查本地关键词（等级2）
        if self.forward_keywords:
            local_keywords = [k.strip() for k in self.forward_keywords.split(",") if k.strip()]
            for keyword in local_keywords:
                if keyword.lower() in message_lower:
                    return True, f"{keyword}(本地)"
        
        return False, None
    
    def open_simple_wechat_config(self):
        """打开简单微信转发配置窗口"""
        config_window = ctk.CTkToplevel(self.root)
        config_window.title("微信转发配置")
        config_window.geometry("500x550")
        config_window.resizable(False, False)
        
        # 设置窗口居中
        config_window.transient(self.root)
        config_window.grab_set()
        
        # 配置内容
        ctk.CTkLabel(
            config_window,
            text="微信转发配置",
            font=ctk.CTkFont(size=20, weight="bold")
        ).pack(pady=(30, 20))
        
        # 提示信息
        tip_frame = ctk.CTkFrame(config_window)
        tip_frame.pack(fill="x", padx=30, pady=(0, 20))
        
        ctk.CTkLabel(
            tip_frame,
            text="💡 配置关键词触发转发，检测到关键词时自动转发消息",
            font=ctk.CTkFont(size=12),
            text_color="orange"
        ).pack(pady=15)
        
        # 转发目标
        target_frame = ctk.CTkFrame(config_window)
        target_frame.pack(fill="x", padx=30, pady=10)
        
        ctk.CTkLabel(
            target_frame,
            text="转发目标微信名称:",
            font=ctk.CTkFont(size=14)
        ).pack(pady=(20, 10))
        
        wechat_name_var = tk.StringVar(value=self.forward_wechat_name)
        wechat_entry = ctk.CTkEntry(
            target_frame,
            textvariable=wechat_name_var,
            width=300,
            height=35
        )
        wechat_entry.pack(pady=(0, 20))
        
        # 关键词配置
        keyword_frame = ctk.CTkFrame(config_window)
        keyword_frame.pack(fill="x", padx=30, pady=10)
        
        ctk.CTkLabel(
            keyword_frame,
            text="本地关键词 (用逗号分隔):",
            font=ctk.CTkFont(size=14)
        ).pack(pady=(20, 10))
        
        # 获取当前关键词配置
        current_keywords = getattr(self, 'forward_keywords', "紧急,故障,停电,事故,报修")
        keywords_var = tk.StringVar(value=current_keywords)
        keywords_entry = ctk.CTkEntry(
            keyword_frame,
            textvariable=keywords_var,
            width=300,
            height=35,
            placeholder_text="例如: 紧急,故障,停电,事故"
        )
        keywords_entry.pack(pady=(0, 20))
        
        # 按钮
        button_frame = ctk.CTkFrame(config_window, fg_color="transparent")
        button_frame.pack(fill="x", padx=30, pady=20)
        
        def save_config():
            self.forward_wechat_name = wechat_name_var.get().strip()
            self.forward_keywords = keywords_var.get().strip()
            
            # 保存配置到文件
            config_data = {
                "forward_wechat_name": self.forward_wechat_name,
                "forward_keywords": self.forward_keywords
            }
            
            try:
                with open("simple_forward_config.json", "w", encoding="utf-8") as f:
                    json.dump(config_data, f, ensure_ascii=False, indent=2)
                messagebox.showinfo("保存成功", "微信转发配置已保存")
                self.log_message(f"✅ 转发配置已保存: 目标[{self.forward_wechat_name}], 关键词[{self.forward_keywords}]")
            except Exception as e:
                messagebox.showerror("保存失败", f"配置保存失败: {str(e)}")
            
            config_window.destroy()
        
        def test_wechat():
            if self.forward_wechat_name and self.forward_keywords:
                # 实际发送消息到文件传输助手
                keywords = [k.strip() for k in keywords_var.get().split(",") if k.strip()]
                if keywords:
                    test_keyword = keywords[0]
                    test_message = f"测试消息包含关键词: {test_keyword}"
                    source_info = "测试群聊 - 测试用户"
                    formatted_message = f"📢 消息来源: {source_info}\n📝 消息内容: {test_message}\n🔑 触发关键词: {test_keyword}"
                    
                    self.log_message(f"🧪 关键词[{test_keyword}]触发转发到[{wechat_name_var.get()}]")
                    self.log_message(f"📤 转发内容: {formatted_message}")
                    
                    # 实际发送到微信
                    try:
                        success, error_msg = self.sync_forward_message_to_wechat(
                            test_message, 
                            sender="测试用户", 
                            triggered_keyword=test_keyword
                        )
                        
                        if success:
                            messagebox.showinfo("发送成功", f"消息已成功发送到微信！\n\n目标联系人: {wechat_name_var.get()}\n触发关键词: {test_keyword}\n发送状态: {error_msg}")
                            self.log_message("✅ 微信消息发送成功")
                        else:
                            # 根据错误类型提供不同的提示
                            if "未找到" in error_msg or "not found" in error_msg.lower():
                                messagebox.showerror("联系人未找到", f"转发失败！\n\n错误原因: {error_msg}\n\n建议解决方案:\n1. 检查联系人名称是否正确\n2. 确保该联系人在微信通讯录中\n3. 尝试使用完整的联系人昵称\n4. 如果是群聊，请使用完整的群名称")
                            elif "wxauto" in error_msg:
                                messagebox.showerror("微信模块错误", f"转发失败！\n\n错误原因: {error_msg}\n\n建议解决方案:\n1. 确保已安装wxauto模块\n2. 确保微信客户端已打开\n3. 重启微信客户端后重试")
                            elif "API" in error_msg or "HTTP" in error_msg:
                                messagebox.showerror("服务器连接错误", f"转发失败！\n\n错误原因: {error_msg}\n\n建议解决方案:\n1. 检查网络连接\n2. 确保后端服务正常运行\n3. 检查微信机器人状态")
                            else:
                                messagebox.showerror("发送失败", f"转发失败！\n\n错误原因: {error_msg}\n\n请检查微信连接状态和联系人设置")
                            
                            self.log_message(f"❌ 微信消息发送失败: {error_msg}")
                    except Exception as e:
                        messagebox.showerror("发送错误", f"发送过程中出现未知错误！\n\n错误详情: {str(e)}\n\n请联系技术支持")
                        self.log_message(f"❌ 微信消息发送错误: {str(e)}")
                else:
                    messagebox.showwarning("提示", "请配置至少一个关键词")
            else:
                messagebox.showwarning("提示", "请先配置转发目标和关键词")
        
        ctk.CTkButton(
            button_frame,
            text="保存配置",
            command=save_config,
            width=120
        ).pack(side="left", padx=(0, 10))
        
        ctk.CTkButton(
            button_frame,
            text="测试转发",
            command=test_wechat,
            width=120
        ).pack(side="left", padx=10)
        
        ctk.CTkButton(
            button_frame,
            text="取消",
            command=config_window.destroy,
            width=120,
            fg_color="#e74c3c",
            hover_color="#c0392b"
        ).pack(side="right")
    
    def update_time(self):
        """更新时间显示"""
        current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        self.time_label.configure(text=current_time)
        self.root.after(1000, self.update_time)
    
    def on_closing(self):
        """程序关闭时的清理"""
        if self.monitoring_active:
            self.stop_monitoring()
        
        # 清理网络管理器和配置管理器
        try:
            if hasattr(self, 'network_manager') and self.network_manager:
                cleanup_network_manager()
                print("网络管理器已清理")
        except Exception as e:
            print(f"清理网络管理器失败: {str(e)}")
        
        try:
            if hasattr(self, 'config_manager') and self.config_manager:
                cleanup_config_manager()
                print("配置管理器已清理")
        except Exception as e:
            print(f"清理配置管理器失败: {str(e)}")
        
        self.root.destroy()
    
    def run(self):
        """运行程序"""
        self.root.protocol("WM_DELETE_WINDOW", self.on_closing)
        self.root.mainloop()

def main():
    """主函数"""
    app = ModernGridDesktopApp()
    app.run()

if __name__ == "__main__":
    main()