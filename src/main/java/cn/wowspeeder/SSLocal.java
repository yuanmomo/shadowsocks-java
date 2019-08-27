package cn.wowspeeder;

import java.util.concurrent.TimeUnit;

import cn.wowspeeder.socks5.SocksServerHandler;
import cn.wowspeeder.ss.SSCommon;
import cn.wowspeeder.ss.SSLocalTcpProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class SSLocal {
    private static InternalLogger logger = InternalLoggerFactory.getInstance(SSLocal.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();


    public void startSingle(String socks5Server, Integer socks5Port, String server, Integer port, String password, String method) throws Exception {
        ServerBootstrap tcpBootstrap = new ServerBootstrap();

        //local socks5  server ,tcp
        tcpBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)// 读缓冲区为32k
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ctx) throws Exception {
                        logger.debug("channel initializer");
                        ctx.pipeline()
                                //timeout
                                .addLast("timeout", new IdleStateHandler(0, 0, SSCommon.TCP_PROXY_IDEL_TIME, TimeUnit.SECONDS) {
                                    @Override
                                    protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
                                        ctx.close();
                                        return super.newIdleStateEvent(state, first);
                                    }
                                });

                        //socks5
                        ctx.pipeline()
//                                .addLast(new LoggingHandler(LogLevel.INFO))
                                .addLast(new SocksPortUnificationServerHandler())
                                .addLast(SocksServerHandler.INSTANCE)
                                .addLast(new SSLocalTcpProxyHandler(server, port, method, password));
                    }
                });

//            logger.info("TCP Start At Port " + config.get_localPort());
        tcpBootstrap.bind(socks5Server, socks5Port).sync();

        //local socks5  server ,udp
//        Bootstrap udpBootstrap = new Bootstrap();
//        udpBootstrap.group(bossGroup).channel(NioDatagramChannel.class)
//                .option(ChannelOption.SO_BROADCAST, false)// 支持广播
//                .option(ChannelOption.SO_RCVBUF, 64 * 1024)// 设置UDP读缓冲区为64k
//                .option(ChannelOption.SO_SNDBUF, 64 * 1024)// 设置UDP写缓冲区为64k
//                .handler(new ChannelInitializer<NioDatagramChannel>() {
//
//                    @Override
//                    protected void initChannel(NioDatagramChannel ctx) throws Exception {
//                        ctx.pipeline()
////                                .addLast(new LoggingHandler(LogLevel.INFO))
//                                .addLast(new SSLocalUdpProxyHandler(server, port, method, password))
//                        ;
//                    }
//                })
//        ;
//        udpBootstrap.bind(socks5Server, socks5Port).sync();

        logger.info("listen at {} with password:[{}],method:[{}]", socks5Port,password,method);
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("Stop Server!");
    }

    public static void main(String[] args) throws Exception {
        try {
            new SSLocal().startSingle("127.0.0.1", 3086, "127.0.0.1", 20000, "20000", "aes-256-cfb");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
