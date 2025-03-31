package com.example.toongallery.domain.author.service;

import com.example.toongallery.domain.author.entity.Author;
import com.example.toongallery.domain.author.repository.AuthorRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final WebtoonRepository webtoonRepository;

    @Transactional
    public void createAuthors(Webtoon webtoon, List<User> authors) {
        List<Author> authorList = authors.stream()
                .map(author -> new Author(author, webtoon))
                .collect(Collectors.toList());

        authorRepository.saveAll(authorList);
    }

    @Transactional(readOnly = true)
    public List<String> getAuthorNamesByWebtoonId(Long webtoonId) {
        List<Author> authors = authorRepository.findByWebtoonId(webtoonId);
        List<Long> userIds = authors.stream()
                .map(Author::getUserId)
                .collect(Collectors.toList());

        return userRepository.findNamesById(userIds);
    }

//    @Transactional(readOnly = true)
//    public List<User> getAuthorsByWebtoonId(Long webtoonId){
//        List<Author> authors = authorRepository.findByWebtoonId(webtoonId);
//        return authors.stream()
//                .map(Author::getUser)
//                .collect(Collectors.toList());
//    }
}
