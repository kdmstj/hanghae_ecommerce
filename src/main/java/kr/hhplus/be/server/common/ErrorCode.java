package kr.hhplus.be.server.common;

public enum ErrorCode {
    USER_POINT_NOT_FOUND(404, "사용자에 해당하는 포인트가 없습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;

    }

    public String getMessage() {
        return message;
    }

    public int getStatus(){
        return status;
    }
}
