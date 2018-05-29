package com.netease.comment.mapper;



import com.netease.comment.model.UserDomain;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao {


    @Insert("INSERT INTO `t_user` ( `userName`, `password`, `phone`) VALUES (#{record.userName}, #{record.password}, #{record.phone});")
    int insert(@Param("record") UserDomain record);


    @Results({
            @Result(property = "userId", column = "userId"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "userName", column = "userName")
    })
    @Select("select * from t_user")
    List<UserDomain> selectUsers();
}