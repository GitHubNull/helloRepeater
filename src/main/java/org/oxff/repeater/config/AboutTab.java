package org.oxff.repeater.config;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 关于标签页 - 简洁卡片式布局
 * 从Git Tag获取版本号
 */
public class AboutTab {

    private JPanel mainPanel;

    public AboutTab() {
        initUI();
    }

    public Component getUI() {
        return mainPanel;
    }

    private void initUI() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);

        int row = 0;

        // 插件标题
        gbc.gridy = row++;
        JLabel titleLabel = new JLabel("helloRepeater");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        titleLabel.setForeground(new Color(51, 51, 51));
        mainPanel.add(titleLabel, gbc);

        // 版本号
        gbc.gridy = row++;
        String version = getVersionFromGit();
        JLabel versionLabel = new JLabel("版本: " + version);
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        versionLabel.setForeground(new Color(102, 102, 102));
        mainPanel.add(versionLabel, gbc);

        // 分隔线
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator separator1 = new JSeparator();
        separator1.setPreferredSize(new Dimension(400, 1));
        mainPanel.add(separator1, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // 插件描述
        gbc.gridy = row++;
        JLabel descLabel = new JLabel("Burp Suite Repeater 管理插件");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        descLabel.setForeground(new Color(68, 68, 68));
        mainPanel.add(descLabel, gbc);

        // 功能简介
        gbc.gridy = row++;
        JLabel featuresLabel = new JLabel("自动重命名标签页 | 分组管理 | 规则配置");
        featuresLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        featuresLabel.setForeground(new Color(136, 136, 136));
        mainPanel.add(featuresLabel, gbc);

        // 分隔线
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator separator2 = new JSeparator();
        separator2.setPreferredSize(new Dimension(400, 1));
        mainPanel.add(separator2, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // 作者信息
        gbc.gridy = row++;
        JPanel authorPanel = createInfoPanel("作者", "oxff");
        mainPanel.add(authorPanel, gbc);

        // 系统要求
        gbc.gridy = row++;
        JPanel requirementsPanel = createRequirementsPanel();
        mainPanel.add(requirementsPanel, gbc);

        // 许可证
        gbc.gridy = row++;
        JPanel licensePanel = createInfoPanel("许可证", "Apache License 2.0");
        mainPanel.add(licensePanel, gbc);

        // GitHub链接
        gbc.gridy = row++;
        JPanel githubPanel = createLinkPanel("GitHub", "https://github.com/GitHubNull/helloRepeater");
        mainPanel.add(githubPanel, gbc);

        // 分隔线
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator separator3 = new JSeparator();
        separator3.setPreferredSize(new Dimension(400, 1));
        mainPanel.add(separator3, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // 依赖组件
        gbc.gridy = row++;
        JPanel dependenciesPanel = createDependenciesPanel();
        mainPanel.add(dependenciesPanel, gbc);

        // 弹性空间
        gbc.gridy = row++;
        gbc.weighty = 1.0;
        mainPanel.add(Box.createVerticalGlue(), gbc);
    }

    /**
     * 从Git Tag获取版本号
     */
    private String getVersionFromGit() {
        try {
            // 首先尝试从打包的资源文件读取版本
            String versionFromResource = getVersionFromResource();
            if (versionFromResource != null && !versionFromResource.isEmpty()) {
                return versionFromResource;
            }

            // 如果资源文件不存在，尝试执行git命令
            Process process = Runtime.getRuntime().exec("git describe --tags --abbrev=0");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            process.waitFor();

            if (version != null && !version.isEmpty()) {
                return version.startsWith("v") ? version : "v" + version;
            }
        } catch (Exception e) {
            // 忽略错误，返回默认版本
        }

        // 尝试从pom.xml读取版本
        String pomVersion = getVersionFromPom();
        if (pomVersion != null) {
            return "v" + pomVersion;
        }

        return "v1.0.0";
    }

    /**
     * 从资源文件读取版本
     */
    private String getVersionFromResource() {
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream("/version.txt"))) {
            BufferedReader br = new BufferedReader(reader);
            return br.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从pom.xml读取版本（简化实现）
     */
    private String getVersionFromPom() {
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream("/META-INF/maven/org.oxff/helloRepeater/pom.xml"))) {
            if (reader == null) return null;

            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("<version>") && !line.contains("<project ")) {
                    int start = line.indexOf("<version>") + 9;
                    int end = line.indexOf("</version>");
                    if (start > 8 && end > start) {
                        String version = line.substring(start, end).trim();
                        // 排除变量引用
                        if (!version.contains("${") && !version.contains("}")) {
                            return version;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return null;
    }

    private JPanel createInfoPanel(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setOpaque(false);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        labelComponent.setForeground(new Color(102, 102, 102));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        valueComponent.setForeground(new Color(68, 68, 68));

        panel.add(labelComponent);
        panel.add(valueComponent);

        return panel;
    }

    private JPanel createLinkPanel(String label, String url) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setOpaque(false);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        labelComponent.setForeground(new Color(102, 102, 102));

        JButton linkButton = new JButton("<html><u>" + url + "</u></html>");
        linkButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        linkButton.setForeground(new Color(0, 102, 204));
        linkButton.setBorderPainted(false);
        linkButton.setContentAreaFilled(false);
        linkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "无法打开浏览器，请手动访问:\n" + url,
                        "打开链接失败",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(labelComponent);
        panel.add(linkButton);

        return panel;
    }

    private JPanel createRequirementsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel titleLabel = new JLabel("系统要求");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(new Color(102, 102, 102));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));

        String[] requirements = {
                "Java 17+",
                "Burp Suite 2023.x+",
                "Windows / macOS / Linux"
        };

        for (String req : requirements) {
            JLabel reqLabel = new JLabel("• " + req);
            reqLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            reqLabel.setForeground(new Color(136, 136, 136));
            reqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(reqLabel);
        }

        return panel;
    }

    private JPanel createDependenciesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel titleLabel = new JLabel("依赖组件");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(new Color(102, 102, 102));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));

        String[] dependencies = {
                "Burp Montoya API",
                "Jayway JSONPath",
                "SQLite JDBC",
                "Jackson",
                "SnakeYAML"
        };

        // 使用流式布局显示依赖
        JPanel depsFlowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        depsFlowPanel.setOpaque(false);

        for (String dep : dependencies) {
            JLabel depLabel = new JLabel(dep);
            depLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            depLabel.setForeground(new Color(153, 153, 153));
            depsFlowPanel.add(depLabel);
        }

        panel.add(depsFlowPanel);

        return panel;
    }
}
