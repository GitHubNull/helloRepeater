# helloRepeater - 快速开始指南

> 🚀 一个强大的 Burp Suite Repeater 标签页管理插件，支持自动重命名和智能分组

## ✨ 功能亮点

- **🎯 自动重命名** - 从 Proxy 历史记录发送到 Repeater 时自动根据规则重命名标签页
- **📁 智能分组** - 支持将请求发送到指定分组，便于管理大量标签页
- **🛠️ 灵活规则** - 支持 5 种规则类型：路径截取、正则匹配、JSON Path、XPath 等
- **💾 数据持久化** - 规则和分组数据自动保存，支持导入导出
- **🔍 快速搜索** - 支持规则和分组的搜索过滤功能

## 🚀 5 分钟快速上手

### 1. 安装插件

```bash
# 克隆项目
git clone <repository-url>
cd helloRepeater

# 构建 JAR 包
mvn clean package -DskipTests
```

在 Burp Suite 中加载：`Extensions` → `Installed` → `Add` → 选择 `target/burp-repeater-manager-1.0.0.jar`

### 2. 发送到 Repeater

1. 在 **Proxy History** 或任意工具中右键点击请求
2. 选择 `发送到 Repeater Manager` → `直接发送`
3. 插件会自动根据规则重命名 Repeater 标签页

### 3. 配置规则

1. 切换到 **helloRepeater** Tab 页面
2. 在"重命名规则"标签页管理规则
3. 双击规则或点击"编辑规则"修改配置

## 📖 详细文档

- **[安装教程](INSTALLATION.md)** - 完整的安装和配置步骤
- **[使用教程](USER_GUIDE.md)** - 详细的功能使用说明
- **[规则配置](RULES_GUIDE.md)** - 重命名规则的完整配置指南
- **[高级功能](ADVANCED_FEATURES.md)** - 进阶使用技巧
- **[常见问题](FAQ.md)** - 常见问题解答
- **[故障排查](TROUBLESHOOTING.md)** - 问题诊断和解决方案

## 🎯 适用场景

- ✅ API 接口测试时需要清晰的标签页命名
- ✅ 渗透测试中管理大量 Repeater 标签页
- ✅ 团队协作时统一命名规范
- ✅ 自动化测试报告生成

## 📝 示例效果

**传统方式：**
```
Repeater 标签页: 1, 2, 3, 4, 5...（难以区分）
```

**使用 helloRepeater：**
```
Repeater 标签页:
- 1-GET-/api/v1/users
- 2-POST-/api/login
- 3-GET-/admin/config
- API测试/1-GET-/users/list
```

## 🔧 系统要求

- Java 17 或更高版本
- Burp Suite Professional/Community 2023.x+
- 操作系统：Windows / macOS / Linux

## 📊 技术栈

- **Burp Montoya API** - Burp Suite 扩展接口
- **SQLite** - 规则数据存储
- **Jayway JSONPath** - JSON 数据处理
- **Jackson** - 数据序列化

## 🤝 参与贡献

欢迎提交 Issue 和 Pull Request！

## 📜 许可证

MIT License - 详见 [LICENSE](../LICENSE) 文件

## 👤 作者

**oxff**

---

> 💡 **提示**：首次使用建议先阅读 [使用教程](USER_GUIDE.md) 了解完整功能！
