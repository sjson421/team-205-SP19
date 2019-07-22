package edu.northeastern.ccs.im.communications;

/**
 * This interface defines Strategy pattern. The motivation of using this pattern is
 * from raw message to the message that should send out, the operation varies for differnt
 * situations. Rather than have so many different message sub-classes, programmer should only
 * modify different Strategies, it helps people to maintian a relatively smaller class.
 * <p>
 * My opinion for Potential Use cases here is expiration and encryption. (can be changed in future)
 */
public interface IMessageStrategy {
    String getOutputText(String raw);

    /**
     * has receiver read it, has time passed, has sender voided the msg
     * input parameter may change when message have more filed
     * @return true if message should expire
     */
    boolean isExpired(Message msg);
}
