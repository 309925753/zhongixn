package fm.jiecao.jcvideoplayer_lib;


public class MessageStateEvent {
    public final int state;
    public final String packetId;

    public MessageStateEvent(int state, String packetId) {
        this.state = state;
        this.packetId = packetId;
    }
}