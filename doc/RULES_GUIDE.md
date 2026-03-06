# 重命名规则配置指南

本指南详细介绍 helloRepeater 支持的 5 种重命名规则类型，包含配置方法、参数说明和实用示例。

## 📋 目录

1. [规则概述](#规则概述)
2. [规则类型详解](#规则类型详解)
3. [优先级机制](#优先级机制)
4. [规则匹配流程](#规则匹配流程)
5. [实战配置案例](#实战配置案例)
6. [规则模板库](#规则模板库)

## 规则概述

helloRepeater 支持 **5 种规则类型**，可以满足从简单到复杂的各种命名需求：

| 规则类型 | 用途 | 复杂度 | 适用场景 |
|---------|------|--------|---------|
| PATH_SUBSTRING | 路径截取 | ⭐ | 简单的 URL 路径提取 |
| PATH_REGEX | 路径正则 | ⭐⭐ | 从 URL 提取特定模式 |
| BODY_REGEX | Body 正则 | ⭐⭐⭐ | 从请求体提取文本 |
| BODY_JSON_PATH | JSON 提取 | ⭐⭐ | 从 JSON 提取字段 |
| BODY_XPATH | XML 提取 | ⭐⭐⭐ | 从 XML/SOAP 提取数据 |

## 规则类型详解

### 1. PATH_SUBSTRING - 路径截取

**功能**：从 URL 路径中截取指定范围的字符

**适用场景**：
- 获取路径的最后 N 个字符
- 截取路径的特定段落
- 简单的路径格式化

**配置参数**：

| 参数 | 类型 | 说明 | 默认值 |
|-----|------|------|--------|
| pattern | 字符串 | 特殊值 "last16" 表示取后 16 字符 | last16 |
| pathStartIndex | 整数 | 开始索引（从 0 开始） | -1 |
| pathEndIndex | 整数 | 结束索引（-1 表示到最后） | -1 |

**配置示例**：

**示例 1：取路径后 16 字符（默认）**
```
类型: PATH_SUBSTRING
模式: last16
开始索引: -1
结束索引: -1
```
- 输入: `/api/v1/users/admin/profile`
- 输出: `ers/admin/profile`

**示例 2：从第 5 个字符开始取**
```
类型: PATH_SUBSTRING
模式: (空)
开始索引: 5
结束索引: -1
```
- 输入: `/api/v1/users`
- 输出: `v1/users`

**示例 3：取固定范围**
```
类型: PATH_SUBSTRING
模式: (空)
开始索引: 0
结束索引: 10
```
- 输入: `/api/v1/users/list`
- 输出: `/api/v1/use`

### 2. PATH_REGEX - 路径正则匹配

**功能**：使用正则表达式从 URL 路径中提取内容

**适用场景**：
- 提取 API 版本号
- 提取资源 ID
- 提取特定的路径段

**配置参数**：

| 参数 | 说明 | 示例 |
|-----|------|------|
| pattern | 正则表达式 | `/api/(v\d+).*` |

**正则技巧**：
- 使用捕获组 `()` 提取特定部分
- 第一个捕获组 `(\d+)` 的内容会被提取
- 如果没有捕获组，匹配到的完整文本会被提取

**配置示例**：

**示例 1：提取 API 版本号**
```
类型: PATH_REGEX
模式: /api/(v\d+)/.*
```
- 输入: `/api/v2/users/list`
- 输出: `v2`

**示例 2：提取资源类型和 ID**
```
类型: PATH_REGEX
模式: /api/\w+/(\d+)
```
- 输入: `/api/users/12345`
- 输出: `12345`

**示例 3：提取多个路径段**
```
类型: PATH_REGEX
模式: /api/(v\d+)/(\w+)/
```
- 输入: `/api/v1/users/`
- 输出: `v1`（取第一个捕获组）

**常用正则表达式**：

```java
// 提取 UUID
[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}

// 提取数字 ID
/(\d+)(?:/|$)

// 提取版本号
/(v\d+(?:\.\d+)*)/

// 提取文件扩展名
\.(\w+)$

// 提取域名路径后的第一段
^/[^/]+/(\w+)
```

### 3. BODY_REGEX - Body 正则匹配

**功能**：从 HTTP 请求 Body 中使用正则表达式提取内容

**适用场景**：
- 提取表单字段
- 从文本 Body 提取特定值
- 提取隐藏参数

**配置参数**：

| 参数 | 说明 | 示例 |
|-----|------|------|
| pattern | 正则表达式（支持 DOTALL 模式） | `"action":\s*"([^"]+)"` |

**注意事项**：
- 自动启用 DOTALL 模式（`.` 可匹配换行符）
- Body 为空时规则自动跳过
- 支持多行文本匹配

**配置示例**：

**示例 1：提取 JSON 字段（简单情况）**
```
类型: BODY_REGEX
模式: "action":\s*"([^"]+)"
```
- 输入 Body:
  ```json
  {"action": "createUser", "name": "test"}
  ```
- 输出: `createUser`

**示例 2：提取表单数据**
```
类型: BODY_REGEX
模式: username=(\w+)
```
- 输入 Body: `username=admin&password=123456`
- 输出: `admin`

**示例 3：提取多行内容**
```
类型: BODY_REGEX
模式: --boundary[\s\S]+?name="([^"]+)"
```
- 适用于 multipart/form-data

**示例 4：提取 XML 标签内容（简单 XML）**
```
类型: BODY_REGEX
模式: <method>([^<]+)</method>
```
- 输入 Body: `<request><method>getUser</method></request>`
- 输出: `getUser`

### 4. BODY_JSON_PATH - JSON 提取

**功能**：使用 JSONPath 表达式从 JSON Body 中提取字段值

**适用场景**：
- RESTful API 测试
- 提取 JSON 中的特定字段
- 处理复杂的 JSON 结构

**配置参数**：

| 参数 | 说明 | 示例 |
|-----|------|------|
| pattern | JSONPath 表达式 | `$.action` 或 `$.data[0].id` |

**JSONPath 语法速查**：

| 表达式 | 说明 | 示例 |
|-------|------|------|
| `$` | 根对象 | `$` |
| `.` | 子元素 | `$.name` |
| `..` | 递归下降 | `$..name` |
| `[]` | 数组索引 | `$.users[0]` |
| `[*]` | 所有数组元素 | `$.users[*]` |
| `[start:end]` | 数组切片 | `$.users[0:3]` |
| `[?(@.key)]` | 过滤 | `$.users[?(@.age > 18)]` |

**配置示例**：

**示例 1：提取根级字段**
```
类型: BODY_JSON_PATH
模式: $.action
```
- 输入 Body:
  ```json
  {"action": "delete", "id": 123}
  ```
- 输出: `delete`

**示例 2：提取嵌套字段**
```
类型: BODY_JSON_PATH
模式: $.data.user.name
```
- 输入 Body:
  ```json
  {"data": {"user": {"name": "admin", "role": "super"}}}
  ```
- 输出: `admin`

**示例 3：提取数组元素**
```
类型: BODY_JSON_PATH
模式: $.items[0].name
```
- 输入 Body:
  ```json
  {"items": [{"name": "item1"}, {"name": "item2"}]}
  ```
- 输出: `item1`

**示例 4：提取数组长度**
```
类型: BODY_JSON_PATH
模式: $.items.length()
```
- 输入 Body:
  ```json
  {"items": [1, 2, 3, 4, 5]}
  ```
- 输出: `5`

**示例 5：使用过滤表达式**
```
类型: BODY_JSON_PATH
模式: $.users[?(@.role == 'admin')].name
```
- 输入 Body:
  ```json
  {"users": [
    {"name": "user1", "role": "user"},
    {"name": "admin1", "role": "admin"}
  ]}
  ```
- 输出: `["admin1"]`

### 5. BODY_XPATH - XML 提取

**功能**：使用 XPath 表达式从 XML Body 中提取数据

**适用场景**：
- SOAP Web Service 测试
- XML API 测试
- 处理 RSS/Atom 源

**配置参数**：

| 参数 | 说明 | 示例 |
|-----|------|------|
| pattern | XPath 表达式 | `//method/text()` 或 `/root/element/@attr` |

**XPath 语法速查**：

| 表达式 | 说明 | 示例 |
|-------|------|------|
| `/` | 从根节点选择 | `/root` |
| `//` | 从任意位置选择 | `//element` |
| `.` | 当前节点 | `.//child` |
| `..` | 父节点 | `..` |
| `@` | 属性 | `//@id` |
| `text()` | 文本内容 | `//name/text()` |
| `[]` | 条件过滤 | `//item[1]` |

**配置示例**：

**示例 1：提取标签文本内容**
```
类型: BODY_XPATH
模式: //method/text()
```
- 输入 Body:
  ```xml
  <request><method>getUserList</method></request>
  ```
- 输出: `getUserList`

**示例 2：提取属性值**
```
类型: BODY_XPATH
模式: //user/@id
```
- 输入 Body:
  ```xml
  <users><user id="12345" name="test"/></users>
  ```
- 输出: `12345`

**示例 3：提取特定位置的元素**
```
类型: BODY_XPATH
模式: //items/item[1]/name/text()
```
- 输入 Body:
  ```xml
  <items>
    <item><name>first</name></item>
    <item><name>second</name></item>
  </items>
  ```
- 输出: `first`

**示例 4：SOAP 请求示例**
```
类型: BODY_XPATH
模式: //soap:Body/*[1]/local-name()
```
- 提取 SOAP Body 中第一个子元素的标签名

**命名空间处理**：

对于带命名空间的 XML，使用前缀：
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getUserRequest>
      <id>123</id>
    </getUserRequest>
  </soap:Body>
</soap:Envelope>
```

XPath 表达式：
```
//soap:Body/*[1]/id/text()
```

## 优先级机制

### 优先级数值

- **数值范围**：1 - 999
- **数值越小，优先级越高**
- **相同优先级**：按创建时间排序（先创建的先执行）

### 优先级建议

| 优先级范围 | 用途建议 |
|-----------|---------|
| 1-10 | 项目特定的核心规则 |
| 11-50 | 通用的业务规则 |
| 51-100 | 通用的默认规则 |
| 100+ | 备用/兜底规则 |

### 优先级调整方法

1. **编辑规则**：直接修改优先级数值
2. **右键菜单**：使用 `上移` / `下移` 调整
3. **拖拽排序**：暂不支持

## 规则匹配流程

### 匹配顺序

```
请求到达
    ↓
按优先级排序所有启用的规则
    ↓
遍历每条规则
    ↓
规则匹配成功？
    ├─ 是 → 使用此规则生成标题 → 结束
    └─ 否 → 继续下一条规则
    ↓
所有规则都失败
    ↓
使用默认处理（路径后 16 字符）
```

### 规则匹配示例

假设有以下规则（按优先级排序）：

```
1. PATH_REGEX: /api/(v\d+)/.*     [优先级: 10]
2. BODY_JSON_PATH: $.action       [优先级: 20]
3. PATH_SUBSTRING: last16         [优先级: 100]
```

**场景 1：URL 匹配成功**
```
请求: POST /api/v2/users/create
Body: {"name": "test"}

匹配过程:
- 规则 1: 匹配成功，提取 "v2"
- 结果: 1-POST-v2
```

**场景 2：URL 不匹配，Body 匹配成功**
```
请求: POST /graphql
Body: {"action": "getUser", "query": "..."}

匹配过程:
- 规则 1: 不匹配 (/graphql 不符合 /api/v...)
- 规则 2: 匹配成功，提取 "getUser"
- 结果: 2-POST-getUser
```

**场景 3：都失败，使用默认**
```
请求: GET /some/random/path
Body: (empty)

匹配过程:
- 规则 1: 不匹配
- 规则 2: 跳过（Body 为空）
- 规则 3: 匹配成功，提取 "random/path"
- 结果: 3-GET-random/path
```

## 实战配置案例

### 案例 1：RESTful API 测试

**场景**：测试一个电商平台的 REST API

**规则配置**：

```yaml
规则 1:
  名称: API版本提取
  类型: PATH_REGEX
  模式: /api/(v\d+)/.*
  优先级: 5
  
规则 2:
  名称: 资源类型提取
  类型: PATH_REGEX
  模式: /api/v\d+/([a-zA-Z]+)
  优先级: 10
  
规则 3:
  名称: 默认路径
  类型: PATH_SUBSTRING
  模式: last16
  优先级: 100
```

**效果**：
- `/api/v1/products` → `1-GET-v1`（高优先级版本规则）
- `/api/v2/users` → `2-GET-v2`（同上）
- `/health/check` → `3-GET-ealth/check`（默认规则）

### 案例 2：微服务网关测试

**场景**：通过网关测试多个微服务

**规则配置**：

```yaml
规则 1:
  名称: 微服务名提取
  类型: PATH_REGEX
  模式: /([^/]+)/api/.*
  优先级: 5
  
规则 2:
  名称: 接口动作提取
  类型: BODY_JSON_PATH
  模式: $.action
  优先级: 10
```

**效果**：
- `/user-service/api/getInfo` → `1-GET-user-service`
- `/order-service/api/create` + Body `{"action": "createOrder"}` → `2-POST-createOrder`

### 案例 3：GraphQL API 测试

**场景**：测试 GraphQL 接口

**规则配置**：

```yaml
规则 1:
  名称: GraphQL操作名
  类型: BODY_REGEX
  模式: "operationName":\s*"([^"]+)"
  优先级: 1
```

**输入**：
```json
{
  "operationName": "GetUserProfile",
  "query": "query GetUserProfile..."
}
```

**效果**：`1-POST-GetUserProfile`

### 案例 4：SOAP Web Service 测试

**场景**：测试传统的 SOAP 接口

**规则配置**：

```yaml
规则 1:
  名称: SOAP方法名
  类型: BODY_XPATH
  模式: //soap:Body/*[1]/local-name()
  优先级: 1
```

**输入**：
```xml
<soap:Envelope>
  <soap:Body>
    <GetUserRequest>
      <id>123</id>
    </GetUserRequest>
  </soap:Body>
</soap:Envelope>
```

**效果**：`1-POST-GetUserRequest`

### 案例 5：文件上传接口测试

**场景**：测试文件上传 API

**规则配置**：

```yaml
规则 1:
  名称: 上传目标路径
  类型: PATH_REGEX
  模式: /upload/(\w+)
  优先级: 5
```

**效果**：
- `/upload/avatar` → `1-POST-avatar`
- `/upload/document` → `2-POST-document`

## 规则模板库

### 通用规则模板

```yaml
# 保存为 rules-template.yaml
rules:
  - id: "template-001"
    name: "API版本号"
    type: "PATH_REGEX"
    pattern: "/api/(v\\d+)/.*"
    priority: 10
    enabled: true
    description: "提取API版本号"

  - id: "template-002"
    name: "资源类型"
    type: "PATH_REGEX"
    pattern: "/api/v\\d+/([a-zA-Z_]+)"
    priority: 20
    enabled: true
    description: "提取资源类型名称"

  - id: "template-003"
    name: "JSON操作字段"
    type: "BODY_JSON_PATH"
    pattern: "$.action"
    priority: 30
    enabled: true
    description: "从JSON body提取action字段"

  - id: "template-004"
    name: "路径最后段"
    type: "PATH_REGEX"
    pattern: "/([^/]+)$"
    priority: 50
    enabled: true
    description: "提取路径最后一段"

  - id: "template-005"
    name: "GraphQL操作名"
    type: "BODY_REGEX"
    pattern: '"operationName":\\s*"([^"]+)"'
    priority: 5
    enabled: false
    description: "GraphQL接口专用"

  - id: "template-006"
    name: "SOAP方法名"
    type: "BODY_XPATH"
    pattern: "//soap:Body/*[1]/local-name()"
    priority: 5
    enabled: false
    description: "SOAP Web Service专用"

  - id: "template-007"
    name: "默认路径后16字符"
    type: "PATH_SUBSTRING"
    pattern: "last16"
    priority: 100
    enabled: true
    description: "默认兜底规则"
```

### 导入模板

1. 在"重命名规则"标签页点击 `导入规则`
2. 选择模板文件
3. 根据需要启用/禁用特定规则

### 自定义模板

你可以根据项目需求：
1. 修改现有规则的 pattern
2. 调整优先级顺序
3. 添加项目特定的规则
4. 分享给团队成员使用

---

## 配置建议

### 最佳实践

1. **从简单开始**：先使用 PATH_SUBSTRING 和简单的 PATH_REGEX
2. **测试优先**：在"规则测试"标签页充分测试新规则
3. **优先级合理**：给特定规则高优先级，通用规则低优先级
4. **规则命名清晰**：使用描述性名称，方便识别
5. **定期维护**：删除不再使用的规则，保持规则列表整洁

### 性能考虑

- 规则按优先级顺序执行，高优先级规则失败才会执行后续规则
- 复杂的正则表达式可能影响性能，尽量使用简单的模式
- JSONPath 和 XPath 需要解析整个 Body，对大包体可能较慢

### 故障排除

规则不生效时检查：
- ✅ 规则是否已启用
- ✅ 优先级是否设置正确
- ✅ 正则表达式语法是否正确
- ✅ 在"规则测试"中验证规则逻辑
- ✅ 查看 Burp Suite 的输出日志

---

**下一步**：查看 [高级功能](ADVANCED_FEATURES.md) 了解更多使用技巧。
