package com.example.toongallery.domain.webtoon.repository;

import com.example.toongallery.domain.author.entity.QAuthor;
import com.example.toongallery.domain.category.entity.QCategory;
import com.example.toongallery.domain.user.entity.QUser;
import com.example.toongallery.domain.webtoon.entity.QWebtoon;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtooncategory.entity.QWebtoonCategory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.toongallery.domain.category.entity.QCategory.category;
import static com.example.toongallery.domain.user.entity.QUser.user;
import static com.example.toongallery.domain.webtoon.entity.QWebtoon.webtoon;

@RequiredArgsConstructor
public class WebtoonRepositoryImpl implements WebtoonRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Webtoon> findBySearch(
            String keyword,
            List<String> genres,
            String authorName,
            Pageable pageable
    ) {
        QWebtoon webtoon = QWebtoon.webtoon;
        QAuthor author = QAuthor.author;
        QUser user = QUser.user;
        QWebtoonCategory webtoonCategory = QWebtoonCategory.webtoonCategory;
        QCategory category = QCategory.category;

        JPAQuery<Webtoon> query = queryFactory
                .selectFrom(webtoon)
                .distinct()
                .leftJoin(author).on(author.webtoon.id.eq(webtoon.id))
                .leftJoin(user).on(author.user.id.eq(user.id))
                .leftJoin(webtoonCategory).on(webtoonCategory.webtoon.id.eq(webtoon.id))
                .leftJoin(webtoonCategory.category, category)
                .where(
                        titleContains(keyword),
                        genresContain(genres),
                        authorNameEquals(authorName)
                )
                .groupBy(webtoon.id)
                .orderBy(webtoon.views.desc());

        long total = query.fetch().size();
        List<Webtoon> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression titleContains(String keyword) {
        return StringUtils.hasText(keyword) ? webtoon.title.containsIgnoreCase(keyword) : null;
    }

//    private BooleanExpression genresContain(List<String> genres) {
//        return (genres != null && !genres.isEmpty()) ?
//                genres.stream()
//                        .map(genre->webtoon.genres.like("%"+genre+"%"))
//                        .reduce(BooleanExpression::or)
//                        .orElse(null) : null;
//    }
    private BooleanExpression genresContain(List<String> genres) {
        return (genres != null && !genres.isEmpty()) ?
                genres.stream()
                        .map(genre -> category.categoryName.like("%" + genre + "%"))
                        .reduce(BooleanExpression::or)
                        .orElse(null) : null;
    }

    private BooleanExpression authorNameEquals(String authorName) {
        return StringUtils.hasText(authorName) ? user.name.eq(authorName) : null;
    }
}