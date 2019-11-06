package konkuk.sunggeun.helloworld.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    private String sendUid;
    private String recvUid;
    private String message;
    private boolean isImg;

    public ChatModel() {
    }

    public ChatModel(String sendUid, String recvUid, String message) {
        this.sendUid = sendUid;
        this.recvUid = recvUid;
        this.message = message;
        this.isImg = false;
    }

    public ChatModel(String sendUid, String recvUid, String message, boolean isImg) {
        this.sendUid = sendUid;
        this.recvUid = recvUid;
        this.message = message;
        this.isImg = isImg;
    }

    public String getSendUid() {
        return sendUid;
    }

    public void setSendUid(String sendUid) {
        this.sendUid = sendUid;
    }

    public String getRecvUid() {
        return recvUid;
    }

    public void setRecvUid(String recvUid) {
        this.recvUid = recvUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isImg() {
        return isImg;
    }

    public void setImg(boolean img) {
        isImg = img;
    }
}
