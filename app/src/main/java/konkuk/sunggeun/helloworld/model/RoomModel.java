package konkuk.sunggeun.helloworld.model;

public class RoomModel {
    private String roomName;
    private String interesting;

    public RoomModel(String roomName, String interesting) {
        this.roomName = roomName;
        this.interesting = interesting;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setInteresting(String interesting) {
        this.interesting = interesting;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getInteresting() {
        return interesting;
    }
}
