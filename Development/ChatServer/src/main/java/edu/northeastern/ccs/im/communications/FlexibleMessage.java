package edu.northeastern.ccs.im.communications;

import java.util.Map;

/**
 * Flexible message extends from message, with an extra strategy filed to get Text
 * It also implements MessageOut Interfeace to accept a message receiver to visit it.
 */
public class FlexibleMessage extends Message implements IMessageOut {
    private IMessageStrategy strategy;

    /**
     * When fields become larger, it is good to use builder pattern
     * For now, I think this way is good enough.
     *
     * @param strategy strategy to use
     */
    public void setStrategy(IMessageStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * @param handle    Handle for the type of message being created.
     * @param srcName   Name of the individual sending this message
     * @param text      Text of the instant message
     * @param msgToInfo send msg to group or user
     */
    protected FlexibleMessage(MessageType handle, String srcName, String text, Map<String, String> msgToInfo) {
        super(handle, srcName, text, msgToInfo);
    }

    /**
     * different receiver should have their different bahavior
     *
     * @param receiver the visitor to look into the msg
     */
    @Override
    public void accept(IReceiver receiver) {
        receiver.visit(this);
    }

    /**
     * get text for different strategy
     *
     * @return text of the message, decided by sender
     */
    @Override
    public String getText() {
        return strategy.getOutputText(super.getText());
    }
}
