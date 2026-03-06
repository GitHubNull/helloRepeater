package org.oxff.repeater.rename;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.repeater.Repeater;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.oxff.repeater.model.RenameRule;
import org.oxff.repeater.storage.SQLiteStorage;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Repeater标签页重命名处理器
 */
public class RepeaterRenameHandler {

    private final MontoyaApi api;
    private final SQLiteStorage sqliteStorage;
    private int repeaterCounter = 1;

    public RepeaterRenameHandler(MontoyaApi api, SQLiteStorage sqliteStorage) {
        this.api = api;
        this.sqliteStorage = sqliteStorage;
    }

    /**
     * 根据规则生成新的标签页标题
     */
    public String generateTitle(HttpRequest request, int repeaterTabId) {
        try {
            List<RenameRule> rules = sqliteStorage.loadEnabledRules();
            String extractedName = null;

            for (RenameRule rule : rules) {
                extractedName = applyRule(rule, request);
                if (extractedName != null && !extractedName.isEmpty()) {
                    break;
                }
            }

            // 如果没有规则匹配，使用默认路径处理
            if (extractedName == null || extractedName.isEmpty()) {
                extractedName = extractDefaultPath(request.path());
            }

            // 限制长度
            if (extractedName.length() > 16) {
                extractedName = extractedName.substring(extractedName.length() - 16);
            }

            return repeaterTabId + "-" + extractedName;

        } catch (SQLException e) {
            api.logging().logToError("加载重命名规则失败: " + e.getMessage());
            return repeaterTabId + "-" + extractDefaultPath(request.path());
        }
    }

    /**
     * 应用单个规则
     */
    private String applyRule(RenameRule rule, HttpRequest request) {
        switch (rule.getType()) {
            case PATH_SUBSTRING:
                return applyPathSubstringRule(rule, request.path());
            case PATH_REGEX:
                return applyPathRegexRule(rule, request.path());
            case BODY_REGEX:
                return applyBodyRegexRule(rule, request);
            case BODY_JSON_PATH:
                return applyBodyJsonPathRule(rule, request);
            case BODY_XPATH:
                return applyBodyXPathRule(rule, request);
            default:
                return null;
        }
    }

    private String applyPathSubstringRule(RenameRule rule, String path) {
        int start = rule.getPathStartIndex();
        int end = rule.getPathEndIndex();

        if (start < 0) {
            start = Math.max(0, path.length() - 16);
        }
        if (end < 0 || end > path.length()) {
            end = path.length();
        }

        if (start < path.length() && start < end) {
            return path.substring(start, end);
        }
        return null;
    }

    private String applyPathRegexRule(RenameRule rule, String path) {
        try {
            Pattern pattern = Pattern.compile(rule.getPattern());
            Matcher matcher = pattern.matcher(path);
            if (matcher.find() && matcher.groupCount() >= 1) {
                return matcher.group(1);
            }
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception e) {
            api.logging().logToError("正则规则执行失败: " + rule.getPattern());
        }
        return null;
    }

    private String applyBodyRegexRule(RenameRule rule, HttpRequest request) {
        String body = request.bodyToString();
        if (body == null || body.isEmpty()) {
            return null;
        }

        try {
            Pattern pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            if (matcher.find() && matcher.groupCount() >= 1) {
                return matcher.group(1);
            }
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception e) {
            api.logging().logToError("Body正则规则执行失败: " + rule.getPattern());
        }
        return null;
    }

    private String applyBodyJsonPathRule(RenameRule rule, HttpRequest request) {
        String body = request.bodyToString();
        if (body == null || body.isEmpty()) {
            return null;
        }

        try {
            Object result = JsonPath.read(body, rule.getPattern());
            if (result != null) {
                return result.toString();
            }
        } catch (PathNotFoundException e) {
            // JSON Path未找到，忽略
        } catch (Exception e) {
            api.logging().logToError("JSON Path规则执行失败: " + rule.getPattern());
        }
        return null;
    }

    private String applyBodyXPathRule(RenameRule rule, HttpRequest request) {
        String body = request.bodyToString();
        if (body == null || body.isEmpty()) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(body)));

            XPath xpath = XPathFactory.newInstance().newXPath();
            Object result = xpath.evaluate(rule.getPattern(), doc, XPathConstants.NODESET);

            if (result instanceof NodeList) {
                NodeList nodes = (NodeList) result;
                if (nodes.getLength() > 0) {
                    return nodes.item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            api.logging().logToError("XPath规则执行失败: " + rule.getPattern());
        }
        return null;
    }

    private String extractDefaultPath(String path) {
        if (path.length() > 16) {
            return path.substring(path.length() - 16);
        }
        return path;
    }

    /**
     * 应用规则到所有Repeater标签页
     */
    public void applyToAllRepeaterTabs() {
        // Burp Montoya API目前没有直接获取所有Repeater标签页的方法
        // 这个功能需要用户在配置界面手动触发，对新标签页生效
        api.logging().logToOutput("规则已应用到所有新的Repeater标签页");
    }

    public int getNextRepeaterId() {
        return repeaterCounter++;
    }
}