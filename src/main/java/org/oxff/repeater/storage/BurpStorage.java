package org.oxff.repeater.storage;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oxff.repeater.model.Group;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分组存储管理器 - 委托给SQLiteStorage，支持从Burp旧存储迁移
 */
public class BurpStorage {

    private static final String GROUPS_KEY = "repeater_groups";
    private static final String GROUP_DATA_KEY = "group_data_";
    private static final String MIGRATION_FLAG = "groups_migrated_to_sqlite";

    private final MontoyaApi api;
    private final SQLiteStorage sqliteStorage;
    private final PersistedObject persistedData;
    private final ObjectMapper objectMapper;
    private final Yaml yaml;

    public BurpStorage(MontoyaApi api, SQLiteStorage sqliteStorage) {
        this.api = api;
        this.sqliteStorage = sqliteStorage;
        this.persistedData = api.persistence().extensionData();
        this.objectMapper = new ObjectMapper();

        // 配置YAML
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        Representer representer = new Representer(dumperOptions);
        LoaderOptions loaderOptions = new LoaderOptions();
        this.yaml = new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions);

        // 执行数据迁移
        migrateFromBurpStorage();
    }

    /**
     * 从Burp旧存储迁移数据到SQLite
     */
    private void migrateFromBurpStorage() {
        // 检查是否已经迁移过
        String migrated = persistedData.getString(MIGRATION_FLAG);
        if ("true".equals(migrated)) {
            return;
        }

        try {
            api.logging().logToOutput("检查是否需要从Burp存储迁移分组数据...");

            List<Group> oldGroups = loadGroupsFromBurpStorage();
            if (!oldGroups.isEmpty()) {
                api.logging().logToOutput("发现 " + oldGroups.size() + " 个旧分组，开始迁移到SQLite...");

                for (Group group : oldGroups) {
                    sqliteStorage.saveGroup(group);
                }

                api.logging().logToOutput("分组数据迁移完成！");
            }

            // 标记已迁移
            persistedData.setString(MIGRATION_FLAG, "true");
        } catch (Exception e) {
            api.logging().logToError("迁移分组数据失败: " + e.getMessage());
        }
    }

    /**
     * 从Burp旧存储加载分组（用于迁移）
     */
    private List<Group> loadGroupsFromBurpStorage() {
        List<Group> groups = new ArrayList<>();

        try {
            String groupsJson = persistedData.getString(GROUPS_KEY);
            if (groupsJson == null || groupsJson.isEmpty()) {
                return groups;
            }

            List<String> groupIds = objectMapper.readValue(groupsJson, new TypeReference<List<String>>() {});

            for (String groupId : groupIds) {
                String json = persistedData.getString(GROUP_DATA_KEY + groupId);
                if (json != null && !json.isEmpty()) {
                    Group group = objectMapper.readValue(json, Group.class);
                    if (group != null) {
                        groups.add(group);
                    }
                }
            }

            // 清理旧数据
            cleanupOldBurpStorage(groupIds);

        } catch (JsonProcessingException e) {
            api.logging().logToError("从Burp存储加载分组失败: " + e.getMessage());
        }

        return groups;
    }

    /**
     * 清理Burp旧存储数据
     */
    private void cleanupOldBurpStorage(List<String> groupIds) {
        try {
            for (String groupId : groupIds) {
                persistedData.deleteString(GROUP_DATA_KEY + groupId);
            }
            persistedData.deleteString(GROUPS_KEY);
            api.logging().logToOutput("已清理Burp旧存储中的分组数据");
        } catch (Exception e) {
            api.logging().logToError("清理旧存储数据失败: " + e.getMessage());
        }
    }

    // ==================== 委托给SQLiteStorage的方法 ====================

    public void saveGroup(Group group) {
        try {
            sqliteStorage.saveGroup(group);
        } catch (SQLException e) {
            api.logging().logToError("保存分组失败: " + e.getMessage());
        }
    }

    public List<Group> loadAllGroups() {
        try {
            return sqliteStorage.loadAllGroups();
        } catch (SQLException e) {
            api.logging().logToError("加载分组失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Group loadGroup(String groupId) {
        try {
            return sqliteStorage.loadGroup(groupId);
        } catch (SQLException e) {
            api.logging().logToError("加载分组失败: " + e.getMessage());
            return null;
        }
    }

    public void deleteGroup(String groupId) {
        try {
            sqliteStorage.deleteGroup(groupId);
        } catch (SQLException e) {
            api.logging().logToError("删除分组失败: " + e.getMessage());
        }
    }

    public void updateGroup(Group group) {
        group.setUpdatedAt(System.currentTimeMillis());
        saveGroup(group);
    }

    // ==================== YAML导入导出 ====================

    /**
     * 导出所有分组为YAML格式
     */
    public String exportGroups() {
        List<Group> groups = loadAllGroups();

        try {
            return yaml.dump(groups);
        } catch (Exception e) {
            api.logging().logToError("导出分组到YAML失败: " + e.getMessage());
            return "groups: []";
        }
    }

    /**
     * 从YAML导入分组
     */
    @SuppressWarnings("unchecked")
    public void importGroups(String yamlContent) {
        try {
            Object data = yaml.load(yamlContent);

            if (data instanceof List) {
                // 直接是列表格式
                List<Map<String, Object>> groupMaps = (List<Map<String, Object>>) data;
                importFromMapList(groupMaps);
            } else if (data instanceof Map) {
                // 是Map格式，可能包含groups键
                Map<String, Object> rootMap = (Map<String, Object>) data;
                if (rootMap.containsKey("groups")) {
                    Object groupsObj = rootMap.get("groups");
                    if (groupsObj instanceof List) {
                        importFromMapList((List<Map<String, Object>>) groupsObj);
                    }
                }
            }

            api.logging().logToOutput("分组导入成功！");
        } catch (Exception e) {
            api.logging().logToError("从YAML导入分组失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void importFromMapList(List<Map<String, Object>> groupMaps) {
        for (Map<String, Object> map : groupMaps) {
            Group group = new Group();
            group.setId(getStringValue(map, "id"));
            group.setName(getStringValue(map, "name"));
            group.setParentId(getStringValue(map, "parentId"));
            group.setDescription(getStringValue(map, "description"));

            // 处理tabIds
            Object tabIdsObj = map.get("repeaterTabIds");
            if (tabIdsObj instanceof List) {
                List<String> tabIds = (List<String>) tabIdsObj;
                group.getRepeaterTabIds().addAll(tabIds);
            }

            // 处理时间戳
            group.setCreatedAt(getLongValue(map, "createdAt", System.currentTimeMillis()));
            group.setUpdatedAt(getLongValue(map, "updatedAt", System.currentTimeMillis()));

            saveGroup(group);
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private long getLongValue(Map<String, Object> map, String key, long defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }
}