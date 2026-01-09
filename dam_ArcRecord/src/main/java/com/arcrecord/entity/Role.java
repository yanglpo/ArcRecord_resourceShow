package com.arcrecord.entity;

public class Role {
    private String id; // 角色唯一编号
    private String title; // 称号文本键
    private String name; // 名字文本键
    private String initStar; // 初始星级
    private String element; // 元素属性
    private String job; // 职业
    private String constellation; // 星座
    private String grp; // 所属阵营(原GROUP)
    private String cupSize; // 罩杯
    private String birthday; // 生日 MM-DD
    private String height; // 身高(cm)
    private String weight; // 体重(kg)
    private String isHero; // 是否可上阵
    private String canSell; // 能否出售
    private String isr18; // 是否成人内容
    private String isSteamSafeMode; // Steam是否走安全模式
    private String imprintGroup; // 印记分组
    private String skillIds; // 技能ID列表(~分隔)
    private String spType; // SP类型
    private String initSp; // 初始SP
    private String hurtSp; // 受击回SP
    private String spineObject; // spine骨骼路径
    private String prototypeId; // 原型ID
    private String soundLanguages; // 配音语言列表
    private String lobbyTouchSounds; // 大厅触摸语音
    private String sexTouchSounds; // 成人触摸语音
    private String counterSounds; // 反击语音
    private String hurtSounds; // 受伤语音
    private String deadSounds; // 死亡语音
    private String readySounds; // 入场语音
    private String victorySounds; // 胜利语音
    private String defeatSounds; // 失败语音
    private String summonSounds; // 召唤语音
    private String giftSounds; // 送礼语音
    private String relvup1Sounds; // 突破1语音
    private String relvup2Sounds; // 突破2语音
    private String relvup3Sounds; // 突破3语音
    private String relvup4Sounds; // 突破4语音
    private String skill1Sounds; // 技能1语音
    private String skill2Sounds; // 技能2语音
    private String skill3Sounds; // 技能3语音
    private String headB; // 头像大图标
    private String headS; // 头像小图标
    private String headM; // 头像中图标
    private String headL; // 头像特大图标
    private String ais; // AI策略集
    private String teamImprint; // 队伍印记加成描述
    private String selfImprint; // 自身印记加成描述
    private String imprintPositions; // 印记槽位序号
    private String minPropDynamicField1; // 最小随机属性值
    private String maxPropDynamicField1; // 最大随机属性值
    private String cutinPrefab; // 大招切入特效
    private String cutinSound; // 大招切入音效
    private String hp; // 初始生命
    private String attack; // 初始攻击
    private String defence; // 初始防御
    private String speed; // 初始速度
    private String criticalRate; // 暴击率(小数)
    private String criticalDamageRate; // 暴击伤害倍率
    private String effectHitRate; // 效果命中
    private String resistanceRate; // 效果抵抗
    private String pinchRate; // 闪避率
    private String dynamicField1; // 预留扩展1
    private String dynamicField2; // 预留扩展2
    private String dynamicField3; // 预留扩展3
    private String rolePropertyId; // 属性成长曲线ID
    private String description; // 角色描述文本键
    private String versionControl; // 数据版本号
    private String realName; //名字

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitStar() {
        return initStar;
    }

    public void setInitStar(String initStar) {
        this.initStar = initStar;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getConstellation() {
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public String getGrp() {
        return grp;
    }

    public void setGrp(String grp) {
        this.grp = grp;
    }

    public String getCupSize() {
        return cupSize;
    }

    public void setCupSize(String cupSize) {
        this.cupSize = cupSize;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getIsHero() {
        return isHero;
    }

    public void setIsHero(String isHero) {
        this.isHero = isHero;
    }

    public String getCanSell() {
        return canSell;
    }

    public void setCanSell(String canSell) {
        this.canSell = canSell;
    }

    public String getIsr18() {
        return isr18;
    }

    public void setIsr18(String isr18) {
        this.isr18 = isr18;
    }

    public String getIsSteamSafeMode() {
        return isSteamSafeMode;
    }

    public void setIsSteamSafeMode(String isSteamSafeMode) {
        this.isSteamSafeMode = isSteamSafeMode;
    }

    public String getImprintGroup() {
        return imprintGroup;
    }

    public void setImprintGroup(String imprintGroup) {
        this.imprintGroup = imprintGroup;
    }

    public String getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(String skillIds) {
        this.skillIds = skillIds;
    }

    public String getSpType() {
        return spType;
    }

    public void setSpType(String spType) {
        this.spType = spType;
    }

    public String getInitSp() {
        return initSp;
    }

    public void setInitSp(String initSp) {
        this.initSp = initSp;
    }

    public String getHurtSp() {
        return hurtSp;
    }

    public void setHurtSp(String hurtSp) {
        this.hurtSp = hurtSp;
    }

    public String getSpineObject() {
        return spineObject;
    }

    public void setSpineObject(String spineObject) {
        this.spineObject = spineObject;
    }

    public String getPrototypeId() {
        return prototypeId;
    }

    public void setPrototypeId(String prototypeId) {
        this.prototypeId = prototypeId;
    }

    public String getSoundLanguages() {
        return soundLanguages;
    }

    public void setSoundLanguages(String soundLanguages) {
        this.soundLanguages = soundLanguages;
    }

    public String getLobbyTouchSounds() {
        return lobbyTouchSounds;
    }

    public void setLobbyTouchSounds(String lobbyTouchSounds) {
        this.lobbyTouchSounds = lobbyTouchSounds;
    }

    public String getSexTouchSounds() {
        return sexTouchSounds;
    }

    public void setSexTouchSounds(String sexTouchSounds) {
        this.sexTouchSounds = sexTouchSounds;
    }

    public String getCounterSounds() {
        return counterSounds;
    }

    public void setCounterSounds(String counterSounds) {
        this.counterSounds = counterSounds;
    }

    public String getHurtSounds() {
        return hurtSounds;
    }

    public void setHurtSounds(String hurtSounds) {
        this.hurtSounds = hurtSounds;
    }

    public String getDeadSounds() {
        return deadSounds;
    }

    public void setDeadSounds(String deadSounds) {
        this.deadSounds = deadSounds;
    }

    public String getReadySounds() {
        return readySounds;
    }

    public void setReadySounds(String readySounds) {
        this.readySounds = readySounds;
    }

    public String getVictorySounds() {
        return victorySounds;
    }

    public void setVictorySounds(String victorySounds) {
        this.victorySounds = victorySounds;
    }

    public String getDefeatSounds() {
        return defeatSounds;
    }

    public void setDefeatSounds(String defeatSounds) {
        this.defeatSounds = defeatSounds;
    }

    public String getSummonSounds() {
        return summonSounds;
    }

    public void setSummonSounds(String summonSounds) {
        this.summonSounds = summonSounds;
    }

    public String getGiftSounds() {
        return giftSounds;
    }

    public void setGiftSounds(String giftSounds) {
        this.giftSounds = giftSounds;
    }

    public String getRelvup1Sounds() {
        return relvup1Sounds;
    }

    public void setRelvup1Sounds(String relvup1Sounds) {
        this.relvup1Sounds = relvup1Sounds;
    }

    public String getRelvup2Sounds() {
        return relvup2Sounds;
    }

    public void setRelvup2Sounds(String relvup2Sounds) {
        this.relvup2Sounds = relvup2Sounds;
    }

    public String getRelvup3Sounds() {
        return relvup3Sounds;
    }

    public void setRelvup3Sounds(String relvup3Sounds) {
        this.relvup3Sounds = relvup3Sounds;
    }

    public String getRelvup4Sounds() {
        return relvup4Sounds;
    }

    public void setRelvup4Sounds(String relvup4Sounds) {
        this.relvup4Sounds = relvup4Sounds;
    }

    public String getSkill1Sounds() {
        return skill1Sounds;
    }

    public void setSkill1Sounds(String skill1Sounds) {
        this.skill1Sounds = skill1Sounds;
    }

    public String getSkill2Sounds() {
        return skill2Sounds;
    }

    public void setSkill2Sounds(String skill2Sounds) {
        this.skill2Sounds = skill2Sounds;
    }

    public String getSkill3Sounds() {
        return skill3Sounds;
    }

    public void setSkill3Sounds(String skill3Sounds) {
        this.skill3Sounds = skill3Sounds;
    }

    public String getHeadB() {
        return headB;
    }

    public void setHeadB(String headB) {
        this.headB = headB;
    }

    public String getHeadS() {
        return headS;
    }

    public void setHeadS(String headS) {
        this.headS = headS;
    }

    public String getHeadM() {
        return headM;
    }

    public void setHeadM(String headM) {
        this.headM = headM;
    }

    public String getHeadL() {
        return headL;
    }

    public void setHeadL(String headL) {
        this.headL = headL;
    }

    public String getAis() {
        return ais;
    }

    public void setAis(String ais) {
        this.ais = ais;
    }

    public String getTeamImprint() {
        return teamImprint;
    }

    public void setTeamImprint(String teamImprint) {
        this.teamImprint = teamImprint;
    }

    public String getSelfImprint() {
        return selfImprint;
    }

    public void setSelfImprint(String selfImprint) {
        this.selfImprint = selfImprint;
    }

    public String getImprintPositions() {
        return imprintPositions;
    }

    public void setImprintPositions(String imprintPositions) {
        this.imprintPositions = imprintPositions;
    }

    public String getMinPropDynamicField1() {
        return minPropDynamicField1;
    }

    public void setMinPropDynamicField1(String minPropDynamicField1) {
        this.minPropDynamicField1 = minPropDynamicField1;
    }

    public String getMaxPropDynamicField1() {
        return maxPropDynamicField1;
    }

    public void setMaxPropDynamicField1(String maxPropDynamicField1) {
        this.maxPropDynamicField1 = maxPropDynamicField1;
    }

    public String getCutinPrefab() {
        return cutinPrefab;
    }

    public void setCutinPrefab(String cutinPrefab) {
        this.cutinPrefab = cutinPrefab;
    }

    public String getCutinSound() {
        return cutinSound;
    }

    public void setCutinSound(String cutinSound) {
        this.cutinSound = cutinSound;
    }

    public String getHp() {
        return hp;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }

    public String getAttack() {
        return attack;
    }

    public void setAttack(String attack) {
        this.attack = attack;
    }

    public String getDefence() {
        return defence;
    }

    public void setDefence(String defence) {
        this.defence = defence;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getCriticalRate() {
        return criticalRate;
    }

    public void setCriticalRate(String criticalRate) {
        this.criticalRate = criticalRate;
    }

    public String getCriticalDamageRate() {
        return criticalDamageRate;
    }

    public void setCriticalDamageRate(String criticalDamageRate) {
        this.criticalDamageRate = criticalDamageRate;
    }

    public String getEffectHitRate() {
        return effectHitRate;
    }

    public void setEffectHitRate(String effectHitRate) {
        this.effectHitRate = effectHitRate;
    }

    public String getResistanceRate() {
        return resistanceRate;
    }

    public void setResistanceRate(String resistanceRate) {
        this.resistanceRate = resistanceRate;
    }

    public String getPinchRate() {
        return pinchRate;
    }

    public void setPinchRate(String pinchRate) {
        this.pinchRate = pinchRate;
    }

    public String getDynamicField1() {
        return dynamicField1;
    }

    public void setDynamicField1(String dynamicField1) {
        this.dynamicField1 = dynamicField1;
    }

    public String getDynamicField2() {
        return dynamicField2;
    }

    public void setDynamicField2(String dynamicField2) {
        this.dynamicField2 = dynamicField2;
    }

    public String getDynamicField3() {
        return dynamicField3;
    }

    public void setDynamicField3(String dynamicField3) {
        this.dynamicField3 = dynamicField3;
    }

    public String getRolePropertyId() {
        return rolePropertyId;
    }

    public void setRolePropertyId(String rolePropertyId) {
        this.rolePropertyId = rolePropertyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersionControl() {
        return versionControl;
    }

    public void setVersionControl(String versionControl) {
        this.versionControl = versionControl;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
