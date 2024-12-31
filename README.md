## 💁‍♂️ Introduce 

한문철 변호사님의 유튜브를 모티브로 하여 **게임 유저들간의 분쟁 조정을 위한 SNS 서비스** 입니다.

## 🏢 Architecture

![](./img/infra-architecture.png)

## 💼 Portfolio 

### ⚡️ Optimization

#### [코드 공통화] AOP를 이용한 유저 인증 정보 로직 공통화 

- 원인
  - JWT Token에서 User의 정보를 꺼내오는 로직 중복 발생.

```java
    @PostMapping
    public void saveComment(@RequestBody CommentRequest request, HttpServletRequest httpServletRequest) {
        // 토큰 추출 및 검증
        String token = httpServletRequest.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("Authorization token is missing");
        }

        // 토큰에서 정보 추출
        TokenInfo tokenInfo = extractTokenInfo(token);

        // 멤버 조회
        Member member = memberRepository.findById(tokenInfo.id())
            .orElseThrow(() -> new BadRequestException("Member not found"));

        // 비즈니스 로직 호출
        CommentSaveRequest saveDto = new CommentSaveRequest(null, request.content(), request.postId());
        commentService.save(saveDto, member);
    }
```

- 해결과정
  - Spring AOP를 이용해서 유저 정보를 가져오는 Annotation을 만들어서 공통화.
  - MemberSession Annotation & Resolver 생성

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Parameter(hidden = true)
public @interface MemberSession {
}
```

<details> 
<summary>MemberSessionResolver 상세 보기</summary>

```java
@Component
@RequiredArgsConstructor
public class MemberSessionResolver implements HandlerMethodArgumentResolver {
    private final MemberRepository memberRepository;

    @Override
    // 여기서 True로 return이 되면 resolveArgument가 실행됨
    public boolean supportsParameter(MethodParameter parameter) {
        // 지원하는 파라미터 체크, 어노테이션 체크하는 영역
        // 1. 어노테이션이 있는지 체크
        var annotation = parameter.hasParameterAnnotation(MemberSession.class);
        // 2. parameter type 체크
        boolean parameterType = parameter.getParameterType()
            .equals(Member.class);

        return annotation && parameterType;
    }

    @Override
    @Nullable
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        return getTokenInfo()
            .map(this::findMember)
            .orElse(null);
    }

    private Optional<TokenInfo> getTokenInfo() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .map(attributes -> (TokenInfo) attributes.getAttribute("tokenInfo", RequestAttributes.SCOPE_REQUEST));
    }

    private Member findMember(TokenInfo tokenInfo) {
        return memberRepository.findById(
                tokenInfo.id())
            .orElseThrow(() -> {
                return new BadRequestException(MemberStatus.MEMBER_NOT_FOUND);
            });
    }
}
```

</details>

  - Controller에 적용 

```java
    @PostMapping
    public PostDetailResponse upload(
        @Valid
        @RequestBody PostUploadRequest request,
        @MemberSession Member member
    ) {
        PostDetailResponse response = postService.upload(request, member);
        return response;
    }
```

- 결과 
  - 5개의 클래스, 17개의 메서드에서 로직 공통화
  - <img src="./img/member-session-aop.png" width="50%">




### 🛠️ ErrorFix

#### OneToOne Lazy Loading 오류 해결 (불필요 쿼리 삭제)

<details>
<summary>상세보기</summary>

- 문제
  - **OneToOne 연관관계 Lazy Loading 문제**: 
    - `MatchUser` 엔티티에서 `MatchGame`과 `ManyToOne` 관계로 매핑되어 있음.
    - `VoteOptions`가 `MatchUser`와 `OneToOne`으로 매핑되어 있으며, `MatchGame`은 `ManyToOne`으로 연관되어 있음.
    - Hibernate에서 연관관계의 주인이 아닌 곳에서는 **Lazy Loading이 동작하지 않을 가능성**이 있음.
  - 이로 인해 `MatchGame` 데이터를 사용하는 곳이 없음에도 **불필요한 쿼리**가 실행됨.
- 해결과정
1. **문제 상황 재현**:
   - `Post -> VoteOptions -> MatchUser -> MatchGame`으로 이어지는 관계에서 쿼리가 과도하게 실행됨을 확인.
   - 디버깅 중 `MatchGame` 관련 데이터 조회가 발생하지만 실제로 데이터가 사용되지 않음.
2. **디버깅 시도**:
   - `Getter`와 `Constructor`를 직접 정의하고, 해당 메서드에 브레이크포인트 설정.
   - 하지만 Stack Trace를 타고 올라가도 **`createQuery` 호출 조건**을 특정할 수 없었음.
   - 원인을 정확히 추적하지 못했지만, `Lazy Loading`과 관련된 문제로 추정.
3. **문제의 본질 파악**:
   - OneToOne 연관관계에서 주인이 아닌 곳에서는 Lazy Loading이 동작하지 않을 수 있다는 Hibernate 문서를 참조.
   - `MatchUser`에서 `MatchGame`의 불필요한 연관관계로 인해 데이터 조회가 발생한 것으로 결론.
- 결과
  - **해결책**:
    - `MatchUser`와 `MatchGame` 간의 **연관관계를 제거**.
    - 이를 통해 `MatchGame`에 대한 불필요한 쿼리 실행 방지.

  - **쿼리 변화**:
    - 기존에 실행되던 쿼리:
      ```sql
      select mu1_0.game_id, mu1_0.id, mu1_0.champion_name, mu1_0.nickname, mu1_0.puuid, mu1_0.win
      from match_user mu1_0
      where mu1_0.game_id = 'KR_7356095596';
      ```
    - 해결 후 불필요한 쿼리가 더 이상 실행되지 않음.

  - **성능 개선**:
    - 해결 전:
      - **TPS**: 1895
      - **응답 속도**: 530ms
    - 해결 후:
      - **TPS**: 2321
      - **응답 속도**: 433ms
    - **개선 결과**:
      - TPS: **426 증가** (약 22.5% 개선)
      - 응답 속도: **97ms 감소** (약 18.3% 개선)

</details>

## 🎨 Design

<details>
<summary>상세보기</summary>

- 메인 화면 

<img src="./img/main.png" width="100%" />

- 회원가입 화면 

<img src="./img/signup.png" width="100%" />

- 게시물 업로드 화면

<img src="./img/post-upload.png" width="100%" />

- 게시물 상세 화면

<img src="./img/post-detail-page.png" width="100%" />
  
</details>

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
