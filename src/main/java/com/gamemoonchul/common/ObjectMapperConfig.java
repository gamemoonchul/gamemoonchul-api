package com.gamemoonchul.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
    // Spring이 실행될 때 Object Mapper를 불러오는데 설정된 Object Mapper가 없으면
    // Spring이 임의적으로 Object Mapper를 생성하고 설정된 Object Mapper가 있다면 설정된 Mapper를 불러오게 된다.
    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        // JDK 버전 이후 클래스 사용 가능
        objectMapper.registerModule(new Jdk8Module());

        objectMapper.registerModule(new JavaTimeModule());

        // 모르는 Json Value에 대해서는 무시하고 나머지 값만 파싱한다.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 비어있는 Bean을 만들 때
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        // 날짜 관련 직렬화 설정 해제
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 스네이크 케이스
         objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        return objectMapper;
    }
}
