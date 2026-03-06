package org.oxff.repeater.menu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.http.message.HttpRequestResponse;
import org.oxff.repeater.model.Group;
import org.oxff.repeater.rename.RepeaterRenameHandler;
import org.oxff.repeater.storage.BurpStorage;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 右键菜单提供者
 */
public class ContextMenuProvider implements ContextMenuItemsProvider {

    private final MontoyaApi api;
    private final BurpStorage burpStorage;
    private final RepeaterRenameHandler renameHandler;

    public ContextMenuProvider(MontoyaApi api, BurpStorage burpStorage, RepeaterRenameHandler renameHandler) {
        this.api = api;
        this.burpStorage = burpStorage;
        this.renameHandler = renameHandler;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();

        // 创建"发送到Repeater Manager"菜单项
        JMenu managerMenu = new JMenu("发送到 Repeater Manager");

        // 子菜单：直接发送
        JMenuItem sendItem = new JMenuItem("直接发送");
        sendItem.addActionListener(e -> {
            sendToRepeater(event);
        });
        managerMenu.add(sendItem);

        managerMenu.addSeparator();

        // 子菜单：选择分组
        JMenu groupMenu = new JMenu("选择分组");
        loadGroupMenuItems(groupMenu, event);
        managerMenu.add(groupMenu);

        // 子菜单：新建分组
        JMenuItem newGroupItem = new JMenuItem("新建分组...");
        newGroupItem.addActionListener(e -> {
            showNewGroupDialog(event);
        });
        managerMenu.add(newGroupItem);

        menuItems.add(managerMenu);

        return menuItems;
    }

    private void loadGroupMenuItems(JMenu groupMenu, ContextMenuEvent event) {
        List<Group> groups = burpStorage.loadAllGroups();

        if (groups.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("(无分组)");
            emptyItem.setEnabled(false);
            groupMenu.add(emptyItem);
            return;
        }

        for (Group group : groups) {
            JMenuItem groupItem = new JMenuItem(group.getName());
            groupItem.addActionListener(e -> {
                sendToRepeaterWithGroup(event, group);
            });
            groupMenu.add(groupItem);
        }
    }

    private void sendToRepeater(ContextMenuEvent event) {
        List<HttpRequestResponse> selectedItems = event.selectedRequestResponses();

        for (HttpRequestResponse item : selectedItems) {
            if (item.request() != null) {
                // 发送到Repeater
                int tabId = renameHandler.getNextRepeaterId();
                String title = renameHandler.generateTitle(item.request(), tabId);

                api.repeater().sendToRepeater(item.request(), title);

                api.logging().logToOutput("已发送到Repeater: " + title);
            }
        }
    }

    private void sendToRepeaterWithGroup(ContextMenuEvent event, Group group) {
        List<HttpRequestResponse> selectedItems = event.selectedRequestResponses();
        List<Group> allGroups = burpStorage.loadAllGroups();

        for (HttpRequestResponse item : selectedItems) {
            if (item.request() != null) {
                int tabId = renameHandler.getNextRepeaterId();
                String title = renameHandler.generateTitle(item.request(), tabId);

                // 构建完整分组路径
                String fullPath = buildGroupPath(group, allGroups);
                String groupedTitle = fullPath + "-" + title;

                api.repeater().sendToRepeater(item.request(), groupedTitle);

                // 记录到分组
                group.addRepeaterTabId(title);
                burpStorage.updateGroup(group);

                api.logging().logToOutput("已发送到Repeater [" + fullPath + "]: " + title);
            }
        }
    }

    /**
     * 递归构建分组的完整路径
     */
    private String buildGroupPath(Group group, List<Group> allGroups) {
        StringBuilder path = new StringBuilder(group.getName());
        String parentId = group.getParentId();

        while (parentId != null && !parentId.isEmpty()) {
            boolean found = false;
            for (Group g : allGroups) {
                if (g.getId().equals(parentId)) {
                    path.insert(0, g.getName() + "/");
                    parentId = g.getParentId();
                    found = true;
                    break;
                }
            }
            if (!found) {
                break; // 防止无限循环
            }
        }

        return path.toString();
    }

    private void showNewGroupDialog(ContextMenuEvent event) {
        String groupName = JOptionPane.showInputDialog(
            null,
            "输入分组名称:",
            "新建分组",
            JOptionPane.QUESTION_MESSAGE
        );

        if (groupName != null && !groupName.trim().isEmpty()) {
            Group newGroup = new Group(UUID.randomUUID().toString(), groupName.trim());
            burpStorage.saveGroup(newGroup);

            // 发送请求到新分组
            sendToRepeaterWithGroup(event, newGroup);

            JOptionPane.showMessageDialog(
                null,
                "分组 \"" + groupName + "\" 创建成功！",
                "成功",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}