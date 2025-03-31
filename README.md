# 🧾툰갤러리 (Toon Gallery)
툰갤러리는 웹툰 사이트를 개발하는 프로젝트로 Cache를 이용한 성능개선을 하는 주제를 채택하여, 검색 기능을 개선하는 과정을 녹였습니다.
## 📌프로젝트 개요
- ### 프로젝트명: 툰갤러리 (Toon Gallery)
- ### 개발 기간: 2025.03.24 ~ 2025.03.31
- ### 개발 인원: 4명
- ### 🛠️사용 기술 스택:
  - #### 🔙Backend: Spring Boot, Spring Security + JWT, JPA, MySQL
  - #### ☁️Cloud Storage: AWS S3
  - #### 🌐CDN: AWS CloudFront
  - #### ⚙️CI/CD: GitHub Actions, Docker, AWS EC2
  - #### 🧪Test & Coverage: JUnit, JaCoCo
  - #### 🧾Version Control: Git & GitHub

---

## 📌 팀원 소개 및 담당 역할

| 이름      | 역할  | 담당 기능               |
|---------|------|---------------------|
| **이정민** | 팀장 | 관심, 좋아요, 별점, 동시성 제어 |
| **윤현호** | 팀원 | 유저, 댓글, CI/CD       |
| **이한빈** | 팀원 | 웹툰, 인기 검색어(캐싱)      |
| **전영준** | 팀원 | 에피소드, 카테고리, 이미지 업로드 |

---
## 📌핵심 기능
### 🌟인기 검색어
- Cacheable어노테이션을 활용하여 조회수 증가 및 어뷰징 방지에 캐시를 적용하였습니다.
- 테스트 코드에서는 조회수 폭증과 인기 검색어 대량 조회 상황을 시뮬레이션하여 캐시 적용 전후의 성능을 비교하였습니다.
### 🌟CI/CD
- GitHub Actions로 CI/CD 파이프라인을 구축하였습니다.
- JaCoCo로 테스트 커버리지를 검증한 뒤 Docker 이미지 빌드 및 EC2 서버에 자동 배포하였습니다.
- 환경 변수는 Git Secret을 활용하여 안전하게 관리하였습니다.
### 🌟동시성 제어
- Optimistic Locking을 활용해 데이터에 직접적인 잠금 없이 버전 필드를 통해 충돌을 감지하고 처리하였습니다.
- 충돌 발생 시 랜덤한 시간(100~150ms) 대기 후, 설정한 최대 재시도 횟수만큼 요청을 반복하여 안정적으로 동시성 문제를 해결하였습니다.
### 🌟이미지 업로드
- S3를 이용해 웹툰과 에피소드의 썸네일 및 본문 이미지를 경로 기반으로 저장하였습니다.
- CloudFront를 통해 해당 이미지들을 빠르게 배포할 수 있도록 구성하였습니다.

--- 
## 📊ERD
![img.png](https://teamsparta.notion.site/image/attachment%3Aca851250-053d-4f6a-ac63-ea1e85021e6c%3Aimage.png?table=block&id=1c72dc3e-f514-8009-97b1-ec0afeb4fd6e&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=2000&userId=&cache=v2)

--- 
## 💬API 명세서
<details>
<summary>회원 관련 API</summary>

| Method |    기능    |       url       |
|:------:|:--------:|:---------------:|
|  POST  |  회원 가입   |  /auth/signup   |
|  POST  |   로그인    |   /auth/login   |
|  GET   |  유저 정보   | /users/{userId} |
|  GET   |   내 정보   |  /users/myinfo  |
| PATCH  | 유저 정보 수정 |     /users      |
| PATCH  | 비밀번호 수정  | /users/password |
| DELETE |  유저 탈퇴   |     /users      |
</details>

<details>
<summary>웹툰 관련 API</summary>

| Method |      기능       |         url          |
|:------:|:-------------:|:--------------------:|
|  POST  |     웹툰 생성     |      /webtoons       |
|  GET   |   웹툰 전체 조회    |      /webtoons       |
|  GET   | 웹툰 검색(캐시 미적용) | /webtoons/v1/search? |
|  GET   | 웹툰 검색(캐시 적용)  | /webtoons/v2/search? |
|  GET   |   웹툰 인기 검색    |  /webtoons/popular   |
</details>

<details>
<summary>에피소드 관련 API</summary>

| Method |     기능     |         url          |
|:------:|:----------:|:--------------------:|
|  POST  |  에피소드 생성   |      /webtoon/{webtoonId}/episode       |
|  GET   | 에피소드 전체 조회 |      /webtoon/{webtoonId}/episode       |
|  GET   | 에피소드 단건 조회 | /webtoon/{webtoonId}/episode/{episodeId} |
| PATCH  | 에피소드 제목 수정 | /webtoon/{webtoonId}/episode/{episodeId}/title |
| PATCH  | 썸네일 이미지 수정 |  /webtoon/{webtoonId}/episode/{episodeId}/thumbnail   |
| PATCH  | 본문 이미지 수정  |/webtoon/{webtoonId}/episode/{episodeId}/images|
| DELETE |  에피소드 삭제   |/webtoon/{webtoonId}/episode/{episodeId}|
</details>

<details>
<summary>카테고리 관련 API</summary>

| Method |     기능     |          url           |
|:------:|:----------:|:----------------------:|
|  POST  |  카테고리 생성   |       /category        |
|  GET   | 카테고리 전체 조회 |       /category        |
|  GET   | 카테고리 단건 조회 | /category/{categoryId} |
| PATCH  |  카테고리 수정   | /category/{categoryId} |
| DELETE |  카테고리 삭제   | /category/{categoryId} |
</details>

<details>
<summary>댓글 관련 API</summary>

| Method |    기능    |          url           |
|:------:|:--------:|:----------------------:|
|  POST  |  댓글 생성   |       /webtoons/{episodeId}/comments        |
|  GET   | 댓글 전체 조회 |       /webtoons/{episodeId}/comments        |
|  GET   |  대댓글 조회  | /webtoons/{episodeId}/comments/{parentId} |
| PATCH  |  댓글 수정   | /webtoons/{episodeId}/comments/{commentId} |
| DELETE |  댓글 삭제   | /webtoons/{episodeId}/comments/{commentId} |
</details>

<details>
<summary>웹툰 관심 관련 API</summary>

| Method |  기능   |          url           |
|:------:|:-----:|:----------------------:|
|  POST  | 웹툰 관심 |       /favorite/{commentId}        |
</details>

<details>
<summary>좋아요 관련 API</summary>

| Method |     기능     |          url           |
|:------:|:----------:|:----------------------:|
|  POST  | 댓글 좋아요(토글) |     /like/{commentId}       |
|  GET   |   좋아요 조회   |       /like/{commnetId}/{count}        |
</details>

<details>
<summary>별점 관련 API</summary>

| Method |  기능   |            url             |
|:------:|:-----:|:--------------------------:|
|  POST  | 별점 생성 | /rate/{userId}/{episodeId} |
|  GET   | 별점 조회 | /rate/average/{webtoonId}  |
| DELETE | 별점 삭제 |           /rate            |
</details>


---
## ✍️프로젝트 소감
#### 윤현호 : 이번 프로젝트를 진행하면서 CI/CD를 처음 해보면서 어렵기도 했지만 처음 해보는 작업을 해보면서 매우 많은 것을 배워가는 프로젝트가 된 것 같습니다.
#### 이한빈 : 캐시를 처음 사용했지만, 캐시를 적용하기 위해 다양한 정보를 알아가면서 캐시에 대한 지식이 쌓을 수 있어서 많은 것을 배울 수 있는 기회가 되었습니다!
#### 이정민 : 동시성 제어를 처음 알게 되면서 지금까지 했던 프로젝트에서 적용해 볼만한 부분이 많았고 새로운 것들을 배워가는 좋은 시간이었습니다.
#### 전영준 : 처음으로 도입해본 기능들로 어려움을 겪었지만, 조원 분들과 재밌고 유익한 시간을 보내서 좋았습니다.