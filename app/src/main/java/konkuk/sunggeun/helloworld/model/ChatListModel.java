package konkuk.sunggeun.helloworld.model;

public class ChatListModel {
    private String desUid;
    private boolean isRoom;

    public ChatListModel() {
        isRoom = false;
    }

    public ChatListModel(String desUid, boolean isRoom) {
        this.desUid = desUid;
        this.isRoom = isRoom;
    }

    public String getDesUid() {
        return desUid;
    }

    public void setDesUid(String desUid) {
        this.desUid = desUid;
    }

    public boolean isRoom() {
        return isRoom;
    }

    public void setRoom(boolean room) {
        isRoom = room;
    }
}
