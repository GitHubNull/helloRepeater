package org.oxff.repeater.model;

import java.io.Serializable;

/**
 * 重命名规则实体类
 */
public class RenameRule implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RuleType {
        PATH_SUBSTRING,      // 路径截取
        PATH_REGEX,          // 路径正则匹配
        BODY_REGEX,          // Body正则匹配
        BODY_JSON_PATH,      // Body JSON Path提取
        BODY_XPATH           // Body XPath提取
    }

    private String id;
    private String name;           // 规则名称
    private RuleType type;         // 规则类型
    private String pattern;        // 正则模式/JSON Path/XPath/截取范围
    private int priority;          // 优先级（数字越小优先级越高）
    private boolean enabled;       // 是否启用
    private String description;    // 描述
    private long createdAt;
    private long updatedAt;

    // 路径截取专用
    private int pathStartIndex = -1;   // 开始索引
    private int pathEndIndex = -1;     // 结束索引（-1表示到最后）

    public RenameRule() {
        this.enabled = true;
        this.priority = 100;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RuleType getType() { return type; }
    public void setType(RuleType type) { this.type = type; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getPathStartIndex() { return pathStartIndex; }
    public void setPathStartIndex(int pathStartIndex) { this.pathStartIndex = pathStartIndex; }

    public int getPathEndIndex() { return pathEndIndex; }
    public void setPathEndIndex(int pathEndIndex) { this.pathEndIndex = pathEndIndex; }

    @Override
    public String toString() {
        return name + " [" + type + "]";
    }
}