package com.example.jjamyechan.community;

import com.example.jjamyechan.community.model.Comment;
import com.example.jjamyechan.community.model.Post;
import com.example.jjamyechan.community.model.ScheduleItem;
import com.example.jjamyechan.community.model.User;

import java.util.List;

/**
 * 캘린더(일정)와 게시판(글, 댓글)을 주고받는 창구.
 *
 * 지금 구현: MockServerDataSource (캘린더·게시판 담당이 만든 더미 서버, 기기 안에 저장)
 * 서버 담당이 만들 것: FirestoreCommunityRepository implements CommunityRepository
 *   - 일정: "units/{unitCode}/schedules", 글: "units/{unitCode}/posts", 댓글: "posts/{postId}/comments"
 * 그다음 DataProvider.community() 한 줄만 바꾸면 돼요.
 */
public interface CommunityRepository {

    // ───────── 결과를 돌려받는 통로 (원래 MockServerDataSource 안에 있던 것을 옮겨 왔어요) ─────────
    interface OnScheduleLoadListener {
        void onSuccess(List<ScheduleItem> scheduleList);
        void onFailure(Exception e);
    }

    interface OnPostLoadListener {
        void onSuccess(List<Post> postList);
        void onFailure(Exception e);
    }

    interface OnCommentLoadListener {
        void onSuccess(List<Comment> commentList);
        void onFailure(Exception e);
    }

    interface OnCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    // ───────── 지금 로그인한 사람 (UserManager 의 유저를 게시판·캘린더용 모양으로 바꿔서 줘요) ─────────
    User getCurrentUser();

    // ───────── 일정 ─────────
    void fetchServerSchedules(OnScheduleLoadListener listener);

    void fetchServerUnitSchedules(OnScheduleLoadListener listener);

    void publishSchedule(ScheduleItem schedule, OnCompleteListener listener);

    // ───────── 게시판 ─────────
    void fetchServerPosts(OnPostLoadListener listener);

    void publishPost(Post post, OnCompleteListener listener);

    void deletePost(String postId, OnCompleteListener listener);

    void updatePostLikes(String postId, int newLikes, OnCompleteListener listener);

    // ───────── 댓글 ─────────
    void fetchCommentsForPost(String postId, OnCommentLoadListener listener);

    void addCommentToPost(String postId, Comment comment, OnCompleteListener listener);

    void deleteCommentFromPost(String postId, String commentId, OnCompleteListener listener);
}
