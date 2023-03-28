package com.spsi.minesweeper.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data 
@Document
public class Game implements Serializable {

    @Id @Indexed
    String id;
    @Indexed
    String host;
    @NonNull
    int[] board;
    @NonNull
    Integer width;
    @NonNull
    Integer height;
    @NonNull
    boolean[] revealed;
    @NonNull
    boolean[] flag;
    @NonNull
    Set<String> attenders;
    @NonNull
    List<Integer> mines;
    @NonNull
    GameStatus gameStatus;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,ToStringStyle.JSON_STYLE);
    }
}
