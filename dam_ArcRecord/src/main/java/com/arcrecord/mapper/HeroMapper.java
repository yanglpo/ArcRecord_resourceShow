package com.arcrecord.mapper;

import com.arcrecord.entity.Role;
import com.arcrecord.entity.RoleCollection;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HeroMapper {

    List<Role> getHeroList();

    Role getHeroById(String id);

    int insertHero(Role hero);

    int updateHero(Role hero);

    int deleteHero(String id);

    List<RoleCollection> getRoleCollectionByHeroId(String heroId);
}