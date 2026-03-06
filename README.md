# Burp Suite Repeater Manager Plugin

一个功能强大的Burp Suite插件，用于增强Repeater功能，支持自动重命名标签页和分组管理。

## 功能特性

### 1. 自动重命名Repeater标签页
- 从历史记录窗口右键发送到Repeater时自动重命名
- 支持多种命名规则：路径截取、正则匹配、JSON Path、XPath
- 可配置规则优先级和启用状态
- 规则可导出导入，存储在SQLite数据库中

### 2. 分组管理
- 右键菜单支持选择现有分组或新建分组
- 支持层级分组结构（如果Burp API支持）
- 分组信息持久化存储在Burp配置中
- 支持分组数据的导出导入

### 3. 配置界面
- 独立的配置Tab页面
- 规则管理：添加、编辑、删除、启用/禁用
- 分组管理：添加、编辑、删除
- 规则测试工具
- 一键应用到所有标签页

### 4. 重命名规则类型
- **路径截取**：截取URL路径的指定范围（默认后16字符）
- **路径正则**：从路径中提取匹配的内容
- **Body正则**：从请求Body中通过正则提取
- **Body JSON Path**：从JSON Body中提取特定字段
- **Body XPath**：从XML Body中提取特定字段

## 预置规则

插件预置以下常用规则：

1. **路径后16字符** - 默认规则，截取路径最后16个字符
2. **API版本提取** - 从路径提取API版本号 (如 /api/v1/...)
3. **JSON Action字段** - 从JSON body提取action字段
4. **XML Method字段** - 从XML body提取method字段
5. **Body接口名提取** - 从Body提取interface字段

## 安装方法

### 方法1：直接加载JAR
1. 运行 `mvn clean package` 构建JAR文件
2. 在Burp Suite中，进入 Extensions → Installed → Add
3. 选择构建好的JAR文件：`target/burp-repeater-manager-1.0.0.jar`

### 方法2：从BApp Store安装（待上架）

## 使用方法

### 发送请求到Repeater
1. 在Proxy History或任何其他工具中右键点击请求
2. 选择 "发送到 Repeater Manager"
3. 选择：
   - **直接发送**：使用默认规则自动命名
   - **选择分组**：发送到指定分组
   - **新建分组**：创建新分组并发送

### 配置规则
1. 打开 "Repeater Manager" Tab
2. 在"重命名规则"标签页管理规则
3. 点击"添加规则"创建新规则
4. 选择规则类型并配置参数
5. 设置优先级（数字越小优先级越高）

### 测试规则
1. 切换到"规则测试"标签页
2. 输入HTTP请求数据
3. 点击"测试规则"查看结果

### 导出导入
- 规则导出：在规则页面点击"导出规则"，保存为JSON文件
- 规则导入：点击"导入规则"选择JSON文件
- 分组导出导入：类似操作在分组页面

## 文件存储位置

- **SQLite数据库**：`repeater_manager.db`（位于Burp Suite启动目录）
- **分组数据**：存储在Burp Suite的持久化存储中

## 构建要求

- Java 21+
- Gradle 8.x

## 技术栈

- Burp Suite Montoya API
- Jayway JSONPath (JSON处理)
- SQLite JDBC (数据存储)
- Jackson (JSON序列化)

## 许可证

MIT License

## 作者

oxff

## 更新日志

### v1.0.0
- 初始版本发布
- 实现自动重命名功能
- 实现分组管理功能
- 实现配置界面
- 支持5种规则类型
- 支持导入导出功能