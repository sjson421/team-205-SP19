package edu.northeastern.ccs.im.communications;

/**
 * Receiver interface defines a visitor pattern
 * The strategy pattern is used in Sender's part
 * The receiver use receiver patter. (because sender does not have chance to know the receiver's config)
 * <p>
 * Currenrly, I think visitor pattern will be used filter and following(specailly marked)
 * <p>
 * I will plan to extend socket channel as well to have this receiver.
 */
public interface IReceiver {
    void visit(Message message);
}
