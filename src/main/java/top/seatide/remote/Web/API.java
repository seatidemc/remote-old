package top.seatide.remote.Web;

import java.net.ServerSocket;
import java.net.Socket;

import top.seatide.remote.Utils.Files;

import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class API {
    public Server server;
    public Thread thread;

    public int getServerPort() {
        return server.serverPort;
    }

    public void startServer() {
        var poolSize = Files.cfg.getInt("pool-size");
        if (poolSize == 0)
            poolSize = 15;
        server = new Server(poolSize);
        thread = new Thread(server);
        thread.start();
    }

    public void stopServer() {
        if (!server.stopped) {
            server.stop();
        }
    }
}

class WorkerRunnable implements Runnable {
    public Socket clientSocket = null;
    public String serverText = null;

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText = serverText;
    }

    public void run() {
        try {
            new Request(clientSocket).handle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Server implements Runnable {
    public int serverPort = 26656;
    public ServerSocket serverSocket = null;
    public boolean stopped = false;
    public Thread runningThread = null;
    public ExecutorService pool;
    public static Logger log = Logger.getLogger("HTTPServer");

    public Server(int poolSize) {
        var port = Files.cfg.getInt("port");
        if (port != 0)
            serverPort = port;
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    log.warning("无法获取接收请求，服务器已关闭。");
                    break;
                }
                throw new RuntimeException("无法获取客户端请求：", e);
            }
            this.pool.execute(new WorkerRunnable(clientSocket, "HTTPServer"));
        }
        this.pool.shutdown();
        log.info("HTTP 服务器已关闭。");
    }

    private synchronized boolean isStopped() {
        return this.stopped;
    }

    public synchronized void stop() {
        this.stopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("关闭服务器失败：", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("无法打开相应端口：", e);
        }
    }
}