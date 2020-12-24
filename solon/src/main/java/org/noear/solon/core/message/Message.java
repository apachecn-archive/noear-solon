package org.noear.solon.core.message;

import org.noear.solon.annotation.Note;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;

/**
 * SocketD 消息包（实现 Message + Listener 架构）
 *
 * 格式一：{flag + body}
 * 格式二：{flag + key + resourceDescriptor + header + body}
 *
 * @see Listener#onMessage(Session, Message, boolean)
 * @author noear
 * @since 1.0
 * */
public class Message {
    /**
     * 1.消息标志
     */
    private final int flag;

    @Note("1.消息标志")
    public int flag() {
        return flag;
    }

    /**
     * 2.消息key
     */
    private final String key;

    @Note("2.消息key")
    public String key() {
        return key;
    }

    /**
     * 3.资源描述
     */
    private final String resourceDescriptor;

    @Note("3.资源描述")
    public String resourceDescriptor() {
        return resourceDescriptor;
    }


    private final String header;

    @Note("4.消息头")
    public String header() {
        return header;
    }


    /**
     * 5.消息主体
     */
    private final byte[] body;

    @Note("5.消息主体")
    public byte[] body() {
        return body;
    }

    public String bodyAsString() {
        if (body == null) {
            return null;
        } else {
            return new String(body, charset);
        }
    }

    //////////////////////////////////////////

    /**
     * 消息转换
     */
    public <T> T map(Function<Message, T> mapper) {
        return mapper.apply(this);
    }

    //////////////////////////////////////////

    private static final byte[] EMPTY_B = new byte[]{};
    private static final String EMPTY_S = "";

    public Message(int flag, String key, String resourceDescriptor, String header, byte[] body) {
        this.flag = flag;
        this.key = (key == null ? EMPTY_S : key);
        this.resourceDescriptor = (resourceDescriptor == null ? EMPTY_S : resourceDescriptor);
        this.header = (header == null ? EMPTY_S : header);
        this.body = (body == null ? EMPTY_B : body);
    }

    public Message(int flag, String key, String resourceDescriptor, byte[] body) {
        this(flag, key, resourceDescriptor, null, body);
    }

    public Message(int flag, String key, byte[] body) {
        this(flag, key, null, null, body);
    }

    public Message(int flag, byte[] body) {
        this(flag, null, null, null, body);
    }

    @Override
    public String toString() {
        return "Message{" +
                "flag=" + flag() +
                ", key='" + key() + '\'' +
                ", resourceDescriptor='" + resourceDescriptor() + '\'' +
                ", header='" + header() + '\'' +
                ", body='" + bodyAsString() + '\'' +
                '}';
    }

    /**
     * 消息编码
     */
    private Charset charset = StandardCharsets.UTF_8;

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    //////////////////////////////////////////

    private boolean _handled;

    public void setHandled(boolean handled) {
        _handled = handled;
    }

    public boolean getHandled() {
        return _handled;
    }

    //////////////////////////////////////////
}
