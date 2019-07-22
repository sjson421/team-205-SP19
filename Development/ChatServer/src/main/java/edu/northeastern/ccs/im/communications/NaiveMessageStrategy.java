package edu.northeastern.ccs.im.communications;

/**
 * Naive Message Strategy just send the orignal information
 * and will not expire. It is ttrival and the default behaviour. However, it could be a example and test
 * strategy pattern.
 */
public class NaiveMessageStrategy implements IMessageStrategy {
    /**
     * There is no encryption or modification
     *
     * @param raw Original message
     * @return orginal message
     */
    @Override
    public String getOutputText(String raw) {
        return raw;
    }

    /**
     * It depends on has receiver read it, has time passed, has sender voided the msg.
     * input parameter may change when message have more filed
     *
     * @param msg
     * @return false will not expires
     */
    @Override
    public boolean isExpired(Message msg) {
        return false;
    }
}
