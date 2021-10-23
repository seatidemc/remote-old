package top.seatide.remote;

import org.bukkit.plugin.java.JavaPlugin;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.LogUtil;
import top.seatide.remote.Utils.Tick;
import top.seatide.remote.Utils.Vault;
import top.seatide.remote.Utils.X;
import top.seatide.remote.Web.API;
import top.seatide.remote.Web.Request;

public final class Main extends JavaPlugin {
    public API api;
    public Tick tick;
    public static boolean hasVault = false;

    @Override
    public void onEnable() {
        Request.setPlugin(this);
        api = new API();
        tick = new Tick();
        tick.start(this);
        Files.init(this);
        X.init(this);
        if (Vault.setup()) {
            LogUtil.success("Vault 已载入。");
            hasVault = true;
        } else {
            LogUtil.error("Vault 无法载入，可能无法安装或出现问题。");
        }
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