# Docker 镜像加速器配置指南 (Windows)

## 问题背景

在您尝试构建和运行项目时，反复遇到 Docker 镜像拉取失败或速度过慢的问题。这是因为 Docker 默认从国外的 Docker Hub 官方仓库拉取镜像，由于网络原因，直接访问可能不稳定。

## 解决方案

为了解决这个问题，您需要在本地的 Docker 环境中配置一个国内的镜像加速器。这将使 Docker 通过国内的服务器来下载镜像，从而大大提高速度和成功率。

## 配置步骤

请按照以下步骤为您的 Docker Desktop for Windows 配置镜像加速器：

1.  **打开 Docker Desktop 设置**
    - 在系统托盘中右键点击 Docker 图标。
    - 选择 “Settings”（设置）。

2.  **进入 Docker Engine 配置**
    - 在左侧菜单中，选择 “Docker Engine”。

3.  **修改配置文件**
    - 在右侧的 JSON 编辑器中，找到或添加 `registry-mirrors` 键。请将以下内容完整地复制并粘贴到配置中：

    ```json
    {
      "builder": {
        "gc": {
          "defaultKeepStorage": "20GB",
          "enabled": true
        }
      },
      "experimental": false,
      "registry-mirrors": [
        "https://hub-mirror.c.163.com",
        "https://mirror.baidubce.com",
        "https://docker.nju.edu.cn"
      ]
    }
    ```

    **注意**：如果您的配置文件中已有其他内容，请确保只添加或修改 `registry-mirrors` 部分，不要删除其他现有配置。

4.  **应用并重启 Docker**
    - 点击右下角的 “Apply & Restart”（应用并重启）按钮。
    - Docker 将会保存您的配置并自动重启服务。

## 验证配置

重启完成后，您的 Docker 就配置好了镜像加速器。您可以再次尝试运行之前的 `docker-compose up --build` 命令，镜像拉取应该会变得顺畅。

---

**请在完成以上配置后，再继续执行项目的启动步骤。**