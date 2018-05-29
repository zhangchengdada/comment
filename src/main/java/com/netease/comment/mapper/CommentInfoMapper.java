package com.netease.comment.mapper;

import com.netease.comment.dto.CommentInfoAddData;
import com.netease.comment.model.CommentInfoNewest;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentInfoMapper {




    @UpdateProvider(type = CommentInfoMapperProvider.class, method = "updatePicUrlsAndStatus")
    Integer updatePicAndStatus(@Param("commentId") String commentId,@Param("jsonPics")String jsonPics ,@Param("userface")String userface,@Param("status") Integer status);

    @Update("UPDATE `comment_info` " +
            "SET  " +
            " `reply_count` = reply_count + #{i} " +
            "WHERE " +
            " comment_fid = #{commentId};")
    Integer updateReplyCount(@Param("commentId") String commentFid,@Param("i") Integer i);


    @Results({
            @Result(property = "commentId", column = "comment_id"),
            @Result(property = "commentFid", column = "comment_fid"),
            @Result(property = "infoId", column = "info_id"),
            @Result(property = "fromAppId", column = "from_app_id"),
            @Result(property = "fromUserId", column = "from_user_id"),
            @Result(property = "fromUserNName", column = "from_user_n_name"),
            @Result(property = "fromUserface", column = "from_userface"),
            @Result(property = "commentTime", column = "comment_time"),
            @Result(property = "content", column = "content"),
            @Result(property = "picUrls", column = "pic_urls"),
            @Result(property = "status", column = "status")
    })
    @Select("<script>" +
            "SELECT " +
            " a.comment_id , " +
            " a.comment_fid , " +
            " a.comment_time , " +
            " a.content , " +
            " a.pic_urls , " +
            " a.status , " +
            " a.info_id , " +
            " a.from_app_id , " +
            " a.from_user_id , " +
            " a.from_user_n_name , " +
            " a.from_userface  " +
            "FROM " +
            "  comment_info a " +
            "WHERE " +
            " a.comment_mid in " +
            " <foreach  collection='commentIds'  item='commentId' open='(' separator=',' close=')'>" +
            " #{commentId} " +
            "</foreach>" +
            "</script>")
    List<CommentInfoNewest> selectByCommentIds(@Param("commentIds") List<String> commentIds);



    @Insert("INSERT IGNORE INTO `comment_info` (  " +
            " `info_id`,  " +
            " `comment_id`,  " +
            " `comment_fid`,  " +
            " `comment_mid`,  " +
            " `comment_time`,  " +
            " `content`,  " +
            " `pic_urls`,  " +
            " `praise_count`,  " +
            " `reply_count`,  " +
            " `from_ip`,  " +
            " `from_app_id`,  " +
            " `from_user_id`,  " +
            " `from_user_name`,  " +
            " `from_user_n_name `,  " +
            " `from_userface`,  " +
            " `status`  " +
            ")  " +
            "VALUES  " +
            " (  " +
            "  #{commentInfoAddData.infoId},  " +
            "  #{commentInfoAddData.commentId},  " +
            "  #{commentInfoAddData.commentFid},  " +
            "  #{commentInfoAddData.commentMid},  " +
            "  #{commentInfoAddData.commentTime},  " +
            "  #{commentInfoAddData.content},  " +
            "  #{picUrls},  " +
            "  0,  " +
            "  0,  " +
            "  #{commentInfoAddData.fromIp},  " +
            "  #{commentInfoAddData.appId},  " +
            "  #{commentInfoAddData.userId},  " +
            "  #{commentInfoAddData.userName},  " +
            "  #{commentInfoAddData.userNName},  " +
            "  #{commentInfoAddData.userface},  " +
            "  0 " +
            " );")
    Integer insert(@Param("commentInfoAddData")CommentInfoAddData commentInfoAddData,@Param("picUrls") String picUrls);
}
