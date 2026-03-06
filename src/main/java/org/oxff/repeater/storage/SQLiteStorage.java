package org.oxff.repeater.storage;

import org.oxff.repeater.model.Group;
import org.oxff.repeater.model.RenameRule;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SQLite存储 - 用于存储重命名规则
 */
public class SQLiteStorage {

    private static final String DB_NAME = "repeater_manager.db";
    private Connection connection;
    private final String dbPath;

    public SQLiteStorage(String burpDir) {
        this.dbPath = burpDir + "/" + DB_NAME;
        initDatabase();
    }

    private void initDatabase() {
        try {
            // 显式加载 SQLite JDBC 驱动（在 fat JAR 中需要手动注册）
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTables();
            insertDefaultRules();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC 驱动未找到", e);
        } catch (SQLException e) {
            throw new RuntimeException("初始化SQLite数据库失败", e);
        }
    }

    private void createTables() throws SQLException {
        String createRulesTable = "CREATE TABLE IF NOT EXISTS rename_rules (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "pattern TEXT," +
                "priority INTEGER DEFAULT 100," +
                "enabled INTEGER DEFAULT 1," +
                "description TEXT," +
                "path_start_index INTEGER DEFAULT -1," +
                "path_end_index INTEGER DEFAULT -1," +
                "created_at INTEGER," +
                "updated_at INTEGER" +
                ")";

        String createGroupsTable = "CREATE TABLE IF NOT EXISTS groups (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "parent_id TEXT," +
                "description TEXT," +
                "tab_ids TEXT," +
                "created_at INTEGER," +
                "updated_at INTEGER" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createRulesTable);
            stmt.execute(createGroupsTable);
        }
    }

    private void insertDefaultRules() throws SQLException {
        // 检查是否已有规则
        String checkSql = "SELECT COUNT(*) FROM rename_rules";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // 已有规则，不插入默认规则
            }
        }

        List<RenameRule> defaultRules = createDefaultRules();
        for (RenameRule rule : defaultRules) {
            saveRule(rule);
        }
    }

    private List<RenameRule> createDefaultRules() {
        List<RenameRule> rules = new ArrayList<>();

        // 默认规则1: 路径最后16个字符
        RenameRule rule1 = new RenameRule();
        rule1.setId("default_path_last_16");
        rule1.setName("路径后16字符");
        rule1.setType(RenameRule.RuleType.PATH_SUBSTRING);
        rule1.setPattern("last16");
        rule1.setPriority(1);
        rule1.setDescription("默认规则：取路径最后16个字符");
        rules.add(rule1);

        // 默认规则2: 提取API版本号
        RenameRule rule2 = new RenameRule();
        rule2.setId("default_api_version");
        rule2.setName("API版本提取");
        rule2.setType(RenameRule.RuleType.PATH_REGEX);
        rule2.setPattern("/api/(v\\d+)/.*");
        rule2.setPriority(2);
        rule2.setDescription("从路径提取API版本");
        rules.add(rule2);

        // 默认规则3: JSON body中的action字段
        RenameRule rule3 = new RenameRule();
        rule3.setId("default_json_action");
        rule3.setName("JSON Action字段");
        rule3.setType(RenameRule.RuleType.BODY_JSON_PATH);
        rule3.setPattern("$.action");
        rule3.setPriority(3);
        rule3.setDescription("从JSON body提取action字段");
        rules.add(rule3);

        // 默认规则4: XML body中的method字段
        RenameRule rule4 = new RenameRule();
        rule4.setId("default_xml_method");
        rule4.setName("XML Method字段");
        rule4.setType(RenameRule.RuleType.BODY_XPATH);
        rule4.setPattern("//method/text()");
        rule4.setPriority(4);
        rule4.setDescription("从XML body提取method字段");
        rules.add(rule4);

        // 默认规则5: Body中的接口名称（通用正则）
        RenameRule rule5 = new RenameRule();
        rule5.setId("default_body_interface");
        rule5.setName("Body接口名提取");
        rule5.setType(RenameRule.RuleType.BODY_REGEX);
        rule5.setPattern("interface\\s*[=:]\\s*['\"]([^'\"]+)['\"]");
        rule5.setPriority(5);
        rule5.setDescription("从Body提取interface字段");
        rules.add(rule5);

        return rules;
    }

    public void saveRule(RenameRule rule) throws SQLException {
        String sql = "INSERT OR REPLACE INTO rename_rules " +
                "(id, name, type, pattern, priority, enabled, description, " +
                "path_start_index, path_end_index, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, rule.getId());
            stmt.setString(2, rule.getName());
            stmt.setString(3, rule.getType().name());
            stmt.setString(4, rule.getPattern());
            stmt.setInt(5, rule.getPriority());
            stmt.setInt(6, rule.isEnabled() ? 1 : 0);
            stmt.setString(7, rule.getDescription());
            stmt.setInt(8, rule.getPathStartIndex());
            stmt.setInt(9, rule.getPathEndIndex());
            stmt.setLong(10, rule.getCreatedAt());
            stmt.setLong(11, rule.getUpdatedAt());
            stmt.executeUpdate();
        }
    }

    public List<RenameRule> loadAllRules() throws SQLException {
        List<RenameRule> rules = new ArrayList<>();
        String sql = "SELECT * FROM rename_rules ORDER BY priority ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rules.add(mapResultSetToRule(rs));
            }
        }

        return rules;
    }

    public List<RenameRule> loadEnabledRules() throws SQLException {
        List<RenameRule> rules = new ArrayList<>();
        String sql = "SELECT * FROM rename_rules WHERE enabled = 1 ORDER BY priority ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rules.add(mapResultSetToRule(rs));
            }
        }

        return rules;
    }

    public RenameRule loadRule(String id) throws SQLException {
        String sql = "SELECT * FROM rename_rules WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRule(rs);
                }
            }
        }
        return null;
    }

    public void deleteRule(String id) throws SQLException {
        String sql = "DELETE FROM rename_rules WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    private RenameRule mapResultSetToRule(ResultSet rs) throws SQLException {
        RenameRule rule = new RenameRule();
        rule.setId(rs.getString("id"));
        rule.setName(rs.getString("name"));
        rule.setType(RenameRule.RuleType.valueOf(rs.getString("type")));
        rule.setPattern(rs.getString("pattern"));
        rule.setPriority(rs.getInt("priority"));
        rule.setEnabled(rs.getInt("enabled") == 1);
        rule.setDescription(rs.getString("description"));
        rule.setPathStartIndex(rs.getInt("path_start_index"));
        rule.setPathEndIndex(rs.getInt("path_end_index"));
        rule.setCreatedAt(rs.getLong("created_at"));
        rule.setUpdatedAt(rs.getLong("updated_at"));
        return rule;
    }

    // ==================== 分组操作方法 ====================

    public void saveGroup(Group group) throws SQLException {
        String sql = "INSERT OR REPLACE INTO groups " +
                "(id, name, parent_id, description, tab_ids, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, group.getId());
            stmt.setString(2, group.getName());
            stmt.setString(3, group.getParentId());
            stmt.setString(4, group.getDescription());
            stmt.setString(5, String.join(",", group.getRepeaterTabIds()));
            stmt.setLong(6, group.getCreatedAt());
            stmt.setLong(7, group.getUpdatedAt());
            stmt.executeUpdate();
        }
    }

    public List<Group> loadAllGroups() throws SQLException {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups ORDER BY created_at ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        }

        return groups;
    }

    public Group loadGroup(String id) throws SQLException {
        String sql = "SELECT * FROM groups WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGroup(rs);
                }
            }
        }
        return null;
    }

    public void deleteGroup(String id) throws SQLException {
        String sql = "DELETE FROM groups WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void updateGroup(Group group) throws SQLException {
        group.setUpdatedAt(System.currentTimeMillis());
        saveGroup(group);
    }

    private Group mapResultSetToGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getString("id"));
        group.setName(rs.getString("name"));
        group.setParentId(rs.getString("parent_id"));
        group.setDescription(rs.getString("description"));

        String tabIdsStr = rs.getString("tab_ids");
        if (tabIdsStr != null && !tabIdsStr.isEmpty()) {
            group.getRepeaterTabIds().addAll(Arrays.asList(tabIdsStr.split(",")));
        }

        group.setCreatedAt(rs.getLong("created_at"));
        group.setUpdatedAt(rs.getLong("updated_at"));
        return group;
    }

    // ==================== 批量操作 ====================

    public void saveGroups(List<Group> groups) throws SQLException {
        connection.setAutoCommit(false);
        try {
            for (Group group : groups) {
                saveGroup(group);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void saveRules(List<RenameRule> rules) throws SQLException {
        connection.setAutoCommit(false);
        try {
            for (RenameRule rule : rules) {
                saveRule(rule);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ==================== 规则导出导入方法 ====================

    public String exportRulesToYaml() throws SQLException {
        List<RenameRule> rules = loadAllRules();
        StringBuilder yaml = new StringBuilder();
        yaml.append("rules:\n");
        
        for (RenameRule rule : rules) {
            yaml.append("  - id: \"").append(escapeYaml(rule.getId())).append("\"\n");
            yaml.append("    name: \"").append(escapeYaml(rule.getName())).append("\"\n");
            yaml.append("    type: \"").append(rule.getType().name()).append("\"\n");
            yaml.append("    pattern: \"").append(escapeYaml(rule.getPattern())).append("\"\n");
            yaml.append("    priority: ").append(rule.getPriority()).append("\n");
            yaml.append("    enabled: ").append(rule.isEnabled()).append("\n");
            yaml.append("    description: \"").append(escapeYaml(rule.getDescription())).append("\"\n");
            yaml.append("    pathStartIndex: ").append(rule.getPathStartIndex()).append("\n");
            yaml.append("    pathEndIndex: ").append(rule.getPathEndIndex()).append("\n");
        }
        
        return yaml.toString();
    }

    @SuppressWarnings("unchecked")
    public int importRulesFromYaml(String yamlContent) throws SQLException {
        List<String> existingIds = new ArrayList<>();
        for (RenameRule rule : loadAllRules()) {
            existingIds.add(rule.getId());
        }
        
        int importedCount = 0;
        try {
            org.yaml.snakeyaml.Yaml yamlParser = new org.yaml.snakeyaml.Yaml();
            Map<String, Object> data = yamlParser.load(yamlContent);
            
            if (data == null || !data.containsKey("rules")) {
                return 0;
            }
            
            List<Map<String, Object>> rulesList = (List<Map<String, Object>>) data.get("rules");
            for (Map<String, Object> ruleMap : rulesList) {
                String id = getStringValue(ruleMap, "id");
                
                // 如果规则已存在，跳过
                if (existingIds.contains(id)) {
                    continue;
                }
                
                RenameRule rule = new RenameRule();
                rule.setId(id);
                rule.setName(getStringValue(ruleMap, "name"));
                rule.setType(RenameRule.RuleType.valueOf(getStringValue(ruleMap, "type")));
                rule.setPattern(getStringValue(ruleMap, "pattern"));
                rule.setPriority(getIntValue(ruleMap, "priority", 100));
                rule.setEnabled(getBooleanValue(ruleMap, "enabled", true));
                rule.setDescription(getStringValue(ruleMap, "description"));
                rule.setPathStartIndex(getIntValue(ruleMap, "pathStartIndex", -1));
                rule.setPathEndIndex(getIntValue(ruleMap, "pathEndIndex", -1));
                rule.setCreatedAt(System.currentTimeMillis());
                rule.setUpdatedAt(System.currentTimeMillis());
                
                saveRule(rule);
                importedCount++;
            }
        } catch (Exception e) {
            throw new SQLException("导入规则失败: " + e.getMessage(), e);
        }
        
        return importedCount;
    }

    private String escapeYaml(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private boolean getBooleanValue(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
}