package edu.northeastern.ccs.im.communications;

/**
 * Interface for Visitor pattern to accept
 */
public interface IMessageOut {
    /**
     * accept one receiver
     * @param receiver receiver
     */
    void accept(IReceiver receiver);
}
