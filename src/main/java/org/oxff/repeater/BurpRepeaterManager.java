package org.oxff.repeater;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolSource;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedObject;
import org.oxff.repeater.config.ConfigTab;
import org.oxff.repeater.menu.ContextMenuProvider;
import org.oxff.repeater.rename.RepeaterRenameHandler;
import org.oxff.repeater.storage.BurpStorage;
import org.oxff.repeater.storage.SQLiteStorage;

public class BurpRepeaterManager implements BurpExtension {

    private static MontoyaApi api;
    private static PersistedObject persistedData;
    private SQLiteStorage sqliteStorage;
    private BurpStorage burpStorage;
    private RepeaterRenameHandler renameHandler;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;

        // 设置扩展信息
        api.extension().setName("helloRepeater");
        api.logging().logToOutput("helloRepeater 插件初始化中...");

        // 初始化存储
        String burpDir = System.getProperty("user.dir");
        try {
            this.sqliteStorage = new SQLiteStorage(burpDir);
            api.logging().logToOutput("SQLite 数据库初始化成功: " + burpDir + "/repeater_manager.db");
        } catch (Exception e) {
            api.logging().logToError("SQLite 数据库初始化失败: " + e.getMessage());
            api.logging().logToOutput("插件将以有限功能模式运行");
            // 创建一个空的存储实现或处理异常情况
            this.sqliteStorage = null;
        }
        
        this.burpStorage = new BurpStorage(api, sqliteStorage);

        // 初始化重命名处理器
        this.renameHandler = new RepeaterRenameHandler(api, sqliteStorage);

        // 注册配置Tab
        ConfigTab configTab = new ConfigTab(api, sqliteStorage, burpStorage, renameHandler);
        api.userInterface().registerSuiteTab("helloRepeater", configTab.getUI());

        // 注册右键菜单提供者
        api.userInterface().registerContextMenuItemsProvider(
            new ContextMenuProvider(api, burpStorage, renameHandler)
        );

        // 注册HTTP处理器来监听发送到Repeater的事件
        api.http().registerHttpHandler(new HttpHandler() {
            @Override
            public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
                // 检查是否是从Proxy/历史记录发送到Repeater的请求
                ToolSource toolSource = requestToBeSent.toolSource();
                if (toolSource.isFromTool(ToolType.REPEATER)) {
                    // 这是发送到Repeater的请求，在响应后处理
                    // 实际上需要在请求发送后处理，这里只是标记
                }
                return RequestToBeSentAction.continueWith(requestToBeSent);
            }

            @Override
            public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
                return ResponseReceivedAction.continueWith(responseReceived);
            }
        });

        api.logging().logToOutput("helloRepeater 插件初始化完成！");
    }

    public static MontoyaApi getApi() {
        return api;
    }
}