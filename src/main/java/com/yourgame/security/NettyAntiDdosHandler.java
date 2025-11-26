package com.yourgame.security;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

import nro.server.AntiDdosFrame; // Panel log UI
import com.yourgame.security.AntiDdosConfig;
import com.yourgame.security.AntiDdosManager;

public class NettyAntiDdosHandler extends ChannelInboundHandlerAdapter {

    private static final AttributeKey<String> ATTR_IP = AttributeKey.valueOf("remoteIp");

    private int getPort(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        }
        return -1;
    }

    private String extractIp(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress) ctx.channel().remoteAddress();
            return isa.getAddress().getHostAddress();
        }
        // fallback parse
        String s = String.valueOf(ctx.channel().remoteAddress());
        int slash = s.indexOf('/');
        int colon = s.lastIndexOf(':');
        return (slash != -1 && colon != -1) ? s.substring(slash + 1, colon) : s;
    }

    private boolean isProtectedPort(int port) {
        return AntiDdosConfig.PROTECTED_PORTS.contains(port);
    }

    private boolean isHeavy(Object msg) {
        // Tuỳ định nghĩa gói tin nặng
        return false;
    }

    // Ghi log ra console + UI panel
    private void logToUI(String msg) {
        System.out.println(msg);
        if (AntiDdosFrame.instance != null) {
            AntiDdosFrame.instance.appendLog(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = extractIp(ctx);
        int port = getPort(ctx);

        ctx.channel().attr(ATTR_IP).set(ip);

        logToUI("🔗 [AntiDDoS] New connection from " + ip + ":" + port +
                " | ENABLED=" + AntiDdosConfig.ENABLED +
                " | ProtectedPorts=" + AntiDdosConfig.PROTECTED_PORTS);

        if (AntiDdosConfig.ENABLED && isProtectedPort(port)) {
            AntiDdosManager.Decision d = AntiDdosManager.onConnect(ip, port);
            if (d == AntiDdosManager.Decision.BLOCK) {
                logToUI("⚠️ [AntiDDoS] BLOCKED connection from " + ip + ":" + port);
                ctx.close();
                return;
            }
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String ip = ctx.channel().attr(ATTR_IP).get();
        int port = getPort(ctx);

        if (AntiDdosConfig.ENABLED && isProtectedPort(port)) {
            AntiDdosManager.Decision d = AntiDdosManager.onMessage(ip, port, isHeavy(msg));
            if (d == AntiDdosManager.Decision.BLOCK) {
                logToUI("⛔ [AntiDDoS] BLOCKED message from " + ip + ":" + port);
                ctx.close();
                return;
            } else if (d == AntiDdosManager.Decision.CHALLENGE) {
                logToUI("⚠️ [AntiDDoS] CHALLENGE message from " + ip + ":" + port);
                Thread.sleep(50); // làm chậm nhẹ
            }
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String ip = ctx.channel().attr(ATTR_IP).get();
        int port = getPort(ctx);

        if (ip != null && AntiDdosConfig.ENABLED && isProtectedPort(port)) {
            AntiDdosManager.onDisconnect(ip);
            logToUI("❌ [AntiDDoS] Disconnected " + ip + ":" + port);
        }

        super.channelInactive(ctx);
    }
}
