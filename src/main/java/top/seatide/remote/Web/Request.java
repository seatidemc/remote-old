package top.seatide.remote.Web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import top.seatide.remote.Main;
import top.seatide.remote.Utils.FakePlayer;

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
    public FakePlayer fakePlayer;

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
                this.setResponse("200 OK", build("ng", "Missing action type."));
            } else if (!object.has("params")) {
                this.setResponse("200 OK", build("ng", "Missing parameter (params)."));
            } else {
                var params = object.isNull("params") ? null : object.getJSONObject("params");
                var result = callHandler(params, object.getString("type"));
                this.setResponse(result.size() == 3 ? result.get(2) : "200 OK", build(result.get(0), result.get(1)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Server.log.warning("Failed to parse incoming request.");
            this.setResponse("200 OK", build("ng", "Failed to parse incoming request."));
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

    public List<String> callHandler(JSONObject params, String type) {
        var result = new ArrayList<String>();
        try {
            switch (type) {
                case "get": {
                    // TODO: get useful informations here
                    break;
                }

                default: {
                    result.add("ng");
                    result.add("Action not found.");
                }
            }
        } catch (JSONException e) {
            result.add("ng");
            result.add("Parameter is not fulfilled.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            result.add("ng");
            result.add("Runtime error: " + e.getMessage());
        }
        return result;
    }

    public String build(String status, String content) {
        return "{\"status\": \"" + status + "\", \"msg\": \"" + content + "\"}";
    }

    public void setResponse(String codeInfo, String res) {
        this.codeInfo = codeInfo;
        this.res = res;
    }
}