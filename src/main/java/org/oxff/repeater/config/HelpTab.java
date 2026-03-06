package org.oxff.repeater.config;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 帮助标签页 - 树形导航+内容面板
 */
public class HelpTab {

    private JPanel mainPanel;
    private JTextPane contentPane;
    private JTree navigationTree;

    public HelpTab() {
        initUI();
    }

    public Component getUI() {
        return mainPanel;
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());

        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.25); // 左侧占25%

        // 左侧导航树
        JScrollPane treeScrollPane = createNavigationTree();
        splitPane.setLeftComponent(treeScrollPane);

        // 右侧内容面板
        JScrollPane contentScrollPane = createContentPanel();
        splitPane.setRightComponent(contentScrollPane);

        mainPanel.add(splitPane, BorderLayout.CENTER);
    }

    private JScrollPane createNavigationTree() {
        // 创建树节点
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("帮助目录");

        // 快速开始
        DefaultMutableTreeNode quickStart = new DefaultMutableTreeNode("快速开始");
        quickStart.add(new DefaultMutableTreeNode("安装步骤"));
        quickStart.add(new DefaultMutableTreeNode("基本使用流程"));
        quickStart.add(new DefaultMutableTreeNode("发送方式介绍"));
        root.add(quickStart);

        // 功能详解
        DefaultMutableTreeNode features = new DefaultMutableTreeNode("功能详解");
        features.add(new DefaultMutableTreeNode("自动重命名原理"));
        features.add(new DefaultMutableTreeNode("分组管理机制"));
        features.add(new DefaultMutableTreeNode("规则测试工具"));
        root.add(features);

        // 规则配置指南
        DefaultMutableTreeNode rules = new DefaultMutableTreeNode("规则配置指南");
        rules.add(new DefaultMutableTreeNode("路径截取规则"));
        rules.add(new DefaultMutableTreeNode("路径正则规则"));
        rules.add(new DefaultMutableTreeNode("Body正则规则"));
        rules.add(new DefaultMutableTreeNode("Body JSON Path规则"));
        rules.add(new DefaultMutableTreeNode("Body XPath规则"));
        root.add(rules);

        // 常见问题
        DefaultMutableTreeNode faq = new DefaultMutableTreeNode("常见问题");
        faq.add(new DefaultMutableTreeNode("安装问题"));
        faq.add(new DefaultMutableTreeNode("规则不生效"));
        faq.add(new DefaultMutableTreeNode("数据备份"));
        root.add(faq);

        // 故障排查
        DefaultMutableTreeNode troubleshooting = new DefaultMutableTreeNode("故障排查");
        troubleshooting.add(new DefaultMutableTreeNode("查看日志"));
        troubleshooting.add(new DefaultMutableTreeNode("常见问题解决"));
        root.add(troubleshooting);

        // 外部链接
        DefaultMutableTreeNode links = new DefaultMutableTreeNode("参考链接");
        links.add(new DefaultMutableTreeNode("JSON Path语法"));
        links.add(new DefaultMutableTreeNode("XPath语法"));
        links.add(new DefaultMutableTreeNode("正则表达式"));
        root.add(links);

        navigationTree = new JTree(new DefaultTreeModel(root));
        navigationTree.setRootVisible(false);  // 隐藏根节点，从一级开始显示
        navigationTree.setShowsRootHandles(true);  // 显示展开/折叠图标
        navigationTree.setRowHeight(22);  // 设置行高，确保图标可见
        
        // 设置树形样式，显示连接线
        navigationTree.putClientProperty("JTree.lineStyle", "Angled");
        
        // 设置自定义渲染器，强制显示层级缩进
        navigationTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected,
                        expanded, leaf, row, hasFocus);
                
                // 根据节点层级设置缩进
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    int level = node.getLevel();
                    // 根节点level=0，一级节点level=1，二级节点level=2
                    // 每个层级缩进20像素
                    int indent = (level - 1) * 20;
                    if (indent < 0) indent = 0;
                    
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;
                        label.setBorder(BorderFactory.createEmptyBorder(2, indent, 2, 5));
                    }
                }
                
                return c;
            }
        });
        
        // 默认展开所有节点（使用递归方式）
        expandAllNodes(navigationTree, root, new TreePath(root));

        // 添加选择监听器
        navigationTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) navigationTree.getLastSelectedPathComponent();
                if (node != null && node.isLeaf()) {
                    updateContent(node.getUserObject().toString());
                }
            }
        });

        return new JScrollPane(navigationTree);
    }

    private JScrollPane createContentPanel() {
        contentPane = new JTextPane();
        contentPane.setContentType("text/html");
        contentPane.setEditable(false);
        contentPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        // 添加链接点击监听器
        contentPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getURL().toString();
                openExternalLink(url);
            }
        });

        // 显示默认内容
        updateContent("帮助目录");

        return new JScrollPane(contentPane);
    }

    private void updateContent(String section) {
        String html = generateContent(section);
        contentPane.setText(html);
        contentPane.setCaretPosition(0); // 滚动到顶部
    }

    private String generateContent(String section) {
        switch (section) {
            case "帮助目录":
                return getHelpIndexContent();
            case "安装步骤":
                return getInstallationContent();
            case "基本使用流程":
                return getBasicUsageContent();
            case "发送方式介绍":
                return getSendMethodsContent();
            case "自动重命名原理":
                return getAutoRenameContent();
            case "分组管理机制":
                return getGroupManagementContent();
            case "规则测试工具":
                return getRuleTesterContent();
            case "路径截取规则":
                return getPathSubstringContent();
            case "路径正则规则":
                return getPathRegexContent();
            case "Body正则规则":
                return getBodyRegexContent();
            case "Body JSON Path规则":
                return getBodyJsonPathContent();
            case "Body XPath规则":
                return getBodyXPathContent();
            case "安装问题":
                return getInstallationFaqContent();
            case "规则不生效":
                return getRuleNotWorkingContent();
            case "数据备份":
                return getDataBackupContent();
            case "查看日志":
                return getViewLogsContent();
            case "常见问题解决":
                return getCommonSolutionsContent();
            case "JSON Path语法":
                return getJsonPathReferenceContent();
            case "XPath语法":
                return getXPathReferenceContent();
            case "正则表达式":
                return getRegexReferenceContent();
            default:
                return getDefaultContent();
        }
    }

    private String getHelpIndexContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>欢迎使用 helloRepeater 插件帮助</h2>" +
                "<p>helloRepeater 是一个 Burp Suite 插件，用于增强 Repeater 功能，支持自动重命名标签页和分组管理。</p>" +
                "<h3>快速导航</h3>" +
                "<ul>" +
                "<li><b>快速开始</b> - 学习如何安装和使用插件</li>" +
                "<li><b>功能详解</b> - 深入了解各项功能</li>" +
                "<li><b>规则配置指南</b> - 学习如何配置各种规则</li>" +
                "<li><b>常见问题</b> - 查看常见问题解答</li>" +
                "<li><b>故障排查</b> - 解决使用中遇到的问题</li>" +
                "<li><b>参考链接</b> - 查看相关语法文档</li>" +
                "</ul>" +
                "<p style='margin-top: 20px; color: #666;'>点击左侧导航树查看详细内容</p>" +
                "</body></html>";
    }

    private String getInstallationContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>安装步骤</h2>" +
                "<h3>前提条件</h3>" +
                "<ul>" +
                "<li>Burp Suite Professional 或 Community Edition 2023.x+</li>" +
                "<li>Java 17 或更高版本</li>" +
                "</ul>" +
                "<h3>安装方法</h3>" +
                "<ol>" +
                "<li><b>下载插件</b><br/>" +
                "从 GitHub Releases 页面下载最新的 helloRepeater.jar 文件</li>" +
                "<li><b>加载插件</b><br/>" +
                "在 Burp Suite 中，点击菜单：<b>Extensions → Installed → Add</b></li>" +
                "<li><b>选择文件</b><br/>" +
                "在弹出的对话框中，选择下载的 JAR 文件</li>" +
                "<li><b>确认安装</b><br/>" +
                "插件会自动加载，并在 Output 窗口显示初始化信息</li>" +
                "</ol>" +
                "<h3>验证安装</h3>" +
                "<p>安装成功后，你应该能在 Burp Suite 中看到：</p>" +
                "<ul>" +
                "<li>顶部 Tab 栏出现 <b>\"helloRepeater\"</b> 标签页</li>" +
                "<li>右键菜单中出现 <b>\"发送到 Repeater Manager\"</b> 选项</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getBasicUsageContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>基本使用流程</h2>" +
                "<h3>1. 配置规则（可选）</h3>" +
                "<p>在 <b>helloRepeater</b> 标签页中：</p>" +
                "<ol>" +
                "<li>切换到\"重命名规则\"标签</li>" +
                "<li>点击\"添加规则\"创建自定义规则</li>" +
                "<li>设置规则优先级（数字越小优先级越高）</li>" +
                "</ol>" +
                "<h3>2. 创建分组（可选）</h3>" +
                "<p>在\"分组管理\"标签中：</p>" +
                "<ol>" +
                "<li>点击\"添加分组\"</li>" +
                "<li>输入分组名称和描述</li>" +
                "<li>可选择父分组创建层级结构</li>" +
                "</ol>" +
                "<h3>3. 发送请求</h3>" +
                "<p>在 Proxy History 或任何其他工具中：</p>" +
                "<ol>" +
                "<li>右键点击要发送的请求</li>" +
                "<li>选择\"发送到 Repeater Manager\"</li>" +
                "<li>选择发送方式：直接发送、选择分组或新建分组</li>" +
                "</ol>" +
                "<h3>4. 自动重命名</h3>" +
                "<p>请求会自动发送到 Repeater，并根据规则自动重命名标签页</p>" +
                "</body></html>";
    }

    private String getSendMethodsContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>发送方式介绍</h2>" +
                "<p>当右键请求选择\"发送到 Repeater Manager\"时，会显示三种发送方式：</p>" +
                "<h3>1. 直接发送</h3>" +
                "<p>请求直接发送到 Repeater，使用默认规则自动命名标签页</p>" +
                "<p><b>适用场景：</b>快速发送，不需要分组管理</p>" +
                "<h3>2. 选择分组</h3>" +
                "<p>选择已有的分组，请求会发送到 Repeater 并归类到该分组</p>" +
                "<p><b>适用场景：</b>按项目或功能模块组织请求</p>" +
                "<h3>3. 新建分组</h3>" +
                "<p>创建一个新分组，并将请求发送到该分组</p>" +
                "<p><b>适用场景：</b>开始新项目或新功能测试</p>" +
                "<h3>分组的作用</h3>" +
                "<ul>" +
                "<li>在 Repeater 中按分组查看请求</li>" +
                "<li>快速定位相关请求</li>" +
                "<li>导出导入分组配置进行团队协作</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getAutoRenameContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>自动重命名原理</h2>" +
                "<h3>工作原理</h3>" +
                "<p>当请求发送到 Repeater 时，插件会：</p>" +
                "<ol>" +
                "<li>按优先级排序所有启用的规则</li>" +
                "<li>从最高优先级开始依次尝试匹配</li>" +
                "<li>第一个成功匹配的规则决定标签页名称</li>" +
                "<li>如果没有规则匹配，使用默认名称</li>" +
                "</ol>" +
                "<h3>规则优先级</h3>" +
                "<p>优先级数字越小，优先级越高：</p>" +
                "<ul>" +
                "<li>优先级 1 - 最先尝试</li>" +
                "<li>优先级 100 - 较后尝试</li>" +
                "</ul>" +
                "<h3>匹配过程</h3>" +
                "<p>每条规则包含：</p>" +
                "<ul>" +
                "<li><b>类型</b> - 决定从何处提取信息</li>" +
                "<li><b>模式</b> - 具体的匹配规则</li>" +
                "<li><b>提取逻辑</b> - 如何从匹配结果生成名称</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getGroupManagementContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>分组管理机制</h2>" +
                "<h3>分组概念</h3>" +
                "<p>分组用于将相关的 Repeater 请求组织在一起，便于管理和查找。</p>" +
                "<h3>分组属性</h3>" +
                "<ul>" +
                "<li><b>名称</b> - 分组的显示名称</li>" +
                "<li><b>父分组</b> - 可选，用于创建层级结构</li>" +
                "<li><b>描述</b> - 分组的说明信息</li>" +
                "<li><b>标签页</b> - 属于该分组的 Repeater 标签</li>" +
                "</ul>" +
                "<h3>层级结构</h3>" +
                "<p>支持多级分组：</p>" +
                "<pre style='background: #f5f5f5; padding: 10px; border-radius: 4px;'>" +
                "项目A/\n" +
                "  ├── 用户模块/\n" +
                "  │   ├── 登录\n" +
                "  │   └── 注册\n" +
                "  └── 订单模块/\n" +
                "      └── 创建订单" +
                "</pre>" +
                "<h3>数据持久化</h3>" +
                "<p>分组数据存储在 Burp Suite 的持久化存储中，重启后依然存在。</p>" +
                "</body></html>";
    }

    private String getRuleTesterContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>规则测试工具</h2>" +
                "<h3>用途</h3>" +
                "<p>在正式使用规则前，可以先用测试工具验证规则是否正确。</p>" +
                "<h3>使用方法</h3>" +
                "<ol>" +
                "<li>切换到\"规则测试\"标签</li>" +
                "<li>在输入框中粘贴 HTTP 请求数据</li>" +
                "<li>点击\"测试规则\"按钮</li>" +
                "<li>查看测试结果区域显示的匹配情况</li>" +
                "</ol>" +
                "<h3>测试示例</h3>" +
                "<pre style='background: #f5f5f5; padding: 10px; border-radius: 4px; font-size: 12px;'>" +
                "POST /api/v1/users/login HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Type: application/json\n\n" +
                "{\"action\": \"userLogin\", \"username\": \"admin\"}" +
                "</pre>" +
                "<h3>结果解读</h3>" +
                "<p>测试结果会显示：</p>" +
                "<ul>" +
                "<li>哪些规则匹配成功</li>" +
                "<li>提取到的标签页名称</li>" +
                "<li>匹配失败的规则及原因</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getPathSubstringContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>路径截取规则</h2>" +
                "<h3>功能说明</h3>" +
                "<p>从 URL 路径中截取指定范围的字符作为标签页名称。</p>" +
                "<h3>配置参数</h3>" +
                "<ul>" +
                "<li><b>起始位置</b> - 从路径的第几个字符开始（从0开始计数）</li>" +
                "<li><b>长度</b> - 截取的字符数量</li>" +
                "<li><b>从末尾计算</b> - 是否从路径末尾开始计算</li>" +
                "</ul>" +
                "<h3>示例</h3>" +
                "<p><b>请求路径：</b> /api/v1/users/login</p>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>配置</th><th>结果</th></tr>" +
                "<tr><td>后16字符</td><td>users/login</td></tr>" +
                "<tr><td>起始5, 长度3</td><td>v1</td></tr>" +
                "<tr><td>起始8, 长度5</td><td>users</td></tr>" +
                "</table>" +
                "<h3>常见用法</h3>" +
                "<ul>" +
                "<li><b>后16字符</b> - 适合 API 路径，显示接口名称</li>" +
                "<li><b>提取版本号</b> - 起始位置配合长度提取 v1、v2 等</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getPathRegexContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>路径正则规则</h2>" +
                "<h3>功能说明</h3>" +
                "<p>使用正则表达式从 URL 路径中提取匹配的内容。</p>" +
                "<h3>配置参数</h3>" +
                "<ul>" +
                "<li><b>正则表达式</b> - 匹配路径的模式</li>" +
                "<li><b>捕获组</b> - 使用括号 () 标记要提取的部分</li>" +
                "</ul>" +
                "<h3>示例</h3>" +
                "<p><b>请求路径：</b> /api/v2/products/123/details</p>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>正则表达式</th><th>结果</th></tr>" +
                "<tr><td>/api/v(\\d+)</td><td>v2</td></tr>" +
                "<tr><td>/products/(\\d+)</td><td>123</td></tr>" +
                "<tr><td>/(\\w+)/(\\d+)</td><td>products/123</td></tr>" +
                "</table>" +
                "<h3>常用模式</h3>" +
                "<ul>" +
                "<li><code>\\d+</code> - 匹配一个或多个数字</li>" +
                "<li><code>\\w+</code> - 匹配一个或多个单词字符</li>" +
                "<li><code>[a-z]+</code> - 匹配小写字母</li>" +
                "</ul>" +
                "<p><b>参考链接：</b><a href='https://regexr.com/'>正则表达式在线测试工具</a></p>" +
                "</body></html>";
    }

    private String getBodyRegexContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>Body正则规则</h2>" +
                "<h3>功能说明</h3>" +
                "<p>从 HTTP 请求 Body 中使用正则表达式提取内容。</p>" +
                "<h3>适用场景</h3>" +
                "<ul>" +
                "<li>提取表单字段值</li>" +
                "<li>提取自定义格式的数据</li>" +
                "<li>从非结构化文本中提取信息</li>" +
                "</ul>" +
                "<h3>示例</h3>" +
                "<p><b>请求 Body：</b></p>" +
                "<pre style='background: #f5f5f5; padding: 10px; border-radius: 4px;'>" +
                "action=userLogin&amp;username=admin&amp;password=secret" +
                "</pre>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>正则表达式</th><th>结果</th></tr>" +
                "<tr><td>action=(\\w+)</td><td>userLogin</td></tr>" +
                "<tr><td>username=(\\w+)</td><td>admin</td></tr>" +
                "</table>" +
                "<h3>注意事项</h3>" +
                "<ul>" +
                "<li>支持多行匹配</li>" +
                "<li>如果 Body 是二进制数据，可能无法匹配</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getBodyJsonPathContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>Body JSON Path规则</h2>" +
                "<h3>功能说明</h3>" +
                "<p>使用 JSON Path 表达式从 JSON 格式的请求 Body 中提取字段值。</p>" +
                "<h3>适用场景</h3>" +
                "<ul>" +
                "<li>API 接口使用 JSON 格式传输数据</li>" +
                "<li>需要从嵌套 JSON 中提取特定字段</li>" +
                "<li>根据 action 或 method 字段区分接口</li>" +
                "</ul>" +
                "<h3>示例</h3>" +
                "<p><b>请求 Body：</b></p>" +
                "<pre style='background: #f5f5f5; padding: 10px; border-radius: 4px;'>" +
                "{\n" +
                "  \"action\": \"getUserList\",\n" +
                "  \"params\": {\n" +
                "    \"page\": 1,\n" +
                "    \"size\": 20\n" +
                "  }\n" +
                "}" +
                "</pre>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>JSON Path</th><th>结果</th></tr>" +
                "<tr><td>$.action</td><td>getUserList</td></tr>" +
                "<tr><td>$.params.page</td><td>1</td></tr>" +
                "</table>" +
                "<h3>常用语法</h3>" +
                "<ul>" +
                "<li><code>$</code> - 根对象</li>" +
                "<li><code>.property</code> - 点号表示法访问属性</li>" +
                "<li><code>['property']</code> - 括号表示法</li>" +
                "<li><code>[*]</code> - 所有数组元素</li>" +
                "<li><code>[0]</code> - 数组第一个元素</li>" +
                "</ul>" +
                "<p><b>参考链接：</b><a href='https://goessner.net/articles/JsonPath/'>JSON Path 完整语法文档</a></p>" +
                "</body></html>";
    }

    private String getBodyXPathContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>Body XPath规则</h2>" +
                "<h3>功能说明</h3>" +
                "<p>使用 XPath 表达式从 XML 格式的请求 Body 中提取节点值。</p>" +
                "<h3>适用场景</h3>" +
                "<ul>" +
                "<li>SOAP Web Service 接口</li>" +
                "<li>XML-RPC 接口</li>" +
                "<li>配置文件的 XML 格式</li>" +
                "</ul>" +
                "<h3>示例</h3>" +
                "<p><b>请求 Body：</b></p>" +
                "<pre style='background: #f5f5f5; padding: 10px; border-radius: 4px;'>" +
                "&lt;request&gt;\n" +
                "  &lt;method&gt;getUserInfo&lt;/method&gt;\n" +
                "  &lt;params&gt;\n" +
                "    &lt;id&gt;123&lt;/id&gt;\n" +
                "  &lt;/params&gt;\n" +
                "&lt;/request&gt;" +
                "</pre>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>XPath</th><th>结果</th></tr>" +
                "<tr><td>//method</td><td>getUserInfo</td></tr>" +
                "<tr><td>//params/id</td><td>123</td></tr>" +
                "</table>" +
                "<h3>常用语法</h3>" +
                "<ul>" +
                "<li><code>/</code> - 从根节点选择</li>" +
                "<code>//</code> - 从任意位置选择</li>" +
                "<li><code>@attribute</code> - 选择属性</li>" +
                "<li><code>node()</code> - 匹配任何节点</li>" +
                "<li><code>text()</code> - 选择文本内容</li>" +
                "</ul>" +
                "<p><b>参考链接：</b><a href='https://www.w3schools.com/xml/xpath_syntax.asp'>XPath 完整语法教程</a></p>" +
                "</body></html>";
    }

    private String getInstallationFaqContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>安装问题</h2>" +
                "<h3>Q: 插件加载失败，提示版本不兼容</h3>" +
                "<p><b>A:</b> 请检查 Burp Suite 版本是否为 2023.x 或更高版本。旧版本可能不支持 Montoya API。</p>" +
                "<h3>Q: 加载插件后没有显示 helloRepeater 标签页</h3>" +
                "<p><b>A:</b> 检查 Output 窗口是否有错误信息。常见问题包括：</p>" +
                "<ul>" +
                "<li>Java 版本过低（需要 Java 17+）</li>" +
                "<li>JAR 文件损坏，请重新下载</li>" +
                "<li>插件加载路径包含中文或特殊字符</li>" +
                "</ul>" +
                "<h3>Q: 右键菜单没有\"发送到 Repeater Manager\"选项</h3>" +
                "<p><b>A:</b> 右键菜单可能需要重新加载。尝试重启 Burp Suite。</p>" +
                "</body></html>";
    }

    private String getRuleNotWorkingContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>规则不生效</h2>" +
                "<h3>可能原因</h3>" +
                "<ol>" +
                "<li><b>规则未启用</b><br/>" +
                "检查规则表格中的\"启用\"列是否勾选</li>" +
                "<li><b>优先级设置不当</b><br/>" +
                "确保规则的优先级正确，高优先级的规则会优先匹配</li>" +
                "<li><b>正则表达式错误</b><br/>" +
                "使用\"规则测试\"功能验证正则是否正确</li>" +
                "<li><b>匹配条件不满足</b><br/>" +
                "检查请求的路径或 Body 是否符合规则的预期</li>" +
                "</ol>" +
                "<h3>调试方法</h3>" +
                "<ol>" +
                "<li>使用\"规则测试\"工具输入请求数据</li>" +
                "<li>查看测试结果了解匹配情况</li>" +
                "<li>简化规则逐步排查问题</li>" +
                "</ol>" +
                "</body></html>";
    }

    private String getDataBackupContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>数据备份</h2>" +
                "<h3>备份内容</h3>" +
                "<p>插件的数据包括：</p>" +
                "<ul>" +
                "<li><b>规则数据</b> - 存储在 SQLite 数据库中</li>" +
                "<li><b>分组数据</b> - 存储在 Burp 持久化存储中</li>" +
                "</ul>" +
                "<h3>备份方法</h3>" +
                "<h4>1. 规则备份</h4>" +
                "<ol>" +
                "<li>打开 helloRepeater 标签页</li>" +
                "<li>切换到\"重命名规则\"标签</li>" +
                "<li>点击\"导出规则\"按钮</li>" +
                "<li>保存 YAML 文件到安全位置</li>" +
                "</ol>" +
                "<h4>2. 分组备份</h4>" +
                "<ol>" +
                "<li>切换到\"分组管理\"标签</li>" +
                "<li>点击\"导出分组\"按钮</li>" +
                "<li>保存分组配置文件</li>" +
                "</ol>" +
                "<h3>数据文件位置</h3>" +
                "<p>SQLite 数据库文件：<code>repeater_manager.db</code></p>" +
                "<p>位于 Burp Suite 的启动目录下</p>" +
                "</body></html>";
    }

    private String getViewLogsContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>查看日志</h2>" +
                "<h3>日志位置</h3>" +
                "<p>插件的日志输出到 Burp Suite 的 <b>Output</b> 窗口：</p>" +
                "<ol>" +
                "<li>点击菜单 <b>View → Output</b></li>" +
                "<li>在 Output 窗口查看插件日志</li>" +
                "</ol>" +
                "<h3>日志级别</h3>" +
                "<ul>" +
                "<li><b>INFO</b> - 一般信息，如初始化完成</li>" +
                "<li><b>ERROR</b> - 错误信息，如数据库连接失败</li>" +
                "</ul>" +
                "<h3>常见问题日志</h3>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>日志信息</th><th>含义</th></tr>" +
                "<tr><td>SQLite 数据库初始化成功</td><td>数据库连接正常</td></tr>" +
                "<tr><td>插件初始化完成</td><td>插件加载成功</td></tr>" +
                "<tr><td>加载规则失败</td><td>数据库或配置文件问题</td></tr>" +
                "</table>" +
                "</body></html>";
    }

    private String getCommonSolutionsContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>常见问题解决</h2>" +
                "<h3>插件功能异常</h3>" +
                "<p><b>症状：</b>插件加载成功但功能不工作</p>" +
                "<p><b>解决步骤：</b></p>" +
                "<ol>" +
                "<li>重启 Burp Suite</li>" +
                "<li>检查是否有其他插件冲突</li>" +
                "<li>查看 Output 窗口的错误日志</li>" +
                "<li>尝试重新安装插件</li>" +
                "</ol>" +
                "<h3>数据丢失</h3>" +
                "<p><b>症状：</b>重启后规则或分组消失</p>" +
                "<p><b>可能原因：</b></p>" +
                "<ul>" +
                "<li>数据库文件被删除或损坏</li>" +
                "<li>Burp Suite 配置文件损坏</li>" +
                "<li>权限问题导致无法写入</li>" +
                "</ul>" +
                "<p><b>预防措施：</b></p>" +
                "<ul>" +
                "<li>定期导出规则和分组</li>" +
                "<li>备份 repeater_manager.db 文件</li>" +
                "</ul>" +
                "</body></html>";
    }

    private String getJsonPathReferenceContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>JSON Path 语法参考</h2>" +
                "<p>正在打开 JSON Path 官方文档...</p>" +
                "<p>如果浏览器未自动打开，请访问：</p>" +
                "<p><a href='https://goessner.net/articles/JsonPath/'>https://goessner.net/articles/JsonPath/</a></p>" +
                "<h3>常用语法速查</h3>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>表达式</th><th>说明</th></tr>" +
                "<tr><td>$</td><td>根对象</td></tr>" +
                "<tr><td>.property</td><td>点号访问属性</td></tr>" +
                "<tr><td>['property']</td><td>括号访问属性</td></tr>" +
                "<tr><td>[n]</td><td>数组第n个元素</td></tr>" +
                "<tr><td>[*]</td><td>所有数组元素</td></tr>" +
                "<tr><td>..property</td><td>递归查找</td></tr>" +
                "</table>" +
                "</body></html>";
    }

    private String getXPathReferenceContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>XPath 语法参考</h2>" +
                "<p>正在打开 XPath 教程...</p>" +
                "<p>如果浏览器未自动打开，请访问：</p>" +
                "<p><a href='https://www.w3schools.com/xml/xpath_syntax.asp'>https://www.w3schools.com/xml/xpath_syntax.asp</a></p>" +
                "<h3>常用语法速查</h3>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>表达式</th><th>说明</th></tr>" +
                "<tr><td>/</td><td>从根节点选择</td></tr>" +
                "<tr><td>//</td><td>从任意位置选择</td></tr>" +
                "<tr><td>nodename</td><td>选择指定节点</td></tr>" +
                "<tr><td>@attribute</td><td>选择属性</td></tr>" +
                "<tr><td>.</td><td>当前节点</td></tr>" +
                "<tr><td>..</td><td>父节点</td></tr>" +
                "</table>" +
                "</body></html>";
    }

    private String getRegexReferenceContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>正则表达式参考</h2>" +
                "<p>正在打开 Regexr 在线测试工具...</p>" +
                "<p>如果浏览器未自动打开，请访问：</p>" +
                "<p><a href='https://regexr.com/'>https://regexr.com/</a></p>" +
                "<h3>常用模式速查</h3>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse;'>" +
                "<tr style='background: #f0f0f0;'><th>模式</th><th>说明</th></tr>" +
                "<tr><td>.</td><td>任意单个字符</td></tr>" +
                "<tr><td>\\d</td><td>数字</td></tr>" +
                "<tr><td>\\w</td><td>单词字符</td></tr>" +
                "<tr><td>\\s</td><td>空白字符</td></tr>" +
                "<tr><td>*</td><td>零次或多次</td></tr>" +
                "<tr><td>+</td><td>一次或多次</td></tr>" +
                "<tr><td>?</td><td>零次或一次</td></tr>" +
                "<tr><td>()</td><td>捕获组</td></tr>" +
                "</table>" +
                "</body></html>";
    }

    private String getDefaultContent() {
        return "<html><body style='font-family: Arial, sans-serif; padding: 10px;'>" +
                "<h2>帮助文档</h2>" +
                "<p>请从左侧导航树选择一个主题查看详细内容。</p>" +
                "</body></html>";
    }

    private void openExternalLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            // 如果无法打开浏览器，忽略错误
            System.out.println("无法打开链接: " + url);
        }
    }

    private void expandAllNodes(JTree tree, TreeNode node, TreePath path) {
        if (node.getChildCount() >= 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                TreeNode childNode = node.getChildAt(i);
                TreePath childPath = path.pathByAddingChild(childNode);
                expandAllNodes(tree, childNode, childPath);
            }
        }
        tree.expandPath(path);
    }
}
