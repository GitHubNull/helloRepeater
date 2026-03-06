package org.oxff.repeater.config;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
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
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // 创建HTML显示面板
        JEditorPane htmlPane = createHtmlPane();
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(245, 245, 245));
        scrollPane.getViewport().setBackground(new Color(245, 245, 245));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JEditorPane createHtmlPane() {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setBackground(new Color(245, 245, 245));
        
        // 生成HTML内容
        String html = generateHtmlContent();
        pane.setText(html);
        
        // 添加链接点击监听器
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getURL().toString();
                openExternalLink(url);
            }
        });
        
        return pane;
    }

    private String generateHtmlContent() {
        String version = getVersionFromGit();
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { " +
                "  font-family: Arial, sans-serif; " +
                "  text-align: center; " +
                "  padding: 20px; " +
                "  background-color: #f5f5f5; " +
                "  color: #333; " +
                "}" +
                "h1 { " +
                "  font-size: 28px; " +
                "  font-weight: bold; " +
                "  margin-bottom: 10px; " +
                "  color: #333; " +
                "}" +
                ".version { " +
                "  font-size: 14px; " +
                "  color: #666; " +
                "  margin-bottom: 20px; " +
                "}" +
                ".separator { " +
                "  border: none; " +
                "  border-top: 1px solid #ddd; " +
                "  margin: 20px auto; " +
                "  width: 80%; " +
                "}" +
                ".description { " +
                "  font-size: 14px; " +
                "  color: #444; " +
                "  margin-bottom: 10px; " +
                "}" +
                ".features { " +
                "  font-size: 12px; " +
                "  color: #888; " +
                "  margin-bottom: 20px; " +
                "}" +
                ".section { " +
                "  margin: 15px 0; " +
                "}" +
                ".label { " +
                "  font-weight: bold; " +
                "  color: #666; " +
                "  font-size: 12px; " +
                "}" +
                ".value { " +
                "  color: #444; " +
                "  font-size: 12px; " +
                "}" +
                "a { " +
                "  color: #0066cc; " +
                "  text-decoration: underline; " +
                "}" +
                "a:hover { " +
                "  color: #0052a3; " +
                "}" +
                ".requirements { " +
                "  font-size: 12px; " +
                "  color: #888; " +
                "  line-height: 1.6; " +
                "}" +
                ".dependencies { " +
                "  font-size: 11px; " +
                "  color: #999; " +
                "  margin-top: 5px; " +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>helloRepeater</h1>" +
                "<div class='version'>版本: " + version + "</div>" +
                "<hr class='separator'>" +
                "<div class='description'>Burp Suite Repeater 管理插件</div>" +
                "<div class='features'>自动重命名标签页 | 分组管理 | 规则配置</div>" +
                "<hr class='separator'>" +
                "<div class='section'>" +
                "<span class='label'>作者:</span> " +
                "<span class='value'>oxff</span>" +
                "</div>" +
                "<div class='section'>" +
                "<div class='label'>系统要求</div>" +
                "<div class='requirements'>" +
                "• Java 17+<br>" +
                "• Burp Suite 2023.x+<br>" +
                "• Windows / macOS / Linux" +
                "</div>" +
                "</div>" +
                "<div class='section'>" +
                "<span class='label'>许可证:</span> " +
                "<span class='value'>Apache License 2.0</span>" +
                "</div>" +
                "<div class='section'>" +
                "<span class='label'>GitHub:</span> " +
                "<a href='https://github.com/GitHubNull/helloRepeater'>" +
                "https://github.com/GitHubNull/helloRepeater" +
                "</a>" +
                "</div>" +
                "<hr class='separator'>" +
                "<div class='section'>" +
                "<div class='label'>依赖组件</div>" +
                "<div class='dependencies'>" +
                "Burp Montoya API | Jayway JSONPath | SQLite JDBC | Jackson | SnakeYAML" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
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

    private void openExternalLink(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel,
                    "无法打开浏览器，请手动访问:\n" + url,
                    "打开链接失败",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
