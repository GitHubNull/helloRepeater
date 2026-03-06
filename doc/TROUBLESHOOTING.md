# 故障排查指南

本指南帮助你诊断和解决使用 helloRepeater 插件时遇到的问题。

## 📋 目录

1. [问题诊断流程](#问题诊断流程)
2. [安装问题](#安装问题)
3. [功能异常](#功能异常)
4. [规则匹配问题](#规则匹配问题)
5. [数据库问题](#数据库问题)
6. [界面问题](#界面问题)
7. [日志分析](#日志分析)
8. [联系支持](#联系支持)

## 问题诊断流程

### 标准排查步骤

当遇到问题时，请按以下顺序排查：

```
1. 确认问题现象
   ↓
2. 检查插件加载状态
   ↓
3. 查看错误日志
   ↓
4. 尝试基础解决方案
   ↓
5. 查阅相关文档
   ↓
6. 提交 Issue 或寻求帮助
```

### 诊断清单

**检查插件状态**：
- [ ] Extensions → Installed 中是否有 helloRepeater？
- [ ] 插件前面的复选框是否已勾选？
- [ ] 是否有错误提示？

**检查界面**：
- [ ] Tab 栏是否有 helloRepeater 标签页？
- [ ] 右键菜单是否有 "发送到 Repeater Manager"？
- [ ] 界面元素是否显示正常？

**检查功能**：
- [ ] 发送请求到 Repeater 是否成功？
- [ ] 标签页名称是否自动重命名？
- [ ] 配置界面是否可以正常操作？

## 安装问题

### 问题：无法加载 JAR 文件

**错误信息**：
```
Extension failed to load
或者
java.lang.UnsupportedClassVersionError
```

**排查步骤**：

**步骤 1：检查 Java 版本**
```bash
java -version
```
预期输出：
```
openjdk version "17" 2023-09-19
OpenJDK Runtime Environment (build 17+35-2724)
```

如果版本低于 17：
- 升级 Java：https://adoptium.net/
- 确保系统 PATH 指向正确版本

**步骤 2：检查 Burp Suite 使用的 Java**
```
Burp Suite → Help → About
查看 Java 版本信息
```

**步骤 3：重新构建 JAR**
```bash
# 清理并重新构建
mvn clean package -DskipTests

# 检查生成的文件
ls -lh target/burp-repeater-manager-1.0.0.jar
```

预期文件大小：> 5MB

**步骤 4：查看详细错误**
```
Burp Suite → Extensions → Errors
查看具体错误信息
```

### 问题：插件加载但无界面

**症状**：
- Extensions 列表显示已加载
- 看不到 helloRepeater Tab 页面
- 右键菜单没有新选项

**排查步骤**：

**步骤 1：检查 Tab 栏**
```
查看 Tab 栏右侧是否有 >> 按钮（更多标签页）
点击展开查看是否被折叠
```

**步骤 2：查看输出日志**
```
Extensions → Output → 选择 helloRepeater
查看是否有初始化成功的消息
```

正常输出：
```
helloRepeater 插件初始化中...
SQLite 数据库初始化成功: /path/to/repeater_manager.db
helloRepeater 插件初始化完成！
```

**步骤 3：检查错误日志**
```
Extensions → Errors → 选择 helloRepeater
查看是否有异常堆栈
```

**步骤 4：重启 Burp Suite**
```
完全关闭 Burp Suite
重新启动
重新加载插件
```

**步骤 5：检查冲突**
```
暂时禁用其他插件
特别是其他 Repeater 管理相关插件
查看是否恢复正常
```

### 问题：数据库初始化失败

**错误信息**：
```
SQLite 数据库初始化失败: [错误详情]
插件将以有限功能模式运行
```

**原因分析**：
1. 没有写权限
2. 磁盘空间不足
3. SQLite 驱动问题
4. 数据库文件损坏

**解决方案**：

**方案 1：检查写权限**
```bash
# 查看 Burp Suite 启动目录
# 确保有写入权限

# Windows: 以管理员身份运行 Burp Suite
# 或更改启动目录到用户目录

# Linux/macOS:
ls -ld /path/to/burp/directory
chmod 755 /path/to/burp/directory
```

**方案 2：检查磁盘空间**
```bash
# Windows
dir /s /q repeater_manager.db

# Linux/macOS
df -h
du -sh repeater_manager.db
```

**方案 3：删除损坏的数据库**
```bash
# 备份并删除
mv repeater_manager.db repeater_manager.db.backup.$(date +%Y%m%d)

# 重启插件会自动创建新数据库
```

## 功能异常

### 问题：右键菜单不显示

**症状**：
在 Proxy History 中右键看不到 "发送到 Repeater Manager"

**排查步骤**：

**步骤 1：确认插件已启用**
```
Extensions → Installed
确保 helloRepeater 已勾选
```

**步骤 2：检查右键位置**
```
✅ 支持的界面：
- Proxy → HTTP history
- Proxy → WebSockets history
- Repeater 列表
- Target → Site map
- Scanner → Results
- Intruder → Results

❌ 不支持：
- 单独的请求编辑器
- 某些第三方插件界面
```

**步骤 3：重启 Burp Suite**
```
右键菜单在运行时注册
如果注册失败需要重启
```

**步骤 4：检查冲突**
```
如果安装了多个右键菜单扩展
可能会被覆盖
```

### 问题：发送请求后未重命名

**症状**：
使用 helloRepeater 发送请求，但 Repeater 标签页仍显示默认名称

**排查步骤**：

**步骤 1：确认发送方式**
```
❌ 错误：使用 Ctrl+R 或 "Send to Repeater"
✅ 正确：使用 "发送到 Repeater Manager" → "直接发送"
```

**步骤 2：检查规则状态**
```
打开 helloRepeater → 重命名规则
确认：
- 至少有一条规则已启用
- 规则列表不为空
```

**步骤 3：测试规则匹配**
```
打开 helloRepeater → 规则测试
粘贴请求内容
点击测试规则
查看匹配结果
```

**步骤 4：检查日志**
```
Extensions → Output → helloRepeater
查看发送时的日志输出

应该看到：
已发送到Repeater: 生成的标题
```

**步骤 5：验证 Repeater**
```
切换到 Repeater 标签页
查看标签页标题
应该是：序号-方法-提取内容 格式
```

### 问题：规则测试无结果

**症状**：
在规则测试标签页点击测试，结果显示为空或不正确

**排查步骤**：

**步骤 1：检查请求格式**
```
确保请求格式正确：
GET /path HTTP/1.1
Host: example.com

Body content
```

**步骤 2：检查规则配置**
```
确认规则类型选择正确
确认模式填写正确
确认规则已启用
```

**步骤 3：单独测试每条规则**
```
禁用其他规则
只保留一条待测试规则
查看是否匹配
```

## 规则匹配问题

### 问题：正则表达式不生效

**症状**：
配置了 PATH_REGEX 或 BODY_REGEX 规则，但无法匹配

**调试方法**：

**方法 1：在线测试**
```
访问 https://regex101.com/
选择 Java 8 模式
输入正则表达式和测试字符串
查看匹配结果
```

**方法 2：简化测试**
```
原始规则：/api/(v\d+)/users/(\d+)
简化测试：/api/v1/users/123

先测试简单模式：
模式: /api/
应该匹配所有含 /api/ 的路径
```

**方法 3：查看日志**
```
如果正则语法错误，日志会显示：
正则规则执行失败: [pattern]
```

**常见问题**：

| 问题 | 原因 | 解决方案 |
|-----|------|---------|
| 特殊字符未转义 | 点号 . 匹配任意字符 | 使用 \. 匹配字面量点 |
| 贪婪匹配 | .* 匹配过多 | 使用 .*? 非贪婪匹配 |
| 分组错误 | 没有捕获组 | 添加括号 () |
| 大小写问题 | 默认区分大小写 | 使用 [a-zA-Z] 或 (?i) |

### 问题：JSONPath 返回空

**症状**：
BODY_JSON_PATH 规则匹配失败

**调试步骤**：

**步骤 1：验证 JSON 格式**
```
访问 https://jsonlint.com/
粘贴 Body 内容
确保 JSON 格式正确
```

**步骤 2：验证 JSONPath**
```
访问 https://jsonpath.com/
输入 JSON 和 JSONPath 表达式
查看结果
```

**步骤 3：检查路径**
```json
{
  "data": {
    "action": "create"
  }
}

$.action     → 错误
$.data.action → 正确
```

**步骤 4：检查数组**
```json
{
  "items": [{"name": "test"}]
}

$.items.name    → 错误
$.items[0].name → 正确
```

### 问题：XPath 提取失败

**症状**：
BODY_XPATH 规则无法从 XML 提取数据

**调试步骤**：

**步骤 1：验证 XML 格式**
```
访问 https://www.xmlvalidation.com/
检查 XML 格式是否正确
```

**步骤 2：简化 XPath**
```xml
<request><method>get</method></request>

//method/text()    → 正确
/request/method/text() → 也正确
```

**步骤 3：处理命名空间**
```xml
<soap:Envelope xmlns:soap="http://...">
  <soap:Body>
    <getUser/>
  </soap:Body>
</soap:Envelope>

//soap:Body/*[1]/local-name() → 正确
//Body/getUser → 错误（需要前缀）
```

## 数据库问题

### 问题：规则数据丢失

**症状**：
重启后规则列表为空或数据不完整

**排查步骤**：

**步骤 1：检查数据库文件**
```bash
# 确认文件存在
ls -lh repeater_manager.db

# 检查文件大小
# 空数据库约 20KB
# 有数据通常 > 50KB
```

**步骤 2：检查文件权限**
```bash
# 确保可读可写
ls -l repeater_manager.db
# 应该有 rw-r--r-- 权限
```

**步骤 3：检查数据库内容**
```bash
# 使用 SQLite 命令行
sqlite3 repeater_manager.db "SELECT COUNT(*) FROM rename_rules;"
```

**步骤 4：恢复备份**
```bash
# 如果有备份
cp repeater_manager.db.backup repeater_manager.db
```

### 问题：数据库锁定

**错误信息**：
```
database is locked
或
timeout waiting for lock
```

**原因**：
- 多个 Burp Suite 实例同时访问
- 上次关闭异常
- 其他程序占用

**解决方案**：

**步骤 1：关闭所有 Burp Suite 实例**

**步骤 2：删除锁定文件**
```bash
# 查找并删除
find . -name "*.db-journal" -delete
find . -name "*.db-wal" -delete
find . -name "*.db-shm" -delete
```

**步骤 3：重启 Burp Suite**

## 界面问题

### 问题：界面显示异常

**症状**：
- 表格列宽异常
- 文字显示不全
- 布局错乱

**解决方案**：

**方法 1：调整窗口大小**
```
尝试调整 Burp Suite 窗口大小
有些布局会根据窗口大小自适应
```

**方法 2：重启插件**
```
Extensions → Installed
取消勾选 helloRepeater
等待几秒
重新勾选
```

**方法 3：重置界面**
```
如果支持：
Window → Reset Layout
或者删除配置文件
```

### 问题：搜索功能无效

**症状**：
在规则或分组页面搜索无结果

**排查步骤**：

**步骤 1：检查搜索关键词**
```
确保关键词不为空
尝试简单的关键词如 "api"
```

**步骤 2：检查搜索模式**
```
简单匹配：适合普通搜索
正则匹配：确保正则语法正确
```

**步骤 3：清除搜索**
```
点击"清除"按钮
查看是否能显示所有数据
```

## 日志分析

### 如何查看日志

**输出日志**：
```
路径：Extensions → Output → helloRepeater
内容：正常运行信息
```

**错误日志**：
```
路径：Extensions → Errors → helloRepeater
内容：异常和错误堆栈
```

**Burp Suite 主日志**：
```
路径：Dashboard → Event log
或 Help → Diagnostics → View logs
```

### 常见日志信息

**正常初始化**：
```
helloRepeater 插件初始化中...
SQLite 数据库初始化成功: /path/repeater_manager.db
helloRepeater 插件初始化完成！
```

**发送请求**：
```
已发送到Repeater: 1-GET-/api/v1/users
```

**规则执行错误**：
```
正则规则执行失败: [pattern]
JSON Path规则执行失败: [path]
XPath规则执行失败: [path]
```

**数据库错误**：
```
加载规则失败: [SQL error]
保存规则失败: [SQL error]
```

### 启用调试模式

如果需要更详细的日志，可以：

**方法 1：修改代码**
```java
// 在 BurpRepeaterManager.java 中添加
api.logging().logToOutput("Debug: " + message);
```

**方法 2：增加日志点**
```java
// 在关键位置添加日志
api.logging().logToOutput("Entering method X");
api.logging().logToOutput("Variable value: " + value);
```

## 联系支持

### 提交 Issue

如果以上方法都无法解决问题，请提交 Issue：

**Issue 标题格式**：
```
[Bug/Question] 简短描述

示例：
[Bug] 正则规则无法匹配中文路径
[Question] 如何提取 URL 中的多个参数？
```

**Issue 内容模板**：
```markdown
## 环境信息
- Burp Suite 版本: [例如 2023.10.2]
- Java 版本: [例如 OpenJDK 17]
- 操作系统: [例如 Windows 11 / macOS 14 / Ubuntu 22.04]
- 插件版本: [例如 1.0.0]

## 问题描述
清晰准确地描述问题

## 复现步骤
1. 步骤 1
2. 步骤 2
3. 步骤 3

## 预期结果
描述期望的行为

## 实际结果
描述实际发生的行为

## 错误日志
```
粘贴相关的错误日志
```

## 截图
如果有界面问题，请提供截图

## 已尝试的解决方案
- [ ] 重启 Burp Suite
- [ ] 重新安装插件
- [ ] 检查规则配置
- [ ] 其他：___
```

### 提供诊断信息

**收集诊断信息**：

```bash
# 1. Burp Suite 版本
# 查看 Help → About

# 2. Java 版本
java -version

# 3. 操作系统信息
# Windows:
systeminfo | findstr /B /C:"OS Name" /C:"OS Version"

# Linux:
uname -a
lsb_release -a

# macOS:
system_profiler SPSoftwareDataType

# 4. 插件日志
# 导出 Extensions → Output/Errors 中的内容

# 5. 数据库信息（可选）
ls -lh repeater_manager.db
sqlite3 repeater_manager.db ".schema"
sqlite3 repeater_manager.db "SELECT COUNT(*) FROM rename_rules;"
```

### 安全注意事项

**提交 Issue 时请不要包含**：
- ❌ 敏感信息（密码、Token、密钥）
- ❌ 客户数据
- ❌ 内部网络信息
- ❌ 生产环境配置

**可以分享**：
- ✅ 脱敏后的请求示例
- ✅ 配置文件（去除敏感数据）
- ✅ 错误日志
- ✅ 界面截图

---

## 快速修复清单

### 大多数问题的通用解决方案

1. **重启 Burp Suite** - 解决 50% 的问题
2. **重新加载插件** - 解决 20% 的问题
3. **删除并重新安装** - 解决 15% 的问题
4. **检查日志** - 帮助诊断 10% 的问题
5. **提交 Issue** - 解决剩余 5% 的问题

### 一键诊断脚本（可选）

**Windows PowerShell**：
```powershell
Write-Host "=== helloRepeater 诊断工具 ===" -ForegroundColor Green

# 检查 Java
Write-Host "`n1. Java 版本:" -ForegroundColor Yellow
java -version 2>&1

# 检查数据库
Write-Host "`n2. 数据库文件:" -ForegroundColor Yellow
Get-ChildItem repeater_manager.db -ErrorAction SilentlyContinue | Format-Table Name, Length, LastWriteTime

# 检查 Burp 进程
Write-Host "`n3. Burp Suite 进程:" -ForegroundColor Yellow
Get-Process | Where-Object {$_.ProcessName -like "*burp*"} | Format-Table ProcessName, Id

Write-Host "`n诊断完成" -ForegroundColor Green
```

---

**希望这份指南能帮助你解决问题！**

如果仍有问题，请不要犹豫提交 Issue，社区会尽力帮助。
