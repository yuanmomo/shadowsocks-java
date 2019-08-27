package cn.wowspeeder;

import java.util.concurrent.TimeUnit;

import cn.wowspeeder.encryption.CryptFactory;
import cn.wowspeeder.encryption.ICrypt;
import cn.wowspeeder.ss.SSCipherCodec;
import cn.wowspeeder.ss.SSCommon;
import cn.wowspeeder.ss.SSProtocolCodec;
import cn.wowspeeder.ss.SSServerCheckerReceive;
import cn.wowspeeder.ss.SSServerCheckerSend;
import cn.wowspeeder.ss.SSServerTcpProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class SSServer {
    private static InternalLogger logger = InternalLoggerFactory.getInstance(SSServer.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(2);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup(32);

    public void startSingle(Integer port, String password, String method) throws Exception {
        ServerBootstrap tcpBootstrap = new ServerBootstrap();
        tcpBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 5120)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)// 读缓冲区为32k
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, false)
                .childOption(ChannelOption.SO_LINGER, 1) //关闭时等待1s发送关闭
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ctx) throws Exception {
//                            ctx.pipeline().addLast(new SSTcpHandler(config));
                        logger.debug("channel initializer");

                        ctx.attr(SSCommon.IS_UDP).set(false);

                        ICrypt _crypt = CryptFactory.get(method, password);
                        assert _crypt != null;
                        _crypt.isForUdp(false);
                        ctx.attr(SSCommon.CIPHER).set(_crypt);

                        ctx.pipeline()
                                //timeout
                                .addLast("timeout", new IdleStateHandler(0, 0, SSCommon.TCP_PROXY_IDEL_TIME, TimeUnit.SECONDS) {
                                    @Override
                                    protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
                                        ctx.close();
                                        return super.newIdleStateEvent(state, first);
                                    }
                                });

                        //obfs pugin
//                        List<ChannelHandler> obfsHandlers = ObfsFactory.getObfsHandler(obfs);
//                        if (obfsHandlers != null) {
//                            for (ChannelHandler obfsHandler : obfsHandlers) {
//                                ctx.pipeline().addLast(obfsHandler);
//                            }
//                        }
                        //ss
                        ctx.pipeline()
//                                .addLast(new LoggingHandler(LogLevel.INFO))
                                //ss-in
                                .addLast("ssCheckerReceive", new SSServerCheckerReceive())
                                //ss-out
                                .addLast("ssCheckerSend", new SSServerCheckerSend())
                                //ss-cypt
                                .addLast("ssCipherCodec", new SSCipherCodec())
                                //ss-protocol
                                .addLast("ssProtocolCodec", new SSProtocolCodec())
                                //ss-proxy
                                .addLast("ssTcpProxy", new SSServerTcpProxyHandler())
                        ;
                    }
                });

//            logger.info("TCP Start At Port " + config.get_localPort());
        tcpBootstrap.bind(port).sync();

        //udp server
//        Bootstrap udpBootstrap = new Bootstrap();
//        udpBootstrap.group(bossGroup).channel(NioDatagramChannel.class)
//                .option(ChannelOption.SO_BROADCAST, true)// 支持广播
//                .option(ChannelOption.SO_RCVBUF, 64 * 1024)// 设置UDP读缓冲区为64k
//                .option(ChannelOption.SO_SNDBUF, 64 * 1024)// 设置UDP写缓冲区为64k
//                .handler(new ChannelInitializer<NioDatagramChannel>() {
//
//                    @Override
//                    protected void initChannel(NioDatagramChannel ctx) throws Exception {
//
//                        ctx.attr(SSCommon.IS_UDP).set(true);
//
//                        ICrypt _crypt = CryptFactory.get(method, password);
//                        assert _crypt != null;
//                        _crypt.isForUdp(true);
//                        ctx.attr(SSCommon.CIPHER).set(_crypt);
//
//                        ctx.pipeline()
////                                .addLast(new LoggingHandler(LogLevel.INFO))
//                                // in
//                                .addLast("ssCheckerReceive", new SSServerCheckerReceive())
//                                // out
//                                .addLast("ssCheckerSend", new SSServerCheckerSend())
//                                //ss-cypt
//                                .addLast("ssCipherCodec", new SSCipherCodec())
//                                //ss-protocol
//                                .addLast("ssProtocolCodec", new SSProtocolCodec())
//                                //proxy
//                                .addLast("ssUdpProxy", new SSServerUdpProxyHandler())
//                        ;
//                    }
//                })
//        ;
//        udpBootstrap.bind(server, port).sync();
        logger.info("listen at {} with password:[{}],method:[{}]", port,password,method);
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


    public static void main(String[] args) throws InterruptedException {
        try {
            new SSServer().startSingle(20000,"20000","aes-256-cfb");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
