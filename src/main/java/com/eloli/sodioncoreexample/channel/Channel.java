package com.eloli.sodioncoreexample.channel;

import com.eloli.sodioncore.channel.BadSignException;
import com.eloli.sodioncore.channel.MessageChannel;
import com.eloli.sodioncore.channel.util.ByteUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;


public class Channel {
    public void packet() throws BadSignException {
        byte[] key = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        MessageChannel clientChannel = new MessageChannel("test",
                ByteUtil.sha256(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)),
                ByteUtil.sha256(key))
                .registerClientPacket(TestPacket.class);
        MessageChannel serverChannel = new MessageChannel("test",
                ByteUtil.sha256(key),
                ByteUtil.sha256(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)))
                .registerClientPacket(TestPacket.class);
        TestPacket packet = new TestPacket();
        packet.sss = 233;
        packet.pp = new InnerPacket();
        packet.pp.sssub = "safdjkl";
        packet.pp.inss = new int[]{2, 3, 5, 9};
        byte[] bytes = clientChannel.getClientFactory(TestPacket.class).encode(packet);

        packet = (TestPacket) serverChannel.getClientFactory(bytes).parser(bytes);

        if (packet.sss != 233) {
            throw new RuntimeException("packet.sss failed");
        }
        if (!"safdjkl".equals(packet.pp.sssub)) {
            throw new RuntimeException("packet.pp.sssub failed");
        }
        if (!Arrays.equals(packet.pp.inss, new int[]{2, 3, 5, 9})) {
            throw new RuntimeException("packet.pp.inss failed");
        }
    }
}
