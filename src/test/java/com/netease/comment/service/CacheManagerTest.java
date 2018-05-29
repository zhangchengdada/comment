package com.netease.comment.service;

import com.netease.comment.utils.CacheManagerUtils;
import org.junit.Test;

public class CacheManagerTest {
    @Test
    public void writeModel() throws Exception {

        CacheManagerUtils.getInstance().writeModel("测试","~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~飘荡的一条线~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

}