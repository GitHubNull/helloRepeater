package org.oxff.repeater.config;

import burp.api.montoya.MontoyaApi;
import org.oxff.repeater.model.Group;
import org.oxff.repeater.storage.BurpStorage;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 分组编辑对话框
 */
public class GroupDialog extends JDialog {

    private final Group group;
    private final boolean isNew;
    private final MontoyaApi api;
    private final BurpStorage storage;
    private final List<Group> allGroups;
    private final Runnable onSaveCallback;

    private JTextField nameField;
    private JComboBox<String> parentCombo;
    private JTextArea descriptionArea;
    private Map<String, String> nameToIdMap; // 名称到ID的映射

    public GroupDialog(Group existingGroup, MontoyaApi api, BurpStorage storage, 
                       List<Group> allGroups, Runnable onSaveCallback) {
        this.group = existingGroup != null ? existingGroup : new Group(UUID.randomUUID().toString(), "");
        this.isNew = existingGroup == null;
        this.api = api;
        this.storage = storage;
        this.allGroups = allGroups;
        this.onSaveCallback = onSaveCallback;
        this.nameToIdMap = new HashMap<>();

        initUI();
        loadExistingData();

        setTitle(isNew ? "添加分组" : "编辑分组");
        setModal(true);
        setSize(450, 300);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 分组名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("分组名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        // 父分组
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("父分组:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        parentCombo = new JComboBox<>();
        loadParentOptions();
        panel.add(parentCombo, gbc);

        // 分组描述
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(new JLabel("描述:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        add(panel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> saveGroup());
        buttonPanel.add(saveBtn);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadParentOptions() {
        parentCombo.removeAllItems();
        nameToIdMap.clear();

        // 添加"无"选项
        parentCombo.addItem("无");
        nameToIdMap.put("无", null);

        // 过滤可选的父分组
        for (Group g : allGroups) {
            // 如果是编辑模式，排除当前分组自身
            if (!isNew && g.getId().equals(group.getId())) {
                continue;
            }

            // 检查是否会导致循环引用
            if (!isNew && wouldCreateCycle(group.getId(), g.getId())) {
                continue;
            }

            // 构建层级显示名称
            String displayName = buildHierarchicalName(g, 0);
            parentCombo.addItem(displayName);
            nameToIdMap.put(displayName, g.getId());
        }
    }

    private String buildHierarchicalName(Group group, int depth) {
        if (depth > 10) { // 防止无限递归
            return group.getName();
        }

        StringBuilder sb = new StringBuilder();
        // 添加缩进
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(group.getName());

        // 如果有父分组，递归构建完整路径
        if (group.getParentId() != null && !group.getParentId().isEmpty()) {
            for (Group parent : allGroups) {
                if (parent.getId().equals(group.getParentId())) {
                    return buildHierarchicalName(parent, depth + 1) + " > " + sb.toString().trim();
                }
            }
        }

        return sb.toString().trim();
    }

    private boolean wouldCreateCycle(String childId, String potentialParentId) {
        // 检查将 childId 的父设置为 potentialParentId 是否会创建循环
        String currentId = potentialParentId;
        Set<String> visited = new HashSet<>();

        while (currentId != null && !currentId.isEmpty()) {
            if (currentId.equals(childId)) {
                return true; // 发现循环
            }
            if (visited.contains(currentId)) {
                return true; // 已经访问过，说明有循环
            }
            visited.add(currentId);

            // 查找当前ID的父分组
            String parentId = null;
            for (Group g : allGroups) {
                if (g.getId().equals(currentId)) {
                    parentId = g.getParentId();
                    break;
                }
            }
            currentId = parentId;
        }

        return false;
    }

    private void loadExistingData() {
        nameField.setText(group.getName());
        descriptionArea.setText(group.getDescription());

        // 设置父分组下拉框
        if (group.getParentId() != null && !group.getParentId().isEmpty()) {
            for (Map.Entry<String, String> entry : nameToIdMap.entrySet()) {
                if (group.getParentId().equals(entry.getValue())) {
                    parentCombo.setSelectedItem(entry.getKey());
                    break;
                }
            }
        } else {
            parentCombo.setSelectedItem("无");
        }
    }

    private void saveGroup() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "分组名称不能为空");
            return;
        }

        // 检查名称是否重复（排除自身）
        for (Group g : allGroups) {
            if (!g.getId().equals(group.getId()) && g.getName().equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(this, "分组名称已存在");
                return;
            }
        }

        group.setName(name);
        group.setDescription(descriptionArea.getText().trim());

        // 获取选中的父分组ID
        String selectedParentName = (String) parentCombo.getSelectedItem();
        String parentId = nameToIdMap.get(selectedParentName);
        group.setParentId(parentId);

        group.setUpdatedAt(System.currentTimeMillis());

        storage.saveGroup(group);

        JOptionPane.showMessageDialog(this, isNew ? "分组添加成功！" : "分组更新成功！");
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        dispose();
    }

    public Group getGroup() {
        return group;
    }
}