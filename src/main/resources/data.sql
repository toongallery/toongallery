# DELETE FROM users;
# DELETE FROM categories;
#
# ALTER TABLE users AUTO_INCREMENT = 1;
# ALTER TABLE categories AUTO_INCREMENT = 1;
#
# INSERT INTO users (id, email, password, name, birth_date, gender,user_role, deleted_At, created_At, modified_At)
# VALUES (1, 'test@example.com', 'password1!', '테스트', '1990-01-01', 'MALE','ROLE_ADMIN', NULL, NOW(), NOW());
# INSERT INTO users (id, email, password, name, birth_date, gender,user_role, deleted_At, created_At, modified_At)
# VALUES (2, 'test2@example.com', 'password1!', '테스트2', '1990-01-01', 'MALE','ROLE_ADMIN', NULL, NOW(), NOW());
# INSERT INTO categories(ID, CREATED_AT, MODIFIED_AT, CATEGORY_NAME)
# VALUES (1,NOW(), NOW(),'ACTION'),
# (2,NOW(), NOW(),'COMEDY');
desc users;
# INSERT INTO webtoons (favorite_count, rate, views, created_at, id, modified_at, day_of_week, description, status, thumbnail, title)
# VALUES (0,0,0,NOW(),1,NOW(),'TUE','TEST', 'ONGOING','testurl','테스트입니다.');

#
# INSERT INTO comments (id, content, created_at, modified_at, episode_id, user_id, parent_id)
# VALUES (1, '테스트 댓글입니다.', NOW(), NOW(), 1, 1, NULL);