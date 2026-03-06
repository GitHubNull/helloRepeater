package org.oxff.repeater.config;

import burp.api.montoya.MontoyaApi;
import org.oxff.repeater.model.RenameRule;
import org.oxff.repeater.storage.SQLiteStorage;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.UUID;

/**
 * 规则编辑对话框
 */
public class RuleDialog extends JDialog {

    private final RenameRule rule;
    private final MontoyaApi api;
    private final SQLiteStorage storage;
    private final Runnable onSaveCallback;

    private JTextField nameField;
    private JComboBox<RenameRule.RuleType> typeCombo;
    private JTextField patternField;
    private JSpinner prioritySpinner;
    private JCheckBox enabledCheck;
    private JTextArea descArea;
    private JTextField startIndexField;
    private JTextField endIndexField;

    public RuleDialog(RenameRule existingRule, MontoyaApi api, SQLiteStorage storage, Runnable onSaveCallback) {
        this.rule = existingRule != null ? existingRule : new RenameRule();
        this.api = api;
        this.storage = storage;
        this.onSaveCallback = onSaveCallback;

        initUI();
        if (existingRule != null) {
            loadExistingData();
        } else {
            rule.setId(UUID.randomUUID().toString());
        }

        setTitle(existingRule != null ? "编辑规则" : "添加规则");
        setModal(true);
        setSize(500, 400);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 名称
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("规则名称:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // 类型
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("规则类型:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        typeCombo = new JComboBox<>(RenameRule.RuleType.values());
        typeCombo.addActionListener(e -> updateFieldsForType());
        formPanel.add(typeCombo, gbc);

        // 模式
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("匹配模式:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        patternField = new JTextField(20);
        formPanel.add(patternField, gbc);

        // 路径截取专用字段
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathPanel.add(new JLabel("开始索引:"));
        startIndexField = new JTextField(5);
        startIndexField.setText("-1");
        pathPanel.add(startIndexField);
        pathPanel.add(new JLabel("结束索引:"));
        endIndexField = new JTextField(5);
        endIndexField.setText("-1");
        pathPanel.add(endIndexField);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(pathPanel, gbc);

        // 优先级
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("优先级:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        prioritySpinner = new JSpinner(new SpinnerNumberModel(100, 1, 999, 1));
        formPanel.add(prioritySpinner, gbc);

        // 启用
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("启用:"), gbc);
        gbc.gridx = 1;
        enabledCheck = new JCheckBox();
        enabledCheck.setSelected(true);
        formPanel.add(enabledCheck, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(descArea), gbc);

        add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> saveRule());
        buttonPanel.add(saveBtn);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        updateFieldsForType();
    }

    private void loadExistingData() {
        nameField.setText(rule.getName());
        typeCombo.setSelectedItem(rule.getType());
        patternField.setText(rule.getPattern());
        prioritySpinner.setValue(rule.getPriority());
        enabledCheck.setSelected(rule.isEnabled());
        descArea.setText(rule.getDescription());
        startIndexField.setText(String.valueOf(rule.getPathStartIndex()));
        endIndexField.setText(String.valueOf(rule.getPathEndIndex()));
    }

    private void updateFieldsForType() {
        RenameRule.RuleType type = (RenameRule.RuleType) typeCombo.getSelectedItem();
        boolean isPathSubstring = type == RenameRule.RuleType.PATH_SUBSTRING;

        startIndexField.setEnabled(isPathSubstring);
        endIndexField.setEnabled(isPathSubstring);

        // 更新提示文本
        switch (type) {
            case PATH_SUBSTRING:
                patternField.setToolTipText("输入 'last16' 表示取后16个字符，或留空使用索引");
                break;
            case PATH_REGEX:
                patternField.setToolTipText("输入正则表达式，如: /api/(v\\d+)/.*");
                break;
            case BODY_REGEX:
                patternField.setToolTipText("输入Body正则表达式，如: \"action\":\\s*\"([^\"]+)\"");
                break;
            case BODY_JSON_PATH:
                patternField.setToolTipText("输入JSON Path，如: $.action 或 $.data[0].name");
                break;
            case BODY_XPATH:
                patternField.setToolTipText("输入XPath，如: //method/text() 或 /root/element/@attr");
                break;
        }
    }

    private void saveRule() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "规则名称不能为空");
            return;
        }

        rule.setName(name);
        rule.setType((RenameRule.RuleType) typeCombo.getSelectedItem());
        rule.setPattern(patternField.getText().trim());
        rule.setPriority((Integer) prioritySpinner.getValue());
        rule.setEnabled(enabledCheck.isSelected());
        rule.setDescription(descArea.getText().trim());

        try {
            rule.setPathStartIndex(Integer.parseInt(startIndexField.getText().trim()));
            rule.setPathEndIndex(Integer.parseInt(endIndexField.getText().trim()));
        } catch (NumberFormatException e) {
            rule.setPathStartIndex(-1);
            rule.setPathEndIndex(-1);
        }

        rule.setUpdatedAt(System.currentTimeMillis());

        try {
            storage.saveRule(rule);
            JOptionPane.showMessageDialog(this, "规则保存成功！");
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();
        } catch (SQLException e) {
            api.logging().logToError("保存规则失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage());
        }
    }
}