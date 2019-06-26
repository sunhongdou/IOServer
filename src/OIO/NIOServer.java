package OIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer {
    private Selector selector;

    public void initServer(int port) throws IOException {
        //获得一个serverSocket通道
        ServerSocketChannel serverchannel = ServerSocketChannel.open();
        //设置通道为非阻塞
        serverchannel.configureBlocking(false);
        //将通道对应的ServerSocket绑定到端口上
        serverchannel.socket().bind(new InetSocketAddress(port));
        //获得一个通道管理器
        this.selector= Selector.open();
        //将通道管理器和该通道绑定，并未该通道注册SelectionKey.OP_ACCEPT事件
        //当事件到达时，selector.select()会返回，否则一致阻塞
        serverchannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void listen() throws IOException {
        System.out.println("server start success...");
        while(true){
            //当注册的事件到达时，返回，否则一致阻塞
            selector.select();
            //获得selector中的选中的迭代器，选中项为注册的事件
            Iterator iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = (SelectionKey) iterator.next();
                //删除已选中的key,防止重复处理
                iterator.remove();
                handler(key);
            }
        }
    }

    private void handler(SelectionKey key) throws IOException {
        if (key.isAcceptable()){
            handlerAccept(key);
        }else{
            handlerRead(key);
        }
    }

    /**
     * 处理读事件
     * @param key
     * @throws IOException
     */
    private void handlerRead(SelectionKey key) throws IOException{
        //服务器可读取消息
        SocketChannel channel = (SocketChannel) key.channel();
        //创建读取的缓存
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        if (read>0){
            byte[] data = buffer.array();
            String msg = new String(data).trim();
            System.out.println("server receive msg :" + msg);
            ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
            channel.write(outBuffer);
        }else{
            System.out.println("client closed...");
            key.cancel();
        }




    }

    /**
     * 处理连接请求
     * @param key
     * @throws IOException
     */
    private void handlerAccept(SelectionKey key) throws  IOException{
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        //获得和客户端连接的通道
        SocketChannel channel = server.accept();
        //设置为非阻塞通道
        channel.configureBlocking(false);

        System.out.println("A new client...");
        channel.register(this.selector,SelectionKey.OP_READ);
    }

    public static  void main(String[] args) throws IOException {
        NIOServer server = new NIOServer();
        server.initServer(8800);
        server.listen();
    }
}
