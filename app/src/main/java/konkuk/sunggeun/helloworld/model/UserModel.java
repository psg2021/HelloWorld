package konkuk.sunggeun.helloworld.model;

public class UserModel {
    private String nickName;
    private String country;
    private String interesting;

    public UserModel(String nickName) {
        this.nickName = nickName;
        this.country = null;
        this.interesting = null;
    }

    public UserModel(String nickName, String country, String interesting) {
        this.nickName = nickName;
        this.country = country;
        this.interesting = interesting;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getInteresting() {
        return interesting;
    }

    public void setInteresting(String interesting) {
        this.interesting = interesting;
    }
}
