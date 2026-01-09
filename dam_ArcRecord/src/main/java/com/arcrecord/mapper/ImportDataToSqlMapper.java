package com.arcrecord.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ImportDataToSqlMapper {

    Integer insertChs(List<Map<String,String>> list);

    String getRoleName(String deskey);

    Integer insertRole(List<Map<String,String>> list);

    Integer insertLineDialog(List<Map<String,String>> list);

    Integer insertroleCollection(List<Map<String,String>> list);

    Integer insertreadLineDrama(List<Map<String,String>> list);




}