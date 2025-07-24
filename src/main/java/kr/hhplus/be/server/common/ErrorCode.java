package kr.hhplus.be.server.common;

public enum ErrorCode {
    //point
    USER_POINT_NOT_FOUND(404, "사용자에 해당하는 포인트가 없습니다."),
    EXCEED_MAX_BALANCE(400, "최대 보유 금액을 초과하였습니다."),
    INSUFFICIENT_BALANCE(400, "잔액이 부족합니다."),

    //product
    PRODUCT_NOT_FOUND(404, "존재하는 상품이 없습니다.");
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
