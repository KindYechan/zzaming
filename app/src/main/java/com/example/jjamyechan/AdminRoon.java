package com.example.jjamyechan;

public class AdminRoon {
    // (왼쪽부터) 부대코드, 부대이름, 닉네임, 부대타입, 설명, 수정권한(일부, 전체), 생성자 아이디
    String unitCode, unitName, nickname, unitType, description, editPermission, ownerId;
    // 기능 사용 여부 (캘린더, 퀘스트, 진급계산, 게시판, 전파사항, 마편)
    boolean useCalendar, useQuest, usePromotion, useNotice, useBroadcast, useLetter;
    // 소속 부대원
    int memberCount;
    // 활동 로그 기록 여부 (생성 2단계의 스위치)
    // Todo(아마도): 이녀석은 구현 안할 듯 싶긴 하다
    boolean activityLog;

    public AdminRoon() {
    }

    public AdminRoon(String unitName, String nickname, String unitType, String description, String editPermission, String ownerId,
                     boolean useCalendar, boolean useQuest, boolean usePromotion, boolean useNotice, boolean useBroadcast, boolean useLetter) {
        this.unitName = unitName;
        this.nickname = nickname;
        this.unitType = unitType;
        this.description = description;
        this.editPermission = editPermission;
        this.ownerId = ownerId;
        this.useCalendar = useCalendar;
        this.useQuest = useQuest;
        this.usePromotion = usePromotion;
        this.useNotice = useNotice;
        this.useBroadcast = useBroadcast;
        this.useLetter = useLetter;
    }

    // ───────── getter ─────────

    public String getUnitCode() {
        return unitCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUnitType() {
        return unitType;
    }

    public String getDescription() {
        return description;
    }

    public String getEditPermission() {
        return editPermission;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    // 기능 사용 여부를 AdminRoomActivity 가 쓰던 순서의 배열로

    // AdminRoomActivity의 기능들 배치가 행정반 생성 시트의 배치랑 달라서
    // 이 순서를 맞춰주기 위한 작업을 굳이굳이 다시 했습니다.
    // (캘린더, 퀘스트, 진급계산, 게시판, 전파사항, 마편)

    public boolean[] getFeatures() {
        return new boolean[]{useCalendar, useQuest, usePromotion, useNotice, useBroadcast, useLetter};
    }

    // ───────── setter ─────────

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEditPermission(String editPermission) {
        this.editPermission = editPermission;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public boolean isActivityLog() {
        return activityLog;
    }

    public void setActivityLog(boolean activityLog) {
        this.activityLog = activityLog;
    }
}
