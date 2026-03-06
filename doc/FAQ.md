# 常见问题解答 (FAQ)

本文档收集了 helloRepeater 用户最常遇到的问题及其解答。

## 📋 目录

1. [安装相关问题](#安装相关问题)
2. [功能使用问题](#功能使用问题)
3. [规则配置问题](#规则配置问题)
4. [分组管理问题](#分组管理问题)
5. [数据存储问题](#数据存储问题)
6. [兼容性问题](#兼容性问题)
7. [性能问题](#性能问题)

## 安装相关问题

### Q1: 安装插件时提示 "Extension failed to load"

**问题描述**：
在 Burp Suite 中添加 JAR 文件时，出现错误提示无法加载扩展。

**可能原因及解决方案**：

**原因 1：Java 版本不兼容**
- **检查方法**：运行 `java -version`
- **解决方案**：升级到 Java 17 或更高版本
- **验证**：确保版本号显示为 `openjdk version "17"` 或更高

**原因 2：JAR 文件损坏或不完整**
- **检查方法**：查看文件大小是否正常（应该 > 5MB）
- **解决方案**：
  ```bash
  # 重新构建
  mvn clean package -DskipTests
  ```

**原因 3：依赖缺失**
- **检查方法**：查看 Burp Suite 的 Errors 标签页
- **解决方案**：确保使用的是 fat JAR（已包含所有依赖）

**原因 4：Burp Suite 版本过低**
- **检查方法**：查看 Help → About
- **解决方案**：升级到 Burp Suite 2023.x 或更高版本

### Q2: 插件加载成功但界面没有变化

**问题描述**：
插件显示已加载，但没有看到 helloRepeater Tab 页面。

**排查步骤**：

1. **检查 Tab 栏**：
   - 查看 Tab 栏右侧是否有更多标签页（点击 >> 按钮）
   - 可能被折叠了

2. **重启 Burp Suite**：
   - 有些情况下需要重启才能完全初始化

3. **检查日志**：
   - 查看 Extensions → Output 中的输出信息
   - 应该显示：
     ```
     helloRepeater 插件初始化完成！
     ```

4. **重新安装**：
   - Extensions → Installed → 选中 helloRepeater → Remove
   - 重新添加 JAR 文件

### Q3: 如何确认插件安装成功？

**验证清单**：

- [ ] Extensions → Installed 列表中有 helloRepeater
- [ ] Tab 栏中有 helloRepeater 标签页
- [ ] Proxy History 中右键有 "发送到 Repeater Manager" 菜单
- [ ] 发送请求后 Repeater 标签页名称自动变更

**验证命令**：
```
1. 打开任意请求
2. 右键 → 发送到 Repeater Manager → 直接发送
3. 切换到 Repeater 标签页
4. 检查标签页名称是否变成了 "序号-方法-提取内容" 格式
```

## 功能使用问题

### Q4: 为什么发送请求后标签页没有重命名？

**可能原因**：

**原因 1：使用了原生发送到 Repeater**
- **识别**：使用 Ctrl+R 或右键的 "Send to Repeater"
- **解决**：使用右键的 "发送到 Repeater Manager"

**原因 2：规则未匹配成功**
- **检查**：切换到 helloRepeater → 规则测试
- **测试**：输入请求查看哪条规则匹配

**原因 3：规则被禁用**
- **检查**：查看规则列表的"启用"列是否勾选
- **解决**：启用需要的规则

**原因 4：规则优先级问题**
- **分析**：高优先级规则匹配但提取为空
- **解决**：调整规则优先级或修改规则

**调试步骤**：
```
1. 打开规则测试标签页
2. 粘贴未重命名的请求
3. 点击测试规则
4. 查看哪条规则匹配成功
5. 如果都未匹配，检查默认规则（路径后16字符）
```

### Q5: 右键菜单在哪里？

**支持右键的位置**：
- ✅ Proxy → HTTP history
- ✅ Proxy → WebSockets history
- ✅ Repeater 请求列表
- ✅ Target → Site map
- ✅ Scanner → Results
- ✅ Intruder → Results

**不显示右键菜单？**

1. 确保插件已正确加载
2. 尝试在 Proxy History 中右键
3. 检查是否有其他插件冲突
4. 重启 Burp Suite

### Q6: 如何重命名已存在的 Repeater 标签页？

**回答**：
当前版本不支持直接重命名已存在的标签页。

**变通方案**：

**方案 1：重新发送**
1. 复制已有请求的内容
2. 在 Proxy History 中粘贴 URL
3. 使用 helloRepeater 重新发送

**方案 2：导出导入**
1. 导出 Repeater 请求（使用 Burp 原生功能）
2. 删除旧标签页
3. 重新导入并发送

**方案 3：手动重命名**
- 使用 Burp Suite 原生的标签页重命名功能（如果有）

### Q7: 发送到分组后，标签页名称是什么样的？

**命名格式**：
```
分组路径/序号-方法-提取内容

示例：
- API测试/1-GET-users/list
- 漏洞验证/SQL注入/3-POST-admin/login
```

**父分组显示**：
- 如果有父分组，会显示完整路径
- 例如：项目A/用户模块/登录接口

## 规则配置问题

### Q8: 如何编写正则表达式规则？

**基础示例**：

**提取数字 ID**：
```
模式: /api/\w+/(\d+)
输入: /api/users/12345
输出: 12345
```

**提取版本号**：
```
模式: /(v\d+(?:\.\d+)*)/
输入: /api/v2.1/users
输出: v2.1
```

**提取最后一段路径**：
```
模式: /([^/]+)$
输入: /api/v1/users/admin
输出: admin
```

**在线测试工具**：
- https://regex101.com/
- https://regexr.com/

**测试步骤**：
1. 在规则测试标签页输入请求
2. 创建新规则（类型选择 PATH_REGEX）
3. 输入正则表达式
4. 保存并在规则测试页面验证

### Q9: JSONPath 规则总是匹配失败？

**常见错误**：

**错误 1：路径错误**
```json
// 错误
{
  "data": {
    "action": "create"
  }
}
JSONPath: $.action  // ❌ 失败
正确: $.data.action  // ✅ 成功
```

**错误 2：数组索引**
```json
// 错误
{
  "items": [{"name": "item1"}]
}
JSONPath: $.items.name      // ❌ 失败
正确: $.items[0].name       // ✅ 成功
正确: $.items[*].name       // ✅ 返回数组
```

**错误 3：特殊字符**
```json
// 字段名包含特殊字符
{
  "user-name": "test"
}
JSONPath: $['user-name']    // ✅ 使用括号表示法
```

**在线测试工具**：
- https://jsonpath.com/

### Q10: XPath 规则如何提取数据？

**基础示例**：

**提取标签文本**：
```xml
<request>
  <method>getUser</method>
</request>

XPath: //method/text()
输出: getUser
```

**提取属性**：
```xml
<user id="123" name="test"/>

XPath: //@id
输出: 123
```

**处理命名空间**：
```xml
<soap:Envelope xmlns:soap="http://...">
  <soap:Body>
    <getUser/>
  </soap:Body>
</soap:Envelope>

XPath: //soap:Body/*[1]/local-name()
输出: getUser
```

### Q11: 规则的优先级如何设置？

**优先级原则**：
- 数字越小，优先级越高
- 1 是最高优先级
- 999 是最低优先级

**推荐设置**：
```
优先级 1-10:   项目特定的关键规则
优先级 11-50:  通用的业务规则
优先级 51-100: 默认规则
优先级 100+:    备用兜底规则
```

**调整方法**：
1. 双击规则编辑
2. 修改优先级数值
3. 保存

或使用右键菜单：
- 右键规则 → 上移（优先级减 1）
- 右键规则 → 下移（优先级加 1）

## 分组管理问题

### Q12: 如何创建层级分组？

**步骤**：

1. **创建顶层分组**：
   ```
   名称: 项目名称
   父分组: 无
   ```

2. **创建子分组**：
   ```
   名称: 功能模块
   父分组: 项目名称
   ```

3. **创建孙分组（可选）**：
   ```
   名称: 具体功能
   父分组: 功能模块
   ```

**层级显示**：
- 子分组在父分组下缩进显示
- 发送时显示完整路径

### Q13: 删除分组会影响已发送的请求吗？

**回答**：不会。

**说明**：
- 删除分组只删除分组记录
- 已发送的 Repeater 标签页名称不会改变
- 已保存的数据不会丢失

**影响**：
- 无法再通过此分组发送新请求
- 历史统计数据会丢失

### Q14: 分组可以重命名吗？

**回答**：可以。

**方法**：
1. 在分组管理标签页选中分组
2. 双击或点击"编辑分组"
3. 修改名称
4. 保存

**注意**：
- 重命名后，新发送的请求会使用新名称
- 已存在的 Repeater 标签页名称不变

## 数据存储问题

### Q15: 规则和分组数据存储在哪里？

**规则数据**：
- 位置：SQLite 数据库文件
- 路径：Burp Suite 启动目录下的 `repeater_manager.db`
- 内容：所有重命名规则

**分组数据**：
- 位置：Burp Suite 的持久化存储
- 路径：Burp 项目文件内部
- 内容：分组结构和配置

**如何查找数据库文件**：

**Windows**：
```powershell
# 在 Burp Suite 启动目录
Get-ChildItem repeater_manager.db
```

**Linux/macOS**：
```bash
# 在 Burp Suite 启动目录
ls -la repeater_manager.db
```

### Q16: 如何备份配置？

**方法 1：导出配置（推荐）**

```
1. helloRepeater → 重命名规则 → 导出规则
2. helloRepeater → 分组管理 → 导出分组
3. 保存 YAML 文件到安全位置
```

**方法 2：备份数据库**

```bash
# 关闭 Burp Suite 后

# Windows
copy repeater_manager.db repeater_manager.db.backup

# Linux/macOS
cp repeater_manager.db repeater_manager.db.backup
```

### Q17: 重装系统后如何恢复数据？

**恢复步骤**：

**如果有配置文件备份**：
1. 重新安装插件
2. 导入规则和分组配置

**如果有数据库备份**：
1. 重新安装插件
2. 关闭 Burp Suite
3. 将备份的数据库文件复制到启动目录
4. 重新启动 Burp Suite

**如果没有备份**：
- 需要重新配置规则和分组
- 建议定期备份

### Q18: 数据库文件损坏了怎么办？

**症状**：
- 插件无法加载
- 规则列表为空
- 报错信息包含 "SQLite"

**解决方案**：

**步骤 1：备份损坏的数据库**
```bash
mv repeater_manager.db repeater_manager.db.corrupted
```

**步骤 2：重启 Burp Suite**
- 插件会自动创建新数据库
- 恢复默认规则

**步骤 3：恢复配置**
- 从 YAML 备份文件导入
- 或手动重新配置

**步骤 4：尝试修复（可选）**
```bash
# 使用 SQLite 命令行工具
sqlite3 repeater_manager.db.corrupted ".dump" > dump.sql
sqlite3 repeater_manager.db.new < dump.sql
```

## 兼容性问题

### Q19: 与其他 Repeater 管理插件冲突？

**可能现象**：
- 右键菜单重复
- 功能异常
- 界面显示错误

**解决方案**：

1. **暂时禁用其他插件**：
   - Extensions → Installed
   - 取消勾选其他 Repeater 相关插件

2. **调整插件加载顺序**：
   - 有些插件可以调整加载顺序
   - 将 helloRepeater 设为后加载

3. **联系开发者**：
   - 提交 Issue 说明冲突情况

### Q20: 支持 Burp Suite Community Edition 吗？

**回答**：支持。

**说明**：
- 完全兼容 Community Edition
- 所有功能都可以正常使用
- 不需要 Professional 版本的特性

### Q21: 支持哪些 Burp Suite 版本？

**支持版本**：
- Burp Suite 2023.x 及以上
- 推荐使用最新版本

**不支持的版本**：
- 2022.x 及以下（未测试）
- 如果使用旧版本遇到问题，建议升级

## 性能问题

### Q22: 插件会影响 Burp Suite 性能吗？

**影响程度**：
- 正常使用：几乎没有影响
- 大量规则（>100）：界面加载可能稍慢
- 复杂正则：匹配时可能有轻微延迟

**优化建议**：
- 规则数量控制在 50 条以内
- 禁用不用的规则
- 定期清理无用数据

### Q23: 发送大量请求时卡住？

**可能原因**：
- 规则匹配耗时较长
- 数据库写入阻塞
- Burp Suite 界面渲染

**解决方案**：

1. **分批发送**：
   - 每次发送不超过 50 个请求
   - 等待一批完成后再发下一批

2. **简化规则**：
   - 临时禁用复杂规则
   - 使用简单的 PATH_SUBSTRING 规则

3. **增加内存**：
   - 增加 Burp Suite 的 JVM 内存
   - 编辑启动脚本增加 `-Xmx` 参数

### Q24: 规则测试界面响应慢？

**优化方法**：

1. **减少测试数据**：
   - 移除不必要的 Header
   - 使用简化的请求体

2. **禁用复杂规则**：
   - 临时禁用 BODY_REGEX 规则
   - 这些规则需要解析整个 Body

3. **分批测试**：
   - 每次测试一条规则
   - 不要同时测试所有规则

---

## 问题未解决？

如果以上 FAQ 没有解决你的问题：

1. **查看故障排查指南**：[TROUBLESHOOTING.md](TROUBLESHOOTING.md)
2. **提交 Issue**：在 GitHub 上提交问题报告
3. **联系开发者**：通过邮件或社交媒体联系

**提交 Issue 时请提供**：
- Burp Suite 版本
- Java 版本
- 操作系统
- 问题描述
- 复现步骤
- 错误日志（如果有）

---

**最后更新**：2024年
