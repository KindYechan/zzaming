package com.example.jjamyechan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/*
유저 정보를 하나의 클래스로 만들어서 관리한다.
회원 가입시 부대코드 및 역할을 제외한 모든 정보를 받는다.
부대코드 및 역할은 행정반 생성 및 가입 시에 입력받는다.

진급일은 실제로 값을 저장해두지 않고, 매 순간 조건에 따라서 진급일을 계산 및 출력

주로 MainActivity의 유저 카드 출력, AdminRoomActivity의 유저 정보 및 진급일 계산에 사용되는 class
 */
public class User {

    // (왼쪽부터) 이메일, 이름, 병or간부, 계급, 입대일, 전역일, 부대코드, 역할(수정권한)
    private String email, name, memberType, rank, enlistmentDate, dischargeDate, unitCode, role;

    private String pfcDate;        // 일병 진급일 "yyyy-MM-dd" (아직이면 null)
    private String corporalDate;   // 상병 진급일
    private String sergeantDate;   // 병장 진급일
    public User() {
    }

    public User(String email, String name, String memberType, String rank, String enlistmentDate, String dischargeDate) {
        this.email = email;
        this.name = name;
        this.memberType = memberType;
        this.rank = rank;
        this.enlistmentDate = enlistmentDate;
        this.dischargeDate = dischargeDate;
    }


    // ───────── getter ─────────


    public String getPfcDate() {
        return pfcDate;
    }

    public String getCorporalDate() {
        return corporalDate;
    }

    public String getSergeantDate() {
        return sergeantDate;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getMemberType() {
        return memberType;
    }

    public String getRank() {
        return rank;
    }

    public String getEnlistmentDate() {
        return enlistmentDate;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public String getRole() {
        return role;
    }

    // ───────── setter ─────────


    public void setPfcDate(String pfcDate) {
        this.pfcDate = pfcDate;
    }

    public void setCorporalDate(String corporalDate) {
        this.corporalDate = corporalDate;
    }

    public void setSergeantDate(String sergeantDate) {
        this.sergeantDate = sergeantDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setEnlistmentDate(String enlistmentDate) {
        this.enlistmentDate = enlistmentDate;
    }

    public void setDischargeDate(String dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ───────── 계산 ─────────

    // 남은 전역일 계산
    public Long getLeftDays() {
        LocalDate end = LocalDate.parse(dischargeDate);
        return ChronoUnit.DAYS.between(LocalDate.now(), end);
    }

    // 남은 월급일 계산
    public long getSalaryDay() {
        long salaryDay = LocalDate.now().lengthOfMonth() + 10;
        return salaryDay - LocalDate.now().getDayOfMonth();
    }

    // 복무율 progress
    public double getServiceProgress() {
        double allDay = ChronoUnit.DAYS.between(LocalDate.parse(enlistmentDate), LocalDate.parse(dischargeDate));
        return Math.floor((1 - getLeftDays() / allDay) * 1000) / 10;
    }

    // 총 적금
    public long getTotalSalary() {
        LocalDate start = LocalDate.parse(enlistmentDate);
        LocalDate end = LocalDate.parse(dischargeDate);
        LocalDate today = LocalDate.now();
        LocalDate limit = today.isBefore(end) ? today : end;

        // 입대 후 첫 월급날 찾기
        LocalDate payday = start.withDayOfMonth(10);
        if (payday.isBefore(start)) {
            payday = payday.plusMonths(1);
        }

        long total = 0;
        while (!payday.isAfter(limit)) {    // 지나간 월급날마다
            total += 550000;                // 적금을 더함
            payday = payday.plusMonths(1);
        }
        return total;
    }

    // 계급별 월급
    public int getSalaryOf(String rank) {
        if (rank == null) return 0;         // 간부 등
        switch (rank) {
            case "private": return 750000;
            case "pfc": return 900000;
            case "corporal": return 1200000;
            case "sergeant": return 1500000;
            default: return 0;
        }
    }

    // 현재 계급의 월급
    public int getCurrentSalary() {
        return getSalaryOf(getRank());
    }

    public double getSavingAccountPercent() {
        // 지금까지 적금 / 990만 * 1000 하고 바닥함수, 나누기 10
        return getTotalSalary() / 99000;
    }

    // 그 계급으로 이미 진급했는지 (진급일이 저장돼 있으면 true)
    public boolean isPromotedTo(String rank) {
        switch (rank) {
            case "pfc":      return pfcDate != null;
            case "corporal": return corporalDate != null;
            case "sergeant": return sergeantDate != null;
            default:         return true;   // 이병은 입대하면 바로
        }
    }

    // 그 계급으로 진급하는 날 (진급했으면 실제 날짜, 아니면 정상 예정일)

    // 진급계산기 카드에 사용됩니다.
    // 또한 진급일 및 진급예정일을 보유하고있는 유저 클래스 데이터 계산에 사용

    public LocalDate getPromotionDate(String rank) {
        switch (rank) {
            case "pfc":
                if (pfcDate != null) return LocalDate.parse(pfcDate);
                // 입대한 달 1일 + 2개월
                return LocalDate.parse(enlistmentDate).withDayOfMonth(1).plusMonths(2);
            case "corporal":
                if (corporalDate != null) return LocalDate.parse(corporalDate);
                return getPromotionDate("pfc").plusMonths(6);        // 일병 진급일 + 6개월
            case "sergeant":
                if (sergeantDate != null) return LocalDate.parse(sergeantDate);
                return getPromotionDate("corporal").plusMonths(6);   // 상병 진급일 + 6개월
            default:
                return LocalDate.parse(enlistmentDate);
        }
    }

    // ───────── 계급 이름 (화면 표시용) ─────────

    // 계급 코드 → 한글. 예) "pfc" → "일병"
    // static 이라서 User 객체 없이 User.rankToKorean("pfc") 처럼 쓸 수 있어요

    // 계급 코드를 영어 데이터로 저장 및 계산하는데
    // 메인화면, 행정반 화면에서는 이를 한국어로 표시해야 하기 때문에 존재
    // 홈의 유저 카드, xp 바, 행정반의 ~~까지 몇일 표시할 때 사용
    // MainActivity 및 AdminRoomActivity 에서 사용합니다.
    public static String rankToKorean(String rank) {
        if (rank == null) return "간부";
        switch (rank) {
            case "private":             return "이병";
            case "pfc":
            case "private-first-class": return "일병";   // 내 정보 수정 화면은 이 코드를 써요
            case "corporal":            return "상병";
            case "sergeant":            return "병장";
            default:                    return rank;
        }
    }

    // 다음 계급 코드. 병장이거나 간부면 null

    // 진급계산기 카드에서 병장 및 간부 예외처리에 사용되는 함수
    // AdminRoomActivity 에서 사용합니다.
    public static String nextRank(String rank) {
        if (rank == null) return null;
        switch (rank) {
            case "private":             return "pfc";
            case "pfc":
            case "private-first-class": return "corporal";
            case "corporal":            return "sergeant";
            default:                    return null;
        }
    }

    // ───────── Object 메서드 ─────────

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", memberType='" + memberType + '\'' +
                ", rank='" + rank + '\'' +
                ", enlistmentDate='" + enlistmentDate + '\'' +
                ", dischargeDate='" + dischargeDate + '\'' +
                ", unitCode='" + unitCode + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
