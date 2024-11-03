package com.gamemoonchul.common.constants;

public class RedisKeyConstant {
    /**
     * Cacheable에서 사용하는 키
     */
    public static final String POST_COUNT_CACHEABLE = "post:count";
    public static String postCountKey(Long postId) {
        // Cacheable에서 자동으로 뒤에 ::을 붙이기 때문에
        // Sync를 맞출려면 붙여줘야 함
        return POST_COUNT_CACHEABLE + "::" + postId;
    }
}
