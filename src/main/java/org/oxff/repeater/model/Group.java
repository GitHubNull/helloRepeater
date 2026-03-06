package org.oxff.repeater.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分组实体类
 */
public class Group implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String parentId;  // 父分组ID，支持层级
    private String description;
    private long createdAt;
    private long updatedAt;
    private List<String> repeaterTabIds;  // 关联的Repeater标签页ID

    public Group() {
        this.repeaterTabIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Group(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getRepeaterTabIds() { return repeaterTabIds; }
    public void setRepeaterTabIds(List<String> repeaterTabIds) { this.repeaterTabIds = repeaterTabIds; }

    public void addRepeaterTabId(String tabId) {
        if (!repeaterTabIds.contains(tabId)) {
            repeaterTabIds.add(tabId);
        }
    }

    public void removeRepeaterTabId(String tabId) {
        repeaterTabIds.remove(tabId);
    }

    @Override
    public String toString() {
        return name;
    }
}