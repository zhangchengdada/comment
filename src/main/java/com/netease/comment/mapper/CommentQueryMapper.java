package com.netease.comment.mapper;

import com.netease.comment.dto.CommentInfoAddData;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentQueryMapper {


    @Select("select comment_id from  comment_query  where info_id = #{infoId} and deleted = 0 and (status = 0 or status = -1 ) ")
    List<String> selectByInfoIdNTrial(@Param("infoId") String infoId);


    @Select("select info_id from  comment_query GROUP BY info_id")
    List<String> selectAllInfoId();

    @UpdateProvider(type = CommentQueryMapperProvider.class, method = "updatePicUrlsAndStatus")
    Integer updatePicAndStatus(@Param("commentId") String commentId,@Param("jsonPics")String jsonPics ,@Param("status") Integer status);

    @Insert("INSERT INTO `comment_query` ( " +
            " `info_id`, " +
            " `comment_id`, " +
            " `comment_fid`, " +
            " `comment_mid`, " +
            " `praise_count`, " +
            " `reply_count`, " +
            " `deleted`, " +
            " `status` " +
            ") " +
            "VALUES " +
            " ( " +
            "  #{commentInfoAddData.infoId}, " +
            "  #{commentInfoAddData.commentId}, " +
            "  #{commentInfoAddData.commentFid}, " +
            "  #{commentInfoAddData.commentMid}, " +
            "  0, " +
            "  0, " +
            "  0, " +
            "  0 " +
            " );")
    Integer insert(@Param("commentInfoAddData") CommentInfoAddData commentInfoAddData);

    @Update("UPDATE `comment_query` " +
            "SET  " +
            " `reply_count` = reply_count + #{i} " +
            "WHERE " +
            " comment_fid = #{commentId};")
    Integer updateReplyCount(@Param("commentId") String commentFid,@Param("i") Integer i);



    @Select("select count(0) from comment_query where info_id = #{infoId} and  deleted = 0 and status = 1 ")
    Integer selectCommentCount(@Param("infoId") String infoId);


    @Select("SELECT " +
            " a.comment_id  " +
            "FROM " +
            "  comment_query a " +
            "WHERE " +
            " a.info_id = #{infoId}" +
            " and a.comment_fid is null " +
            " and a.deleted = 0  " +
            " and a.status = 1  " +
            "ORDER BY " +
            "( a.create_time) DESC " +
            "LIMIT #{begin},#{finish}")
    List<String> selectByInfoIdNewest(@Param("infoId") String infoId, @Param("begin") Integer begin, @Param("finish") Integer finish);





    @Select("SELECT " +
            " a.comment_id " +
            "FROM " +
            "  comment_query a " +
            "WHERE " +
            " a.info_id = #{infoId}" +
            " and a.comment_fid is null " +
            " and a.deleted = 0  " +
            " and a.status = 1  " +
            "ORDER BY " +
            "( a.praise_count  + a.reply_count) DESC " +
            "LIMIT #{begin},#{finish}")
    List<String> selectByInfoIdHottest(@Param("infoId") String infoId, @Param("begin") Integer begin, @Param("finish") Integer finish);
}
