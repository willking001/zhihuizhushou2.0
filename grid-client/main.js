const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const { Wechaty } = require('wechaty');

let mainWindow;
let bot;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    },
    icon: path.join(__dirname, 'assets/icon.png')
  });

  mainWindow.loadFile('index.html');

  if (process.argv.includes('--dev')) {
    mainWindow.webContents.openDevTools();
  }
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    if (bot) {
      bot.stop();
    }
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

// 微信机器人相关
ipcMain.handle('start-bot', async () => {
  try {
    bot = new Wechaty({
      name: 'dianxiaozhu-grid-bot',
      puppet: 'wechaty-puppet-wechat'
    });

    bot.on('scan', (qrcode, status) => {
      mainWindow.webContents.send('bot-scan', { qrcode, status });
    });

    bot.on('login', (user) => {
      mainWindow.webContents.send('bot-login', { user: user.name() });
    });

    bot.on('message', async (message) => {
      if (message.self()) return;
      
      const contact = message.talker();
      const text = message.text();
      const room = message.room();
      
      mainWindow.webContents.send('bot-message', {
        from: contact.name(),
        text: text,
        room: room ? room.topic() : null,
        timestamp: new Date().toISOString()
      });
    });

    await bot.start();
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle('stop-bot', async () => {
  try {
    if (bot) {
      await bot.stop();
      bot = null;
    }
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle('send-message', async (event, { contactName, message }) => {
  try {
    if (!bot) {
      throw new Error('机器人未启动');
    }
    
    const contact = await bot.Contact.find({ name: contactName });
    if (contact) {
      await contact.say(message);
      return { success: true };
    } else {
      throw new Error('联系人未找到');
    }
  } catch (error) {
    return { success: false, error: error.message };
  }
});