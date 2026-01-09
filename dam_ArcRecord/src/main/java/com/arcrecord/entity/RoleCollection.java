package com.arcrecord.entity;

import java.io.Serializable;

public class RoleCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    // 配置唯一主键
    private String id;

    // 关联角色ID
    private String roleId;

    // 配置展示名称
    private String title;

    // 配置类型（枚举：亲密/觉醒/任务/剧情）
    private String type;

    // 触发所需亲密度等级
    private String intimacyLv;

    // 奖励线ID
    private String lineRewardId;

    // 角色觉醒等级门槛
    private String awakenLv;

    // 触发场景ID
    private String sceneId;

    // 触发所需道具ID
    private String itemId;

    // 互动图标资源路径
    private String prefabIcon;

    // 互动预制体路径
    private String prefab;

    // 关联剧情ID
    private String dramaIds;

    // 角色动作ID
    private String roleMotions;

    // UI是否高亮展示（1=高亮，0=不高亮）
    private String highlight;

    // 安全模式开关（1=开启，0=关闭）
    private String isSafeMode;

    // 关联运营活动ID
    private String activityIds;

    // Getter & Setter（全字段）
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIntimacyLv() {
        return intimacyLv;
    }

    public void setIntimacyLv(String intimacyLv) {
        this.intimacyLv = intimacyLv;
    }

    public String getLineRewardId() {
        return lineRewardId;
    }

    public void setLineRewardId(String lineRewardId) {
        this.lineRewardId = lineRewardId;
    }

    public String getAwakenLv() {
        return awakenLv;
    }

    public void setAwakenLv(String awakenLv) {
        this.awakenLv = awakenLv;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getPrefabIcon() {
        return prefabIcon;
    }

    public void setPrefabIcon(String prefabIcon) {
        this.prefabIcon = prefabIcon;
    }

    public String getPrefab() {
        return prefab;
    }

    public void setPrefab(String prefab) {
        this.prefab = prefab;
    }

    public String getDramaIds() {
        return dramaIds;
    }

    public void setDramaIds(String dramaIds) {
        this.dramaIds = dramaIds;
    }

    public String getRoleMotions() {
        return roleMotions;
    }

    public void setRoleMotions(String roleMotions) {
        this.roleMotions = roleMotions;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public String getIsSafeMode() {
        return isSafeMode;
    }

    public void setIsSafeMode(String isSafeMode) {
        this.isSafeMode = isSafeMode;
    }

    public String getActivityIds() {
        return activityIds;
    }

    public void setActivityIds(String activityIds) {
        this.activityIds = activityIds;
    }

    // toString方法（可选，便于日志打印/调试）
    @Override
    public String toString() {
        return "RoleAwakenConfig{" +
                "id='" + id + '\'' +
                ", roleId='" + roleId + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", intimacyLv=" + intimacyLv +
                ", lineRewardId='" + lineRewardId + '\'' +
                ", awakenLv=" + awakenLv +
                ", sceneId='" + sceneId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", prefabIcon='" + prefabIcon + '\'' +
                ", prefab='" + prefab + '\'' +
                ", dramaIds='" + dramaIds + '\'' +
                ", roleMotions='" + roleMotions + '\'' +
                ", highlight=" + highlight +
                ", isSafeMode=" + isSafeMode +
                ", activityIds='" + activityIds + '\'' +
                '}';
    }
}