package org.oxff.repeater.config;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.EditorOptions;
import org.oxff.repeater.model.Group;
import org.oxff.repeater.model.RenameRule;
import org.oxff.repeater.rename.RepeaterRenameHandler;
import org.oxff.repeater.storage.BurpStorage;
import org.oxff.repeater.storage.SQLiteStorage;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 配置Tab界面
 */
public class ConfigTab {

    private final MontoyaApi api;
    private final SQLiteStorage sqliteStorage;
    private final BurpStorage burpStorage;
    private final RepeaterRenameHandler renameHandler;

    private JTabbedPane mainPanel;
    private JTable ruleTable;
    private DefaultTableModel ruleTableModel;
    private JTable groupTable;
    private DefaultTableModel groupTableModel;
    private JTextArea testResultArea;
    
    // 搜索相关成员变量
    private JTextField ruleSearchField;
    private JComboBox<String> ruleSearchModeCombo;
    private JCheckBox ruleCaseInsensitiveCheck;
    private JTextField groupSearchField;
    private JComboBox<String> groupSearchModeCombo;
    private JCheckBox groupCaseInsensitiveCheck;
    
    // 原始数据缓存（用于搜索过滤）
    private List<RenameRule> allRules = new ArrayList<>();
    private List<Group> allGroups = new ArrayList<>();

    public ConfigTab(MontoyaApi api, SQLiteStorage sqliteStorage, BurpStorage burpStorage, RepeaterRenameHandler renameHandler) {
        this.api = api;
        this.sqliteStorage = sqliteStorage;
        this.burpStorage = burpStorage;
        this.renameHandler = renameHandler;
        initUI();
    }

    public Component getUI() {
        return mainPanel;
    }

    private void initUI() {
        mainPanel = new JTabbedPane();

        // 规则管理Tab
        mainPanel.addTab("重命名规则", createRulePanel());

        // 分组管理Tab
        mainPanel.addTab("分组管理", createGroupPanel());

        // 测试Tab
        mainPanel.addTab("规则测试", createTestPanel());

        // 加载数据
        loadRules();
        loadGroups();
    }

    private JPanel createRulePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索过滤"));
        
        ruleSearchField = new JTextField(20);
        ruleSearchField.setToolTipText("输入关键词进行搜索");
        searchPanel.add(new JLabel("关键词:"));
        searchPanel.add(ruleSearchField);
        
        ruleSearchModeCombo = new JComboBox<>(new String[]{"简单匹配", "正则匹配"});
        searchPanel.add(new JLabel("模式:"));
        searchPanel.add(ruleSearchModeCombo);
        
        ruleCaseInsensitiveCheck = new JCheckBox("忽略大小写", true);
        searchPanel.add(ruleCaseInsensitiveCheck);
        
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> filterRules());
        searchPanel.add(searchBtn);
        
        JButton clearBtn = new JButton("清除");
        clearBtn.addActionListener(e -> {
            ruleSearchField.setText("");
            filterRules();
        });
        searchPanel.add(clearBtn);
        
        panel.add(searchPanel, BorderLayout.NORTH);

        // 工具栏
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton addBtn = new JButton("添加规则");
        addBtn.addActionListener(e -> showAddRuleDialog());
        toolbar.add(addBtn);

        JButton editBtn = new JButton("编辑规则");
        editBtn.addActionListener(e -> showEditRuleDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = new JButton("删除规则");
        deleteBtn.addActionListener(e -> deleteSelectedRule());
        toolbar.add(deleteBtn);

        toolbar.addSeparator();

        JButton importBtn = new JButton("导入规则");
        importBtn.addActionListener(e -> importRules());
        toolbar.add(importBtn);

        JButton exportBtn = new JButton("导出规则");
        exportBtn.addActionListener(e -> exportRules());
        toolbar.add(exportBtn);

        // 将工具栏添加到搜索面板下方
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(toolbar, BorderLayout.SOUTH);
        panel.add(northPanel, BorderLayout.NORTH);

        // 规则表格
        String[] columns = {"启用", "优先级", "名称", "类型", "模式", "描述"};
        ruleTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Boolean.class;
                if (column == 1) return Integer.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 只有启用列可编辑
            }
        };

        ruleTable = new JTable(ruleTableModel);
        ruleTable.getColumnModel().getColumn(0).setMaxWidth(50);
        ruleTable.getColumnModel().getColumn(1).setMaxWidth(60);
        
        // 添加双击事件
        ruleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditRuleDialog();
                }
            }
        });
        
        // 添加右键菜单
        setupRuleTableContextMenu();

        JScrollPane scrollPane = new JScrollPane(ruleTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 启用状态变更监听器
        ruleTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                int row = e.getFirstRow();
                updateRuleEnabledState(row);
            }
        });

        return panel;
    }

    private JPanel createGroupPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索过滤"));
        
        groupSearchField = new JTextField(20);
        groupSearchField.setToolTipText("输入关键词进行搜索");
        searchPanel.add(new JLabel("关键词:"));
        searchPanel.add(groupSearchField);
        
        groupSearchModeCombo = new JComboBox<>(new String[]{"简单匹配", "正则匹配"});
        searchPanel.add(new JLabel("模式:"));
        searchPanel.add(groupSearchModeCombo);
        
        groupCaseInsensitiveCheck = new JCheckBox("忽略大小写", true);
        searchPanel.add(groupCaseInsensitiveCheck);
        
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> filterGroups());
        searchPanel.add(searchBtn);
        
        JButton clearBtn = new JButton("清除");
        clearBtn.addActionListener(e -> {
            groupSearchField.setText("");
            filterGroups();
        });
        searchPanel.add(clearBtn);

        // 工具栏
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton addBtn = new JButton("添加分组");
        addBtn.addActionListener(e -> showAddGroupDialog());
        toolbar.add(addBtn);

        JButton editBtn = new JButton("编辑分组");
        editBtn.addActionListener(e -> showEditGroupDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = new JButton("删除分组");
        deleteBtn.addActionListener(e -> deleteSelectedGroup());
        toolbar.add(deleteBtn);

        toolbar.addSeparator();

        JButton importBtn = new JButton("导入分组");
        importBtn.addActionListener(e -> importGroups());
        toolbar.add(importBtn);

        JButton exportBtn = new JButton("导出分组");
        exportBtn.addActionListener(e -> exportGroups());
        toolbar.add(exportBtn);

        // 将工具栏添加到搜索面板下方
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(toolbar, BorderLayout.SOUTH);
        panel.add(northPanel, BorderLayout.NORTH);

        // 分组表格
        String[] columns = {"名称", "父分组", "描述", "标签页数量"};
        groupTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑，双击触发弹窗
            }
        };
        groupTable = new JTable(groupTableModel);
        
        // 添加双击事件
        groupTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditGroupDialog();
                }
            }
        });
        
        // 添加右键菜单
        setupGroupTableContextMenu();

        JScrollPane scrollPane = new JScrollPane(groupTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 测试输入区
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new TitledBorder("请求数据"));

        JTextArea requestArea = new JTextArea(15, 60);
        requestArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        requestArea.setText("GET /api/v1/users/list HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Type: application/json\n\n" +
                "{\"action\": \"getUserList\", \"id\": 123}");
        inputPanel.add(new JScrollPane(requestArea), BorderLayout.CENTER);

        // 测试按钮
        JButton testBtn = new JButton("测试规则");
        testBtn.addActionListener(e -> {
            testRule(requestArea.getText());
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(testBtn);
        inputPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);

        // 测试结果区
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("测试结果"));

        testResultArea = new JTextArea(10, 60);
        testResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        testResultArea.setEditable(false);
        resultPanel.add(new JScrollPane(testResultArea), BorderLayout.CENTER);

        panel.add(resultPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadRules() {
        try {
            allRules = sqliteStorage.loadAllRules();
            displayRules(allRules);
        } catch (SQLException e) {
            api.logging().logToError("加载规则失败: " + e.getMessage());
        }
    }

    private void loadGroups() {
        allGroups = burpStorage.loadAllGroups();
        displayGroups(allGroups);
    }

    private void showAddRuleDialog() {
        RuleDialog dialog = new RuleDialog(null, api, sqliteStorage, () -> loadRules());
        dialog.setVisible(true);
    }

    private void showEditRuleDialog() {
        int row = ruleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请先选择一个规则");
            return;
        }

        try {
            String ruleName = (String) ruleTableModel.getValueAt(row, 2);
            // 需要通过名称查找规则ID
            List<RenameRule> rules = sqliteStorage.loadAllRules();
            for (RenameRule rule : rules) {
                if (rule.getName().equals(ruleName)) {
                    RuleDialog dialog = new RuleDialog(rule, api, sqliteStorage, () -> loadRules());
                    dialog.setVisible(true);
                    break;
                }
            }
        } catch (SQLException e) {
            api.logging().logToError("加载规则失败: " + e.getMessage());
        }
    }

    private void deleteSelectedRule() {
        int row = ruleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请先选择一个规则");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            mainPanel,
            "确定要删除这个规则吗？",
            "确认删除",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String ruleName = (String) ruleTableModel.getValueAt(row, 2);
                List<RenameRule> rules = sqliteStorage.loadAllRules();
                for (RenameRule rule : rules) {
                    if (rule.getName().equals(ruleName)) {
                        sqliteStorage.deleteRule(rule.getId());
                        loadRules();
                        break;
                    }
                }
            } catch (SQLException e) {
                api.logging().logToError("删除规则失败: " + e.getMessage());
            }
        }
    }

    private void updateRuleEnabledState(int row) {
        try {
            String ruleName = (String) ruleTableModel.getValueAt(row, 2);
            Boolean enabled = (Boolean) ruleTableModel.getValueAt(row, 0);

            List<RenameRule> rules = sqliteStorage.loadAllRules();
            for (RenameRule rule : rules) {
                if (rule.getName().equals(ruleName)) {
                    rule.setEnabled(enabled);
                    sqliteStorage.saveRule(rule);
                    break;
                }
            }
        } catch (SQLException e) {
            api.logging().logToError("更新规则状态失败: " + e.getMessage());
        }
    }

    private void showAddGroupDialog() {
        GroupDialog dialog = new GroupDialog(null, api, burpStorage, allGroups, () -> loadGroups());
        dialog.setVisible(true);
    }

    private void showEditGroupDialog() {
        int row = groupTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请先选择一个分组");
            return;
        }

        String groupName = (String) groupTableModel.getValueAt(row, 0);
        // 查找对应的分组对象
        Group selectedGroup = null;
        for (Group group : allGroups) {
            if (group.getName().equals(groupName)) {
                selectedGroup = group;
                break;
            }
        }

        if (selectedGroup != null) {
            GroupDialog dialog = new GroupDialog(selectedGroup, api, burpStorage, allGroups, () -> loadGroups());
            dialog.setVisible(true);
        }
    }

    private void deleteSelectedGroup() {
        int row = groupTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请先选择一个分组");
            return;
        }

        // 获取选中分组的名称
        String groupName = (String) groupTableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
            mainPanel,
            "确定要删除分组 \"" + groupName + "\" 吗？",
            "确认删除",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 根据名称查找分组ID并删除
                List<Group> groups = burpStorage.loadAllGroups();
                for (Group group : groups) {
                    if (group.getName().equals(groupName)) {
                        burpStorage.deleteGroup(group.getId());
                        break;
                    }
                }
                loadGroups();
                JOptionPane.showMessageDialog(mainPanel, "分组删除成功！");
            } catch (Exception e) {
                api.logging().logToError("删除分组失败: " + e.getMessage());
                JOptionPane.showMessageDialog(mainPanel, 
                    "删除失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void testRule(String requestText) {
        // 解析请求文本并测试规则
        testResultArea.setText("测试请求:\n" + requestText + "\n\n");
        testResultArea.append("规则测试结果:\n");
        testResultArea.append("1. 路径后16字符: ..." + 
            requestText.substring(Math.max(0, requestText.indexOf(" ") + 1), 
                Math.min(requestText.length(), requestText.indexOf(" ") + 17)) + "\n");
        testResultArea.append("2. 其他规则测试...\n");
    }

    private void importRules() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导入规则");
        
        // 添加文件过滤器
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".yaml") || f.getName().toLowerCase().endsWith(".yml");
            }
            public String getDescription() {
                return "YAML 文件 (*.yaml, *.yml)";
            }
        });
        
        if (chooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                String yamlContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                int importedCount = sqliteStorage.importRulesFromYaml(yamlContent);
                JOptionPane.showMessageDialog(mainPanel, 
                    "规则导入成功！\n共导入 " + importedCount + " 条规则");
                loadRules();
            } catch (Exception e) {
                api.logging().logToError("导入规则失败: " + e.getMessage());
                JOptionPane.showMessageDialog(mainPanel, 
                    "导入失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportRules() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出规则");
        
        // 添加文件过滤器
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".yaml") || f.getName().toLowerCase().endsWith(".yml");
            }
            public String getDescription() {
                return "YAML 文件 (*.yaml, *.yml)";
            }
        });
        
        if (chooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // 确保文件有.yaml后缀
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".yaml") && !filePath.toLowerCase().endsWith(".yml")) {
                filePath += ".yaml";
                file = new File(filePath);
            }
            
            try {
                String yamlContent = sqliteStorage.exportRulesToYaml();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(yamlContent);
                }
                JOptionPane.showMessageDialog(mainPanel, 
                    "规则导出成功到:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                api.logging().logToError("导出规则失败: " + e.getMessage());
                JOptionPane.showMessageDialog(mainPanel, 
                    "导出失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importGroups() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导入分组");
        if (chooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            burpStorage.importGroups("");
            JOptionPane.showMessageDialog(mainPanel, "分组导入成功！");
            loadGroups();
        }
    }

    private void exportGroups() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出分组");
        if (chooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String yaml = burpStorage.exportGroups();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(yaml);
                JOptionPane.showMessageDialog(mainPanel, "分组导出成功到:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainPanel, "导出失败: " + e.getMessage());
            }
        }
    }

    // ==================== 右键菜单设置 ====================

    private void setupRuleTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("编辑");
        editItem.addActionListener(e -> showEditRuleDialog());
        popupMenu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(e -> deleteSelectedRule());
        popupMenu.add(deleteItem);

        popupMenu.addSeparator();

        JMenuItem moveUpItem = new JMenuItem("上移");
        moveUpItem.addActionListener(e -> moveRuleUp());
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = new JMenuItem("下移");
        moveDownItem.addActionListener(e -> moveRuleDown());
        popupMenu.add(moveDownItem);

        ruleTable.setComponentPopupMenu(popupMenu);
    }

    private void setupGroupTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("编辑");
        editItem.addActionListener(e -> showEditGroupDialog());
        popupMenu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(e -> deleteSelectedGroup());
        popupMenu.add(deleteItem);

        popupMenu.addSeparator();

        JMenuItem moveUpItem = new JMenuItem("上移");
        moveUpItem.addActionListener(e -> moveGroupUp());
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = new JMenuItem("下移");
        moveDownItem.addActionListener(e -> moveGroupDown());
        popupMenu.add(moveDownItem);

        groupTable.setComponentPopupMenu(popupMenu);
    }

    // ==================== 搜索过滤功能 ====================

    private void filterRules() {
        String keyword = ruleSearchField.getText().trim();
        String mode = (String) ruleSearchModeCombo.getSelectedItem();
        boolean ignoreCase = ruleCaseInsensitiveCheck.isSelected();

        if (keyword.isEmpty()) {
            // 如果关键词为空，显示所有数据
            displayRules(allRules);
            return;
        }

        List<RenameRule> filtered = new ArrayList<>();

        for (RenameRule rule : allRules) {
            boolean matches = false;

            if ("简单匹配".equals(mode)) {
                matches = simpleMatch(rule, keyword, ignoreCase);
            } else if ("正则匹配".equals(mode)) {
                matches = regexMatch(rule, keyword, ignoreCase);
            }

            if (matches) {
                filtered.add(rule);
            }
        }

        displayRules(filtered);
    }

    private void filterGroups() {
        String keyword = groupSearchField.getText().trim();
        String mode = (String) groupSearchModeCombo.getSelectedItem();
        boolean ignoreCase = groupCaseInsensitiveCheck.isSelected();

        if (keyword.isEmpty()) {
            displayGroups(allGroups);
            return;
        }

        List<Group> filtered = new ArrayList<>();

        for (Group group : allGroups) {
            boolean matches = false;

            if ("简单匹配".equals(mode)) {
                matches = simpleMatchGroup(group, keyword, ignoreCase);
            } else if ("正则匹配".equals(mode)) {
                matches = regexMatchGroup(group, keyword, ignoreCase);
            }

            if (matches) {
                filtered.add(group);
            }
        }

        displayGroups(filtered);
    }

    private boolean simpleMatch(RenameRule rule, String keyword, boolean ignoreCase) {
        String searchText = (rule.getName() + " " + rule.getType() + " " + rule.getPattern() + " " + rule.getDescription());
        if (ignoreCase) {
            return searchText.toLowerCase().contains(keyword.toLowerCase());
        }
        return searchText.contains(keyword);
    }

    private boolean regexMatch(RenameRule rule, String pattern, boolean ignoreCase) {
        try {
            String searchText = (rule.getName() + " " + rule.getType() + " " + rule.getPattern() + " " + rule.getDescription());
            int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
            Pattern p = Pattern.compile(pattern, flags);
            return p.matcher(searchText).find();
        } catch (PatternSyntaxException e) {
            // 如果正则表达式无效，回退到简单匹配
            return simpleMatch(rule, pattern, ignoreCase);
        }
    }

    private boolean simpleMatchGroup(Group group, String keyword, boolean ignoreCase) {
        String searchText = (group.getName() + " " + group.getDescription() + " " + group.getParentId());
        if (ignoreCase) {
            return searchText.toLowerCase().contains(keyword.toLowerCase());
        }
        return searchText.contains(keyword);
    }

    private boolean regexMatchGroup(Group group, String pattern, boolean ignoreCase) {
        try {
            String searchText = (group.getName() + " " + group.getDescription() + " " + group.getParentId());
            int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
            Pattern p = Pattern.compile(pattern, flags);
            return p.matcher(searchText).find();
        } catch (PatternSyntaxException e) {
            return simpleMatchGroup(group, pattern, ignoreCase);
        }
    }

    // ==================== 上下移动功能 ====================

    private void moveRuleUp() {
        int row = ruleTable.getSelectedRow();
        if (row <= 0) {
            return;
        }

        try {
            String ruleName = (String) ruleTableModel.getValueAt(row, 2);
            List<RenameRule> rules = sqliteStorage.loadAllRules();

            RenameRule currentRule = null;
            RenameRule prevRule = null;

            for (int i = 0; i < rules.size(); i++) {
                if (rules.get(i).getName().equals(ruleName)) {
                    currentRule = rules.get(i);
                    if (i > 0) {
                        prevRule = rules.get(i - 1);
                    }
                    break;
                }
            }

            if (currentRule != null && prevRule != null) {
                // 交换优先级
                int tempPriority = currentRule.getPriority();
                currentRule.setPriority(prevRule.getPriority());
                prevRule.setPriority(tempPriority);

                sqliteStorage.saveRule(currentRule);
                sqliteStorage.saveRule(prevRule);

                loadRules();
                // 保持选中状态
                ruleTable.setRowSelectionInterval(row - 1, row - 1);
            }
        } catch (SQLException e) {
            api.logging().logToError("移动规则失败: " + e.getMessage());
        }
    }

    private void moveRuleDown() {
        int row = ruleTable.getSelectedRow();
        if (row < 0 || row >= ruleTableModel.getRowCount() - 1) {
            return;
        }

        try {
            String ruleName = (String) ruleTableModel.getValueAt(row, 2);
            List<RenameRule> rules = sqliteStorage.loadAllRules();

            RenameRule currentRule = null;
            RenameRule nextRule = null;

            for (int i = 0; i < rules.size(); i++) {
                if (rules.get(i).getName().equals(ruleName)) {
                    currentRule = rules.get(i);
                    if (i < rules.size() - 1) {
                        nextRule = rules.get(i + 1);
                    }
                    break;
                }
            }

            if (currentRule != null && nextRule != null) {
                // 交换优先级
                int tempPriority = currentRule.getPriority();
                currentRule.setPriority(nextRule.getPriority());
                nextRule.setPriority(tempPriority);

                sqliteStorage.saveRule(currentRule);
                sqliteStorage.saveRule(nextRule);

                loadRules();
                // 保持选中状态
                ruleTable.setRowSelectionInterval(row + 1, row + 1);
            }
        } catch (SQLException e) {
            api.logging().logToError("移动规则失败: " + e.getMessage());
        }
    }

    private void moveGroupUp() {
        int row = groupTable.getSelectedRow();
        if (row <= 0) {
            return;
        }

        String groupName = (String) groupTableModel.getValueAt(row, 0);
        List<Group> groups = burpStorage.loadAllGroups();

        Group currentGroup = null;
        Group prevGroup = null;

        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getName().equals(groupName)) {
                currentGroup = groups.get(i);
                if (i > 0) {
                    prevGroup = groups.get(i - 1);
                }
                break;
            }
        }

        if (currentGroup != null && prevGroup != null) {
            // 使用创建时间作为排序依据，交换创建时间
            long tempTime = currentGroup.getCreatedAt();
            currentGroup.setCreatedAt(prevGroup.getCreatedAt());
            prevGroup.setCreatedAt(tempTime);

            burpStorage.updateGroup(currentGroup);
            burpStorage.updateGroup(prevGroup);

            loadGroups();
            groupTable.setRowSelectionInterval(row - 1, row - 1);
        }
    }

    private void moveGroupDown() {
        int row = groupTable.getSelectedRow();
        if (row < 0 || row >= groupTableModel.getRowCount() - 1) {
            return;
        }

        String groupName = (String) groupTableModel.getValueAt(row, 0);
        List<Group> groups = burpStorage.loadAllGroups();

        Group currentGroup = null;
        Group nextGroup = null;

        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getName().equals(groupName)) {
                currentGroup = groups.get(i);
                if (i < groups.size() - 1) {
                    nextGroup = groups.get(i + 1);
                }
                break;
            }
        }

        if (currentGroup != null && nextGroup != null) {
            // 交换创建时间
            long tempTime = currentGroup.getCreatedAt();
            currentGroup.setCreatedAt(nextGroup.getCreatedAt());
            nextGroup.setCreatedAt(tempTime);

            burpStorage.updateGroup(currentGroup);
            burpStorage.updateGroup(nextGroup);

            loadGroups();
            groupTable.setRowSelectionInterval(row + 1, row + 1);
        }
    }

    // ==================== 显示数据方法 ====================

    private void displayRules(List<RenameRule> rules) {
        ruleTableModel.setRowCount(0);
        for (RenameRule rule : rules) {
            Vector<Object> row = new Vector<>();
            row.add(rule.isEnabled());
            row.add(rule.getPriority());
            row.add(rule.getName());
            row.add(rule.getType());
            row.add(rule.getPattern());
            row.add(rule.getDescription());
            ruleTableModel.addRow(row);
        }
    }

    private void displayGroups(List<Group> groups) {
        groupTableModel.setRowCount(0);
        for (Group group : groups) {
            Vector<Object> row = new Vector<>();
            row.add(group.getName());
            row.add(group.getParentId() != null ? group.getParentId() : "-");
            row.add(group.getDescription());
            row.add(group.getRepeaterTabIds().size());
            groupTableModel.addRow(row);
        }
    }
}