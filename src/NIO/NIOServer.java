package NIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOServer {
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception{
        //创建一个线程池
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        //创建一个socket服务，监听10101端口
        ServerSocket server = new ServerSocket(10101);
        System.out.println("server starting ....");
        while(true){
            //获取一个套接字，阻塞
            final Socket socket = server.accept();
            System.out.println("A new client...");
            //用线程池可以有多个客户端连接，即每一个客户端分配一个线程，非常消耗性能
            newCachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //业务处理
                    handler(socket);
                }
            });

        }
    }

    public static  void handler(Socket socket){
        byte[] bytes = new byte[1024];
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
            while (true){
                //读取数据 阻塞
                int read = inputStream.read(bytes);
                if (read != -1){
                    System.out.println(new String(bytes,0,read));
                }else{
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                System.out.println("socket closed ...");
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
