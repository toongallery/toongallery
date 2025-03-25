package com.example.toongallery.domain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //유저 관련 에러 코드
    DUPLICATE_EMAIL("중복된 이메일이 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_USER_ROLE("유효하지 않은 역할입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FORM("유효하지 않은 형식입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST("존재하지 않는 회원입니다.", HttpStatus.BAD_REQUEST),
    SIGNIN_FAILED("로그인에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("잘못된 비밀번호입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PASSWORD_SAME_AS_OLD("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INACTIVE_USER("이미 탈퇴된 회원입니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("해당 refresh token을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("유효하지 않은 refresh token 입니다.", HttpStatus.BAD_REQUEST),
    USERID_NOT_MATCH("유저 id가 일치하지 않습니다." , HttpStatus.BAD_REQUEST),
    //가게 관련 에러 코드
    INVALID_CATEGORY_TYPE("유효하지 않은 카테고리 이름입니다.", HttpStatus.BAD_REQUEST),
    CONFLICT_STORE_NAME("동일한 가게이름이 존재합니다.",HttpStatus.CONFLICT),
    EXCEED_STORE_LIMIT("최대 3개 까지의 가게를 등록할 수 있습니다.",HttpStatus.BAD_REQUEST),
    STORE_NOT_FOUND("가게 정보를 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    UNAUTHORIZED_STORE_ACCESS("본인의 가게가 아닙니다.",HttpStatus.BAD_REQUEST),
    CANNOT_MODIFY_DELETED_STORE("폐업한 가게는 영업이 불가능합니다.",HttpStatus.BAD_REQUEST),
    //메뉴 관련 에러 코드
    NOT_FOUND_MENU("메뉴가 없습니다.", HttpStatus.NOT_FOUND),
    //주문 관련 에러 코드
    ORDER_ONLY_FOR_REGULAR_USER("주문은 일반 회원만 이용할 수 있습니다.", HttpStatus.UNAUTHORIZED),
    ORDER_ACCEPT_ONLY_FOR_OWNER("주문 수락은 가게 사장님만 가능합니다.", HttpStatus.UNAUTHORIZED),
    ORDER_REJECT_ONLY_FOR_OWNER("주문 거절은 가게 사장님만 가능합니다.", HttpStatus.UNAUTHORIZED),
    ORDER_STATUS_ONLY_FOR_OWNER("주문 상태 변경은 가게 사장님만 가능합니다.", HttpStatus.UNAUTHORIZED),
    ORDER_NOT_FOUND("주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    // 메뉴 관련 에러 코드
    MENU_NOT_FOUND("메뉴 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    //리뷰 관련 에러 코드
    REVIEW_NOT_FOUND("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_REVIEW_ACCESS("리뷰 작성자만 수정 가능합니다", HttpStatus.UNAUTHORIZED),
    DUPLICATE_REVIEW("이미 해당 주문에 대한 리뷰가 작성했습니다.", HttpStatus.BAD_REQUEST),
    EARLY_REVIEW("주문이 완료되기 전까지는 리뷰를 달 수 없습니다.", HttpStatus.BAD_REQUEST),
    //그 외 에러 코드
    INVALID_TYPE("유효하지 않은 타입입니다.",HttpStatus.BAD_REQUEST),
    SERVER_NOT_WORK("서버 문제로 인해 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;
}
