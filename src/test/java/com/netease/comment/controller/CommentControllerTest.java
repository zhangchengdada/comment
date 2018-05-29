package com.netease.comment.controller;

import com.netease.comment.dto.CommentInfoAddData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommentControllerTest {

    @Autowired
    CommentController commentController;

    @Test
    public void infoQuery() throws Exception {
    }

    @Test
    public void infoAdd() throws Exception {
        commentController.infoAdd(new CommentInfoAddData());
    }

}