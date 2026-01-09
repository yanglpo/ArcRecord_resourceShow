package com.arcrecord.service.impl;

import com.arcrecord.entity.Role;

import java.util.List;

public interface HeroServiceimpl {

    List<Role> getHeroList();

    Role getHeroById(String id);

    Integer insertHero(Role hero);

    Integer updateHero(Role hero);

    Integer deleteHero(String id);

}
