# 高级功能说明

本文档介绍 helloRepeater 的高级功能和进阶使用技巧，帮助你更高效地使用插件。

## 📋 目录

1. [右键菜单深度使用](#右键菜单深度使用)
2. [分组层级管理](#分组层级管理)
3. [批量操作技巧](#批量操作技巧)
4. [数据备份策略](#数据备份策略)
5. [团队协作配置](#团队协作配置)
6. [性能优化建议](#性能优化建议)
7. [快捷键和效率技巧](#快捷键和效率技巧)

## 右键菜单深度使用

### 右键菜单位置

helloRepeater 的右键菜单 `发送到 Repeater Manager` 在以下位置可用：

#### 1. Proxy History

**位置**：Proxy → HTTP history / WebSockets history

**使用方法**：
1. 在请求列表中右键点击单行
2. 或按住 `Ctrl` 键多选后右键
3. 选择发送到 Repeater Manager

**技巧**：
- 选择多个请求（按住 Ctrl 点击）可批量发送
- 所有选中的请求都会根据规则自动重命名

#### 2. Repeater 界面

**位置**：Repeater 标签页中的请求列表

**使用场景**：
- 复制已有请求到新标签页
- 基于现有请求创建变体

#### 3. Target Site Map

**位置**：Target → Site map 树形结构

**使用方法**：
1. 右键点击网站节点（发送该节点下所有请求）
2. 右键点击具体 URL

**注意事项**：
- 发送大量请求时可能产生多个 Repeater 标签页
- 建议先筛选目标请求

#### 4. Scanner Results

**位置**：Scanner → Results 列表

**使用场景**：
- 将扫描发现的请求发送到 Repeater 验证
- 分类保存不同类型的漏洞请求

#### 5. Intruder Results

**位置**：Intruder → Results 列表

**使用场景**：
- 保存成功的攻击载荷请求
- 分析不同响应的请求

### 快速选择分组技巧

当分组较多时：

1. **最近使用分组**：系统会自动记住你常用的分组，显示在列表顶部（如果实现）
2. **命名规范**：使用 `项目名_功能模块` 格式便于查找
   - 例如：`电商_用户模块`、`OA_审批流程`
3. **层级结构**：通过父分组组织，减少平级分组数量

### 与 Burp 原生功能的配合

```
原生发送到 Repeater → 手动命名（Ctrl+R）
helloRepeater 发送   → 自动命名 + 分组
```

**建议工作流**：
1. 快速测试：使用原生 Ctrl+R（保持习惯）
2. 需要记录：使用 helloRepeater 发送到指定分组
3. 批量操作：使用 helloRepeater 多选发送

## 分组层级管理

### 层级结构设计原则

**扁平结构**（适合简单项目）：
```
API测试
漏洞验证
配置检查
```

**层级结构**（适合复杂项目）：
```
项目名称
├── 用户模块
│   ├── 登录相关
│   └── 权限管理
├── 订单模块
│   ├── 创建订单
│   └── 订单查询
└── 支付模块
```

### 创建层级分组的步骤

**步骤 1：创建顶层分组**
```
名称: 电商项目
父分组: 无
描述: 电商平台渗透测试
```

**步骤 2：创建子分组**
```
名称: 用户模块
父分组: 电商项目
描述: 用户相关接口
```

**步骤 3：创建孙分组（可选）**
```
名称: 登录接口
父分组: 用户模块
描述: 登录相关接口测试
```

### 分组路径显示

发送到层级分组时，标签页名称格式：
```
父分组/子分组/序号-方法-提取内容

示例：
电商项目/用户模块/1-POST-login
```

### 分组循环引用检查

系统自动防止循环引用：
- ✅ 允许：A → B → C
- ❌ 禁止：A → B → A（编辑时会提示）

### 分组管理最佳实践

1. **项目隔离**：不同项目使用不同的顶层分组
2. **功能分类**：按业务模块划分子分组
3. **阶段标记**：使用分组标记测试阶段
   - `待验证`
   - `已确认`
   - `已修复`
4. **定期清理**：删除空分组和过期分组

## 批量操作技巧

### 批量发送请求

**方法**：在 Proxy History 中多选

**操作步骤**：
1. 按住 `Ctrl` 键点击选择多个请求
2. 右键点击选中的任意一行
3. 选择 `发送到 Repeater Manager` → `直接发送` 或 `选择分组`

**注意事项**：
- 大量请求（>50）可能需要等待
- 所有请求会按顺序处理
- 每个请求生成独立的 Repeater 标签页

### 批量应用规则

虽然不能直接重命名已有标签页，但可以：

**方法 1：重新发送**
1. 在 Repeater 中选择请求
2. 复制请求到剪贴板
3. 在 Proxy History 中右键 → Paste URL
4. 使用 helloRepeater 发送

**方法 2：导出再导入**
1. 导出 Repeater 请求（使用 Burp 原生功能）
2. 创建临时项目导入
3. 使用 helloRepeater 批量发送

### 批量编辑规则

**目前支持**：
- 在配置文件中直接编辑 YAML
- 导入修改后的配置

**操作步骤**：
1. 导出规则到 YAML 文件
2. 使用文本编辑器批量修改
3. 删除所有旧规则（可选）
4. 导入修改后的 YAML

**示例：批量修改优先级**
```yaml
# 编辑前
rules:
  - id: "rule-001"
    priority: 10
  - id: "rule-002"
    priority: 20

# 编辑后（全部加 10）
rules:
  - id: "rule-001"
    priority: 20
  - id: "rule-002"
    priority: 30
```

## 数据备份策略

### 数据库文件位置

SQLite 数据库文件默认存储在 Burp Suite 启动目录：

**Windows**：
```
C:\Program Files\BurpSuiteCommunity\repeater_manager.db
或
C:\Users\%USERNAME%\BurpSuite\repeater_manager.db
```

**macOS**：
```
/Applications/Burp Suite Community Edition.app/Contents/MacOS/repeater_manager.db
```

**Linux**：
```
/opt/burpsuite/repeater_manager.db
```

### 备份方法

#### 方法 1：导出配置文件（推荐）

**规则备份**：
1. 打开 helloRepeater → 重命名规则
2. 点击 `导出规则`
3. 保存为 `rules-backup-YYYYMMDD.yaml`

**分组备份**：
1. 打开 helloRepeater → 分组管理
2. 点击 `导出分组`
3. 保存为 `groups-backup-YYYYMMDD.yaml`

#### 方法 2：直接备份数据库

```bash
# 关闭 Burp Suite 后执行

# Windows
copy repeater_manager.db repeater_manager.db.backup.$(Get-Date -Format 'yyyyMMdd')

# Linux/macOS
cp repeater_manager.db repeater_manager.db.backup.$(date +%Y%m%d)
```

#### 方法 3：自动备份脚本

**Windows PowerShell**：
```powershell
# backup-burp-repeater.ps1
$source = "$env:USERPROFILE\BurpSuite\repeater_manager.db"
$destDir = "$env:USERPROFILE\BurpBackups"
$date = Get-Date -Format "yyyyMMdd_HHmmss"

if (Test-Path $source) {
    New-Item -ItemType Directory -Force -Path $destDir
    Copy-Item $source "$destDir\repeater_manager_$date.db"
    Write-Host "Backup created: $destDir\repeater_manager_$date.db"
}
```

### 恢复数据

**从配置恢复**：
1. 在对应标签页点击 `导入规则` 或 `导入分组`
2. 选择备份文件
3. 确认导入

**从数据库恢复**：
1. 关闭 Burp Suite
2. 用备份文件替换当前数据库
3. 重新启动 Burp Suite

### 定期备份建议

| 频率 | 操作 | 保留数量 |
|-----|------|---------|
| 每次重大变更 | 导出配置 | 最近 5 份 |
| 每周 | 备份数据库 | 最近 4 周 |
| 每月 | 完整备份 | 最近 3 个月 |

## 团队协作配置

### 配置共享流程

**负责人配置**：
1. 设计并测试规则集
2. 创建标准分组结构
3. 导出规则和分组配置
4. 保存到团队共享位置（Git/SVN/网盘）

**团队成员**：
1. 安装插件
2. 导入团队配置
3. 根据项目需要微调

### Git 管理配置

**仓库结构**：
```
burp-configs/
├── rules/
│   ├── common-rules.yaml      # 通用规则
│   ├── api-testing-rules.yaml # API测试规则
│   └── web-testing-rules.yaml # Web测试规则
├── groups/
│   ├── project-a-groups.yaml
│   └── project-b-groups.yaml
└── README.md
```

**提交规范**：
```
feat(rules): 添加 GraphQL 接口识别规则
fix(groups): 修正用户模块分组层级
update(common): 更新 API 版本号提取规则
```

### 多环境同步

**环境区分**：
```yaml
# dev-rules.yaml
规则:
  - 名称: "本地环境标识"
    type: PATH_REGEX
    pattern: "/localhost:8080/(\w+)"

# prod-rules.yaml  
规则:
  - 名称: "生产环境标识"
    type: PATH_REGEX
    pattern: "/api.example.com/(\w+)"
```

## 性能优化建议

### 规则优化

**1. 优先级排序**：
- 将最可能匹配的规则设为高优先级
- 减少规则遍历次数

**2. 正则表达式优化**：
```java
// 避免使用 .*
模式: /api/(v\d+)/.*  →  较好
模式: /api/(v\d+)/    →  更好（如果确定）

// 使用非贪婪匹配
模式: "action": "(.+?)"  →  较好
模式: "action": "(.+)"   →  可能过度匹配

// 避免回溯灾难
模式: (a+)+b  →  危险
模式: a+b     →  安全
```

**3. Body 解析优化**：
- Body 较大的请求，优先使用 PATH 规则
- JSONPath/XPath 在 Body > 1MB 时可能较慢

### 数据库优化

**定期清理**：
- 删除不用的规则
- 清理空分组
- 重置测试数据

**数据库维护**：
```sql
-- SQLite 优化（在数据库管理工具中执行）
VACUUM;
ANALYZE;
```

### 界面响应优化

**规则数量建议**：
- 启用规则：< 50 条
- 总规则：< 200 条
- 分组：< 50 个

**搜索过滤**：
- 使用搜索功能减少显示数量
- 避免同时加载大量数据

## 快捷键和效率技巧

### Burp Suite 原生快捷键

与 helloRepeater 配合使用的快捷键：

| 快捷键 | 功能 | 配合用法 |
|-------|------|---------|
| `Ctrl+R` | 发送到 Repeater | 快速发送，手动命名 |
| `Ctrl+Shift+R` | 发送到 Repeater（不激活） | 后台发送 |
| `Ctrl+单击` | 多选 | 批量发送到 helloRepeater |
| `Ctrl+F` | 搜索 | 在 Proxy History 中定位请求 |

### 效率工作流

**场景 1：API 接口批量测试**

```
1. Proxy History 中找到目标域名
2. 按接口路径排序（点击 Path 列）
3. 多选同一接口的不同请求
4. 右键 → 发送到 helloRepeater → 选择分组
5. 在 Repeater 中批量修改参数测试
```

**场景 2：漏洞验证**

```
1. Scanner 发现可疑问题
2. 右键问题请求 → 发送到 Repeater Manager
3. 发送到 "漏洞验证" 分组
4. 在 Repeater 中验证和修改
5. 确认后移动到 "已确认漏洞" 分组
```

**场景 3：规则开发和测试**

```
1. 在"规则测试"标签页准备测试请求
2. 创建新规则
3. 测试规则效果
4. 调整正则/JSONPath/XPath
5. 保存规则
6. 在 Proxy History 中实际测试
```

### 自定义快捷键（如果支持）

> 注意：当前版本不支持自定义快捷键，后续版本可能添加。

**建议的快捷键映射**（未来版本）：
```
Ctrl+Alt+R: 打开 helloRepeater 配置页
Ctrl+Shift+M: 快速发送到默认分组
```

## 实际工作流示例

### 渗透测试完整工作流

**准备阶段**：
```
1. 安装插件
2. 导入团队配置（规则和分组）
3. 创建项目专用分组
4. 配置项目特定规则
```

**测试阶段**：
```
5. 浏览目标网站
6. 关键请求发送到指定分组
7. 使用 Repeater 深入测试
8. 发现漏洞时标记分组
```

**报告阶段**：
```
9. 按分组整理请求
10. 导出相关请求
11. 生成测试报告
```

### 团队协作流程

```
成员 A                    成员 B                    成员 C
   |                        |                        |
   ├── 创建规则配置 ────────┼────────────────────────┤
   ├── 导出配置 ────────────┼────────────────────────┤
   |                        |                        |
   |                    导入配置                    导入配置
   |                        |                        |
   |                    使用测试                  使用测试
   |                        |                        |
   |                    反馈建议 ────────────────────┤
   |                        |                        |
   ├── 更新配置 ◀───────────┴────────────────────────┘
   └── 重新分享
```

---

## 总结

通过掌握这些高级功能，你可以：

- ✅ 提高渗透测试效率
- ✅ 更好地组织和管理 Repeater 标签页
- ✅ 与团队无缝协作
- ✅ 保护重要配置不丢失

**推荐学习路径**：
1. 先掌握 [基础使用教程](USER_GUIDE.md)
2. 深入理解 [规则配置](RULES_GUIDE.md)
3. 学习本文档的高级技巧
4. 参考 [FAQ](FAQ.md) 和 [故障排查](TROUBLESHOOTING.md) 解决实际问题

---

**持续优化**：根据实际使用场景不断调整规则和分组结构，找到最适合你工作流程的配置。
