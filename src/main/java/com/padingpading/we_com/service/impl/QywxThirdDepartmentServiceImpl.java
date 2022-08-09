package com.padingpading.we_com.service.impl;

import com.tobdev.qywxthird.config.QywxThirdConfig;
import com.tobdev.qywxthird.mapper.QywxThirdDepartmentMapper;
import com.tobdev.qywxthird.model.entity.QywxThirdDepartment;
import com.tobdev.qywxthird.service.QywxThirdDepartmentService;
import com.tobdev.qywxthird.service.QywxThirdService;
import com.tobdev.qywxthird.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class QywxThirdDepartmentServiceImpl implements QywxThirdDepartmentService {

    private final static Logger logger = LoggerFactory.getLogger("test");

    @Autowired
    private QywxThirdDepartmentMapper qywxThirdDepartmentMapper;

    @Autowired
    private QywxThirdConfig qywxThirdConfig;
    @Autowired
    private RedisUtils strRedis;
    @Autowired
    private QywxThirdService qywxThirdService;


    @Override
    public QywxThirdDepartment getDepartmentByCorpId(String corpId) {
        return qywxThirdDepartmentMapper.getDepartmentByCorpId(corpId);
    }

    @Override
    public Integer saveDepartment(QywxThirdDepartment Department){
        return qywxThirdDepartmentMapper.saveDepartment(Department);
    }

    public Boolean deleteDepartmentByCorpId(String corpId){
        Integer result = qywxThirdDepartmentMapper.deleteDepartmentByCorpId(corpId);
        return  false;
    }


}
