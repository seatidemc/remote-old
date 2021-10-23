package top.seatide.remote.Web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.BanList.Type;
import org.json.JSONException;
import org.json.JSONObject;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import top.seatide.remote.Main;
import top.seatide.remote.Utils.X;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.Tick;
import top.seatide.remote.Utils.Vault;

public class Request {
    public InputStream input;
    public OutputStream output;
    public BufferedReader in;
    public BufferedWriter out;
    public StringBuilder body;
    public StringBuilder header;
    public String codeInfo = "200 OK";
    public String res = "";
    public static Main plugin;
    private static final String ok = "ok";
    private static final String ng = "ng";

    public Request(Socket socket) throws IOException {
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(input));
        out = new BufferedWriter(new OutputStreamWriter(output));
        body = new StringBuilder();
        header = new StringBuilder();
    }

    public static void setPlugin(Main plug) {
        plugin = plug;
    }

    public void handle() throws IOException {
        String headerRaw;
        try {
            while ((headerRaw = in.readLine()).length() != 0) {
                header.append(headerRaw);
            }
        } catch (NullPointerException e) {
            // nothing
        }
        while (in.ready()) {
            body.append((char) in.read());
        }
        try {
            var object = new JSONObject(body.toString());
            if (!object.has("type")) {
                this.setResponse("200 OK", build(ng, "Missing action type."));
            } else if (!object.has("params")) {
                this.setResponse("200 OK", build(ng, "Missing parameter (params)."));
            } else {
                var params = object.isNull("params") ? null : object.getJSONObject("params");
                var result = callHandler(params, object.getString("type"));
                this.setResponse("200 OK", result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Server.log.warning("Failed to parse incoming request.");
            this.setResponse("200 OK", build(ng, "Failed to parse incoming request."));
        }
        out.write("HTTP/1.1 " + codeInfo + "\r\n");
        out.write("Content-Type: application/json;charset=UTF-8\r\n");
        out.write("Date: " + (new Date()).toString() + "\r\n");
        out.write("\r\n");
        out.write(res);
        this.setResponse("200 OK", "");
        out.flush();
        out.close();
        in.close();
        input.close();
        output.close();
    }

    public String callHandler(JSONObject params, String type) {
        try {
            switch (type) {
            case "get": {
                var name = X.getStringSafe(params, "name");
                switch (name) {
                case "ram": {
                    var runtime = Runtime.getRuntime();
                    var json = new JSONObject();
                    json.put("used", (runtime.maxMemory() - runtime.freeMemory()) / 1024L / 1024L);
                    json.put("max", runtime.maxMemory() / 1024L / 1024L);
                    return build(ok, json);
                }

                case "tps": {
                    return build(ok, String.valueOf(Math.round(Tick.getTPS())));
                }

                default: {
                    return build(ng, "Value not found for name '" + name + "'.");
                }
                }
            }

            case "do": {
                var action = X.getStringSafe(params, "action");
                if (action.isEmpty())
                    return build(ng, "Missing action name (action).");
                var target = X.getStringSafe(params, "target");

                switch (action) {
                case "player.deposit":
                case "player.withdraw": {
                    if (!Main.hasVault)
                        return build(ng, "Vault not enabled on the server.");
                    var amount = params.getDouble("amount");
                    if (amount <= 0)
                        return build(ng, "Amount cannot be 0 or under.");
                    var player = Bukkit.getPlayer(target);
                    if (player == null)
                        return build(ng, "Player named '" + target + "' not found.");
                    EconomyResponse result;
                    if (action.equals("players.deposit")) {
                        result = Vault.eco.depositPlayer(player, amount);
                    } else {
                        result = Vault.eco.withdrawPlayer(player, amount);
                    }
                    if (result.type == ResponseType.SUCCESS)
                        return build(ok);
                    if (result.type == ResponseType.FAILURE)
                        return build(ng, "Error: " + result.errorMessage);
                    return build(ng, "Unknown failure.");
                }

                case "player.ban": {
                    if (Bukkit.getPlayer(target) == null)
                        return build(ng, "Player named '" + target + "' not found.");
                    var reason = X.getStringSafeNull(params, "reason");
                    var expires = X.getStringSafe(params, "expires");
                    var source = X.getStringSafeNull(params, "source");
                    Date expiresDate = null;
                    if (!expires.isEmpty()) {
                        if (!X.isPD(expires))
                            return build(ng, "Invalid expiration format.");
                        expiresDate = X.getDateByPD(expires);
                    }
                    Bukkit.getBanList(Type.NAME).addBan(target, reason, expiresDate, source);
                    return build(ok, "Banned '" + target + "'");
                }

                case "player.unban": {
                    if (Bukkit.getPlayer(target) == null)
                        return build(ng, "Player named '" + target + "' not found.");
                    Bukkit.getBanList(Type.NAME).pardon(target);
                    return build(ok, "Unbanned '" + target + "'.");
                }

                case "server.stop": {
                    Bukkit.shutdown();
                    return build(ok);
                }

                case "server.broadcast": {
                    var message = X.getStringSafe(params, "message");
                    if (message.isEmpty())
                        return build(ng, "Empty message.");
                    var prefix = Files.cfg.getString("broadcast-prefix");
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            prefix == null ? "" : prefix + "&r " + message));
                    return build(ok);
                }
                }
            }

            default: {
                return build(ng, "Action not found.");
            }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return build(ng, "Missing parameters.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return build(ng, "Runtime error: " + e.getMessage());
        }
    }

    public String build(String status, JSONObject content) {
        var json = new JSONObject();
        json.put("status", status);
        json.put("msg", content);
        return json.toString();
    }

    public String build(String status, String content) {
        var json = new JSONObject();
        json.put("status", status);
        json.put("msg", content);
        return json.toString();
    }

    public String build(String status) {
        var json = new JSONObject();
        json.put("status", status);
        json.put("msg", "None.");
        return json.toString();
    }

    public void setResponse(String codeInfo, String res) {
        this.codeInfo = codeInfo;
        this.res = res;
    }
}