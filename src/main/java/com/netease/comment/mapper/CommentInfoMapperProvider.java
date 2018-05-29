package com.netease.comment.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class CommentInfoMapperProvider {
    public static final String TABLE_NAME = "comment_info";

    public String updatePicUrlsAndStatus(Map<String, Object> parameter) {
        String jsonPics = parameter.get("jsonPics").toString();
        String userface = parameter.get("userface").toString();
        return new SQL() {{
            UPDATE(TABLE_NAME);
            if (StringUtils.isNoneBlank(jsonPics)) {
                SET(" pic_urls = #{jsonPics}");
            }
            if (StringUtils.isNoneBlank(userface)) {
                SET(" from_userface = #{userface}");
            }
            SET("status = #{status} ");
            WHERE("comment_id = #{commentId}");
        }}.toString();
    }

}
