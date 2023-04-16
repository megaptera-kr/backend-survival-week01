# backend-survival-week01

백엔드 생존코스 1주차 과제

## 구현할 기능들

- Todo 목록 얻기 -`GET /tasks`
- Todo 생성하기 -`POST /tasks`
    - Body data가 없을 경우 `400 Bad Request`
- Todo 제목 수정하기 - `PATCH /tasks/{id}`
    - 존재하지 않는 id로 요청하는 경우 `404 Not Found`
    - Body data가 없을 경우 `400 Bad Request`
- Todo 삭제하기 -`DELETE /tasks/{id}`
    - 존재하지 않는 id로 요청하는 경우 `404 Not Found`