package com.chat.model;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.*;

/**
 * 自定义编码器详见文档 https://www.cnblogs.com/houzheng/p/11279654.html
 */
public class UserDtoCodec implements MessageCodec<UserDto, UserDto> {

    /**
     * 将消息实体封装到Buffer用于传输
     * 实现方式：使用对象流从对象中获取Byte数组然后追加到Buffer
     */
    @Override
    public void encodeToWire(Buffer buffer, UserDto orderMessage) {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream o = new ObjectOutputStream(b)){
            o.writeObject(orderMessage);
            o.close();
            buffer.appendBytes(b.toByteArray());
        } catch (IOException e) { e.printStackTrace(); }
    }
    //从Buffer中获取消息对象
    @Override
    public UserDto decodeFromWire(int pos, Buffer buffer) {
        final ByteArrayInputStream b = new ByteArrayInputStream(buffer.getBytes());
        UserDto msg = null;
        try (ObjectInputStream o = new ObjectInputStream(b)) {
            msg = (UserDto) o.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return msg;
    }
    //消息转换
    @Override
    public UserDto transform(UserDto userDto) {
        System.out.println("消息转换---");//可对接受消息进行转换,比如转换成另一个对象等

        return userDto;
    }
    @Override
    public String name() { return "userDto"; }
    //识别是否是用户自定义编解码器,通常为-1
    @Override
    public byte systemCodecID() { return -1; }
    public static MessageCodec create() {
        return new UserDtoCodec();
    }
}
