# 安装教程

本教程详细介绍 helloRepeater 插件的安装和配置过程。

## 📋 目录

1. [系统要求](#系统要求)
2. [从源码构建](#从源码构建)
3. [安装到 Burp Suite](#安装到-burp-suite)
4. [验证安装](#验证安装)
5. [常见问题](#常见问题)
6. [卸载方法](#卸载方法)

## 系统要求

### 必需环境

| 组件 | 最低版本 | 推荐版本 |
|------|---------|---------|
| Java | 17 | 17 LTS |
| Maven | 3.6 | 3.9+ |
| Burp Suite | 2023.1 | 最新版 |

### 检查环境

```bash
# 检查 Java 版本
java -version
# 应显示: openjdk version "17" 或更高

# 检查 Maven 版本
mvn -version
# 应显示: Apache Maven 3.6+ (或 3.9+)
```

### Burp Suite 版本确认

打开 Burp Suite，查看标题栏或 `Help` → `About` 确认版本号。

## 从源码构建

### 1. 获取源码

```bash
# 克隆仓库
git clone <repository-url>
cd helloRepeater

# 或者下载 ZIP 解压
unzip helloRepeater-master.zip
cd helloRepeater-master
```

### 2. 构建项目

```bash
# 完整构建（包含运行测试）
mvn clean package

# 快速构建（跳过测试，推荐用于安装）
mvn clean package -DskipTests
```

构建成功后会显示：
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  30.456 s
[INFO] Finished at: 2024-XX-XXTXX:XX:XX+XX:XX
[INFO] ------------------------------------------------------------------------
```

### 3. 构建产物

构建完成后，JAR 文件位于：
```
target/burp-repeater-manager-1.0.0.jar
```

这是一个 fat JAR，已包含所有依赖，可以直接加载到 Burp Suite。

### 4. 构建选项

```bash
# 仅编译，不打包
mvn clean compile

# 安装到本地 Maven 仓库
mvn clean install

# 清理构建产物
mvn clean
```

## 安装到 Burp Suite

### 方法 1：通过 Extensions 界面安装（推荐）

1. **打开 Burp Suite**，启动目标项目

2. **导航到 Extensions**：
   - 点击顶部菜单 `Extensions`
   - 选择 `Extensions` → `Installed`
   - 或点击左侧边栏的 `Extensions` 图标

3. **添加插件**：
   - 点击 `Add` 按钮
   - 在弹出窗口中：
     - **Extension type**: 选择 `Java`
     - **Extension file**: 点击 `Select file` 按钮
     - 选择构建好的 JAR 文件：`target/burp-repeater-manager-1.0.0.jar`
   - 点击 `Next`

4. **确认安装**：
   - 插件信息应显示：
     ```
     Extension: helloRepeater
     Description: Burp Suite Repeater Manager Plugin
     ```
   - 点击 `Close` 完成安装

### 方法 2：通过 BApp Store 安装（待上架）

> ⚠️ 注意：此插件尚未上架 BApp Store，请使用方法 1 安装。

上架后安装步骤：
1. `Extensions` → `BApp Store`
2. 搜索 "helloRepeater"
3. 点击 `Install`

### 安装后检查

安装成功后：
1. **Installed 列表**中应显示 `helloRepeater`
2. **界面变化**：
   - 出现 `helloRepeater` Tab 页面
   - 右键菜单出现 `发送到 Repeater Manager` 选项

## 验证安装

### 1. 检查日志

在 Burp Suite 的 `Dashboard` 或 `Extensions` → `Output` 中查看：

```
helloRepeater 插件初始化中...
SQLite 数据库初始化成功: <路径>/repeater_manager.db
helloRepeater 插件初始化完成！
```

### 2. 测试基本功能

**测试自动重命名**：
1. 打开 `Proxy` → `HTTP history`
2. 右键点击任意请求
3. 选择 `发送到 Repeater Manager` → `直接发送`
4. 切换到 `Repeater` Tab
5. 检查标签页名称是否已自动重命名（如：`1-GET-/api/test`）

**测试配置界面**：
1. 切换到 `helloRepeater` Tab
2. 确认显示三个子标签页：
   - 重命名规则
   - 分组管理
   - 规则测试

### 3. 检查数据库文件

插件会在 Burp Suite 启动目录创建数据库文件：
```
<burp-suite-directory>/repeater_manager.db
```

### 常见问题

#### Q1: 安装时提示 "Extension failed to load"

**原因分析**：
- Java 版本不兼容
- JAR 文件损坏
- 依赖缺失

**解决方案**：
1. 确认 Java 版本：
   ```bash
   java -version
   ```
2. 重新构建：
   ```bash
   mvn clean package -DskipTests
   ```
3. 检查 Burp Suite 的 `Extensions` → `Errors` 查看详细错误信息

#### Q2: 插件加载成功但功能不工作

**检查清单**：
- [ ] 是否能看到 `helloRepeater` Tab 页面？
- [ ] 右键菜单是否有 `发送到 Repeater Manager` 选项？
- [ ] 发送请求后 Repeater 标签页是否重命名？

**排查步骤**：
1. 查看 Burp Suite 输出日志
2. 检查数据库文件是否创建成功
3. 尝试重启 Burp Suite

#### Q3: 与其他插件冲突

如果安装其他 Repeater 管理插件，可能会：
- 右键菜单选项重复
- 功能相互覆盖

**建议**：
- 暂时禁用其他 Repeater 相关插件进行测试
- 在 `Extensions` → `Installed` 中管理插件启用状态

## 卸载方法

### 临时禁用

1. `Extensions` → `Installed`
2. 找到 `helloRepeater`
3. 取消勾选插件名称前的复选框

### 完全卸载

1. `Extensions` → `Installed`
2. 选中 `helloRepeater`
3. 点击 `Remove` 按钮
4. 删除数据库文件（可选）：
   ```
   <burp-suite-directory>/repeater_manager.db
   ```

### 数据备份

卸载前建议备份配置：
1. 打开 `helloRepeater` → `重命名规则`
2. 点击 `导出规则` 保存为 YAML 文件
3. 打开 `分组管理`
4. 点击 `导出分组` 保存为 YAML 文件

## 更新插件

### 保留数据更新

1. 导出当前配置（规则和分组）
2. 按照卸载步骤移除旧版本
3. 构建新版本 JAR
4. 安装新版本
5. 导入之前的配置

### 注意事项

- 数据库结构可能会在新版本中更新
- 建议在更新前备份数据库文件
- 查看版本更新日志了解破坏性变更

## 下一步

安装完成后，请阅读：
- [使用教程](USER_GUIDE.md) - 了解如何使用各项功能
- [规则配置](RULES_GUIDE.md) - 学习配置重命名规则

---

**需要帮助？** 查看 [故障排查](TROUBLESHOOTING.md) 或提交 Issue。
