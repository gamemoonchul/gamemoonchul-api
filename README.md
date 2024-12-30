## 💁‍♂️ Introduce 

한문철 변호사님의 유튜브를 모티브로 하여 **게임 유저들간의 분쟁 조정을 위한 SNS 서비스** 입니다.

<details>
<summary>🎨 Design</summary>

- 메인 화면 

<img src="./img/main.png" width="75%" />

- 회원가입 화면 

<img src="./img/signup.png" width="75%" />

- 게시물 업로드 화면

<img src="./img/post-upload.png" width="75%" />

- 게시물 상세 화면

<img src="./img/post-detail-page.png" width="75%" />
  
</details>

## 🏢 Architecture

![](./img/infra-architecture.png)

## 📚 로컬에서 실행하는 방법

- root 폴더에 .env 생성 아래 **YOUR_로 시작하는 항목들을 전부 적절한 값으로 대체**하시기 바랍니다.

```console
JWT_SECRET=YOUR_JWT_SECRET
S3_BUCKET=YOUR_S3_BUCKET
S3_ACCESS_KEY=YOUR_S3_ACCESS_KEY
S3_SECRET_KEY=YOUR_S3_SECRET_KEY
S3_REGION=YOUR_S3_REGION
RIOT_API=YOUR_RIOT_API
GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET=YOUR_GOOGLE_CLIENT_SECRET
APPLE_CLIENT_ID=YOUR_APPLE_CLIENT_ID
APPLE_KEY_ID=YOUR_APPLE_KEY_ID
APPLE_TEAM_ID=YOUR_APPLE_TEAM_ID
APPLE_PRIVATE_KEY=YOUR_APPLE_PRIVATE_KEY
DB_URL=jdbc:mysql://gm-db:3306/gamemuncheol?userSSL=false&useUnicode=true&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=df159357
BASE_URL=YOUR_BASE_URL
REDIS_HOST=gm-redis
REDIS_PORT=6379
REDIS_PW=df159357
```

- 스크립트 실행

```console
bash local_deploy.sh
```

## 🚨 주요 이슈

- [디테일 화면 Redis Cache Aside Pattern 적용](https://github.com/gamemuncheol/gamemuncheol-api/issues/155)
- [낙관락 vs 비관락 성능 비교](https://github.com/gamemuncheol/gamemuncheol-api/issues/192)
- [Spring Server DB CP, Thread Pool 등 최적화](https://github.com/gamemuncheol/gamemuncheol-api/issues/180)
- [JWT 토큰 외부로 노출되면 안되는 정보 은닉 및 JWT Filter에서 사용자 정보 로드 방식 개선](https://github.com/gamemuncheol/gamemuncheol-api/issues/167)
