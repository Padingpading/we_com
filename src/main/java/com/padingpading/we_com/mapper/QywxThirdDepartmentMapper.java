package com.padingpading.we_com.mapper;

import com.tobdev.qywxthird.model.entity.QywxThirdDepartment;
import org.apache.ibatis.annotations.Param;


public interface QywxThirdDepartmentMapper {

    QywxThirdDepartment  getDepartmentByCorpId(@Param("corp_id") String corpId);

    Integer  saveDepartment(QywxThirdDepartment Department);

    Integer deleteDepartmentByCorpId(@Param("corp_id") String corpId);

}
