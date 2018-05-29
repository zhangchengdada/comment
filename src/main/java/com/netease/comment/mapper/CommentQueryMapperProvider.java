package com.netease.comment.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class CommentQueryMapperProvider {


    public static final String TABLE_NAME = "comment_query";

    public String updatePicUrlsAndStatus(Map<String, Object> parameter) {
        String jsonPics = parameter.get("jsonPics").toString();
         String sql = new SQL() {{
            UPDATE(TABLE_NAME);
            SET("status = #{status} ");
            WHERE("comment_id = #{commentId}");
        }}.toString();

        return sql ;
    }
}
