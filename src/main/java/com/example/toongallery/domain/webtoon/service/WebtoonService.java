package com.example.toongallery.domain.webtoon.service;

import com.example.toongallery.domain.author.entity.Author;
import com.example.toongallery.domain.author.repository.AuthorRepository;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;
    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public Webtoon saveWebtoon(AuthUser authUser, WebtoonSaveRequest webtoonSaveRequest) {

        //현재 로그인한 사용자 가져오기
        User mainAuthor = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(()->new BaseException(ErrorCode.USER_NOT_EXIST,null));

        //사용자가 작가가 아닐 경우 예외처리
        if(authUser.getUserRole() != UserRole.ROLE_ADMIN){
            throw new BaseException(ErrorCode.INVALID_USER_ROLE,null);
        }

        //추가할 작가 이름으로 사용자 조회(있는 경우에만 실행)
        List<User> authors = new ArrayList<>();

        if(webtoonSaveRequest.getAuthors() != null && !webtoonSaveRequest.getAuthors().isEmpty()) {
            authors = userRepository.findByNameIn(webtoonSaveRequest.getAuthors());

            if(authors.size() != webtoonSaveRequest.getAuthors().size()) {
                throw new BaseException(ErrorCode.USER_NOT_FOUND,null);
            }

            //작가가 아닌 사용자가 포함되었는지 체크
            List<User> nonAuthors = authors.stream()
                    .filter(user->user.getUserRole() != UserRole.ROLE_ADMIN)
                    .collect(Collectors.toList());

            if(!nonAuthors.isEmpty()) {
                throw new BaseException(ErrorCode.INVALID_USER_ROLE,null);
            }
        }

        //작가 리스트에 본인 추가
        authors.add(mainAuthor);

        List<Author> authorList = authors.stream()
                .map(Author::new)
                .collect(Collectors.toList());

        //List<String>을 콤마(,)로 연결하여 저장
        String genreString = String.join(",", webtoonSaveRequest.getGenres());

        Webtoon webtoon = new Webtoon(
                webtoonSaveRequest.getTitle(),
                authorList,
                genreString,
                webtoonSaveRequest.getThumbnail(),
                webtoonSaveRequest.getDescription(),
                webtoonSaveRequest.getDay_of_week(),
                webtoonSaveRequest.getStatus()
        );
        Webtoon savedWebtoon = webtoonRepository.save(webtoon);

        return savedWebtoon;
    }

    @Transactional(readOnly = true)
    public Page<WebtoonResponse> getWebtoons(int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size);

        Page<Webtoon> webtoons = webtoonRepository.findAll(pageable);

        return webtoons.map(webtoon -> {

            System.out.println("웹툰 ID: " + webtoon.getId());
            System.out.println("저장된 작가 수: "+webtoon.getAuthors().size());

            List<Long> authorIds = webtoon.getAuthors().stream()
                    .map(Author::getUserId)
                    .collect(Collectors.toList());

            System.out.println("조회할 작가 ID 리스트: "+authorIds);

            List<String> authorNames = userRepository.findNamesById(authorIds);

            System.out.println("조회된 작가 이름 리스트: "+authorNames);

            List<String> genreList = Arrays.asList(webtoon.getGenres().split(","));

            return new WebtoonResponse(
                    webtoon.getId(),
                    webtoon.getTitle(),
                    authorNames,
                    genreList,
                    webtoon.getThumbnail(),
                    webtoon.getDescription(),
                    webtoon.getDay_of_week(),
                    webtoon.getStatus()
            );
        });
    }
}
