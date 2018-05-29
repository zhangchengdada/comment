package com.netease.comment.mapper;

import com.netease.comment.dto.CommentInfoAddData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoPraiseStatisticsMapper {


    @Insert("")
    Integer insert(@Param("commentInfoAddData") CommentInfoAddData commentInfoAddData);

    @Select("select info_id from info_praise_statistics")
    List<String> selectAllInfoId();


}
