package kr.hhplus.be.server.common;

public enum ErrorCode {
    //point
    USER_POINT_NOT_FOUND(404, "사용자에 해당하는 포인트가 없습니다."),
    EXCEED_MAX_BALANCE(400, "최대 보유 금액을 초과하였습니다."),
    INSUFFICIENT_BALANCE(400, "잔액이 부족합니다."),

    //product
    PRODUCT_NOT_FOUND(404, "존재하는 상품이 없습니다."),

    //coupon
    COUPON_NOT_FOUND(404, "존재하는 쿠폰이 없습니다."),
    ALREADY_ISSUED_COUPON(400, "이미 발급된 쿠폰입니다."),
    EXCEED_QUANTITY(400, "발급 수량을 초과하였습니다."),
    ISSUE_PERIOD_NOT_STARTED(400, "발급 시작일보다 이전입니다."),
    ISSUE_PERIOD_ENDED(400, "발급 만료일보다 이후입니다.");

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
