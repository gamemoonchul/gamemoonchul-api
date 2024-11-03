package com.gamemoonchul.common.constants;

public class RedisKeyConstant {
    public static String postCountKey(Long postId) {
        return "post:count:" + postId;
    }
}
