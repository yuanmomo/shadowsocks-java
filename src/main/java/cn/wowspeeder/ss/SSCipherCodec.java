package cn.wowspeeder.ss;

import java.util.List;

import cn.wowspeeder.encryption.CryptUtil;
import cn.wowspeeder.encryption.ICrypt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


@ChannelHandler.Sharable
public class SSCipherCodec extends MessageToMessageCodec<Object, Object> {
    private static InternalLogger logger = InternalLoggerFactory.getInstance(SSCipherCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf;
        if (msg instanceof DatagramPacket) {
            buf = ((DatagramPacket) msg).content();
        } else if (msg instanceof ByteBuf) {
            buf = (ByteBuf) msg;
        } else {
            throw new Exception("unsupported msg type:" + msg.getClass());
        }

        logger.debug("encode msg size before: " + buf.readableBytes());
        ICrypt _crypt = ctx.channel().attr(SSCommon.CIPHER).get();
//        Boolean isUdp = ctx.channel().attr(SSCommon.IS_UDP).get();
        byte[] encryptedData = CryptUtil.encrypt(_crypt, buf);
        if (encryptedData == null || encryptedData.length == 0) {
            return;
        }
//        logger.debug("encode after encryptedData size:{}",encryptedData.length);
        out.add(Unpooled.directBuffer(encryptedData.length).writeBytes(encryptedData));
        logger.debug("encode done after: {} ",buf.readableBytes());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf;
        if (msg instanceof DatagramPacket) {
            buf = ((DatagramPacket) msg).content();
        } else if (msg instanceof ByteBuf) {
            buf = (ByteBuf) msg;
        } else {
            throw new Exception("unsupported msg type:" + msg.getClass());
        }
        logger.debug("decode msg size before: " + buf.readableBytes());
        ICrypt _crypt = ctx.channel().attr(SSCommon.CIPHER).get();
        byte[] data = CryptUtil.decrypt(_crypt, buf);
        if (data == null || data.length == 0) {
            return;
        }
        logger.debug((ctx.channel().attr(SSCommon.IS_UDP).get() ? "(UDP)" : "(TCP)") + " decode after:" + data.length);
//        logger.debug("channel id:{}  decode text:{}", ctx.channel().id(), new String(data, Charset.forName("gbk")));
        buf.retain().clear().writeBytes(data);
        logger.debug("decode msg size after: " + buf.readableBytes());
        out.add(msg);//
    }
}
