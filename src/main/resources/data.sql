INSERT INTO users (id, email, password, name, birth_date, gender,user_role, created_At, modified_At)
VALUES (1, 'test@example.com', 'password@123', '테스트 사용자', '1990-01-01', 'MALE','ROLE_USER', NOW(), NOW());

INSERT INTO episodes (id, title, episode_number, thumbnail_url, created_at, modified_at)
VALUES (1, '에피소드 제목', 3,'testurl', NOW(), NOW());

INSERT INTO comments (id, content, created_at, modified_at, episode_id, user_id, parent_id)
VALUES (1, '테스트 댓글입니다.', NOW(), NOW(), 1, 1, NULL);

INSERT INTO webtoons (favorite_count, rate, views, created_at, id, modified_at, author, day_of_week, description, genre, status, thumbnail, title)
VALUES (0,0,0,NOW(),1,NOW(),'테스트작가','TUE','TEST','fantasy','ONGOING','testurl','테스트입니다.');