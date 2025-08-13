const { ipcRenderer } = require('electron');
const QRCode = require('qrcode');

const startBtn = document.getElementById('startBtn');
const stopBtn = document.getElementById('stopBtn');
const status = document.getElementById('status');
const qrContainer = document.getElementById('qrContainer');
const qrCode = document.getElementById('qrCode');
const userInfo = document.getElementById('userInfo');
const userName = document.getElementById('userName');
const messages = document.getElementById('messages');

// 启动机器人
startBtn.addEventListener('click', async () => {
    try {
        startBtn.disabled = true;
        startBtn.textContent = '启动中...';
        
        const result = await ipcRenderer.invoke('start-bot');
        
        if (result.success) {
            updateStatus('连接中', 'offline');
            startBtn.classList.add('hidden');
            stopBtn.classList.remove('hidden');
        } else {
            addMessage('系统错误', `启动失败: ${result.error}`);
            startBtn.disabled = false;
            startBtn.textContent = '启动微信机器人';
        }
    } catch (error) {
        addMessage('系统错误', `启动失败: ${error.message}`);
        startBtn.disabled = false;
        startBtn.textContent = '启动微信机器人';
    }
});

// 停止机器人
stopBtn.addEventListener('click', async () => {
    try {
        const result = await ipcRenderer.invoke('stop-bot');
        
        if (result.success) {
            updateStatus('离线', 'offline');
            startBtn.classList.remove('hidden');
            stopBtn.classList.add('hidden');
            qrContainer.classList.add('hidden');
            userInfo.classList.add('hidden');
            startBtn.disabled = false;
            startBtn.textContent = '启动微信机器人';
            addMessage('系统消息', '机器人已停止');
        }
    } catch (error) {
        addMessage('系统错误', `停止失败: ${error.message}`);
    }
});

// 监听机器人事件
ipcRenderer.on('bot-scan', async (event, { qrcode, status: scanStatus }) => {
    try {
        qrContainer.classList.remove('hidden');
        const qrCodeDataURL = await QRCode.toDataURL(qrcode);
        qrCode.innerHTML = `<img src="${qrCodeDataURL}" alt="QR Code" style="max-width: 100%;">`;
        updateStatus('等待扫码', 'offline');
        addMessage('系统消息', '请使用微信扫描二维码登录');
    } catch (error) {
        addMessage('系统错误', `生成二维码失败: ${error.message}`);
    }
});

ipcRenderer.on('bot-login', (event, { user }) => {
    qrContainer.classList.add('hidden');
    userInfo.classList.remove('hidden');
    userName.textContent = user;
    updateStatus('在线', 'online');
    addMessage('系统消息', `用户 ${user} 登录成功`);
});

ipcRenderer.on('bot-message', (event, { from, text, room, timestamp }) => {
    const source = room ? `${room} - ${from}` : from;
    addMessage(source, text, timestamp);
});

// 更新状态
function updateStatus(text, className) {
    status.textContent = text;
    status.className = `status ${className}`;
}

// 添加消息
function addMessage(source, content, timestamp = null) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    
    const time = timestamp ? new Date(timestamp).toLocaleTimeString() : new Date().toLocaleTimeString();
    
    messageDiv.innerHTML = `
        <div class="message-header">
            <span>${source}</span>
            <span>${time}</span>
        </div>
        <div class="message-content">${content}</div>
    `;
    
    messages.appendChild(messageDiv);
    messages.scrollTop = messages.scrollHeight;
    
    // 限制消息数量
    const messageElements = messages.querySelectorAll('.message');
    if (messageElements.length > 100) {
        messageElements[0].remove();
    }
}

// 初始化
addMessage('系统消息', '电小助网格员客户端已启动');