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
    EMAIL_MISMATCH("존재하지 않는 이메일입니다.", HttpStatus.BAD_REQUEST),
    SIGNIN_FAILED("로그인에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("잘못된 비밀번호입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PASSWORD_SAME_AS_OLD("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_SAME("새 비밀번호와 새 비밀번호 확인이 다릅니다.", HttpStatus.BAD_REQUEST),
    INACTIVE_USER("이미 탈퇴된 회원입니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("해당 refresh token을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("유효하지 않은 refresh token 입니다.", HttpStatus.BAD_REQUEST),
    USERID_NOT_MATCH("유저 id가 일치하지 않습니다." , HttpStatus.BAD_REQUEST),
    //웹툰 관련 에러 코드
    WEBTOON_NOT_FOUND("웹툰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EPISODE_TITLE("이미 존재하는 에피소드 제목입니다.",HttpStatus.BAD_REQUEST),
    //에피소드 관련 에러 코드
    EPISODE_NOT_FOUND("에피소드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    //카테고리 관련 에러 코드
    DUPLICATE_CATEGORY_NAME("중복된 카테고리 명이 있습니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXIST("존재하지 않는 카테고리입니다.", HttpStatus.BAD_REQUEST),
    //댓글 관련 에러코드
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMMENT_NOT_MATCH_USER("댓글의 작성자가 아닙니다" , HttpStatus.BAD_REQUEST),
    //좋아요 관련 에러 코드
    COMMENT_NOT_EXIST("댓글이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    //관심 관련 에러 코드

    //평점 관련 에러 코드
    EPISODE_NOT_EXIST("에피소드가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    RATE_NOT_EXIST("평점이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    //이미지 관련 에러코드

    //그 외 에러 코드
    UNSUPPORTED_FILE_TYPE("지원하지 않는 파일 타입입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TYPE("유효하지 않은 타입입니다.",HttpStatus.BAD_REQUEST),
    SERVER_NOT_WORK("서버 문제로 인해 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CONCURRENCY_CONFLICT("동시 수정이 시도되었습니다.", HttpStatus.BAD_REQUEST);
    private final String message;
    private final HttpStatus httpStatus;
}
