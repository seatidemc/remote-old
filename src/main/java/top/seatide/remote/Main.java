package top.seatide.remote;

import org.bukkit.plugin.java.JavaPlugin;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.LogUtil;
import top.seatide.remote.Web.API;
import top.seatide.remote.Web.Request;

public final class Main extends JavaPlugin {
    public API api;

    @Override
    public void onEnable() {
        Request.setPlugin(this);
        api = new API();
        Files.init(this);
        try {
            api.startServer();
            LogUtil.success("HTTP 服务器已在端口 " + api.getServerPort() + " 开启。");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("无法启动 HTTP 服务器。");
        }
    }

    @Override
    public void onDisable() {
        api.stopServer();
        LogUtil.info("关闭 HTTP 服务器中...");
    }
}