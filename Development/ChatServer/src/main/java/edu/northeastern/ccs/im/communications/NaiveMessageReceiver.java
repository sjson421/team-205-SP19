package edu.northeastern.ccs.im.communications;

public class NaiveMessageReceiver implements IReceiver {
    /**
     * Extend socket, attach receiver, move work of sending message here.
     *
     * @param message receiver visit the message
     */
    @Override
    public void visit(Message message) {
        // Do nothing for now before socket channel is extended
    }
}
