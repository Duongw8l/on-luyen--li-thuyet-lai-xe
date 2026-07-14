package com.example.oto.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.oto.data.dao.AttemptDao;
import com.example.oto.data.dao.ChapterDao;
import com.example.oto.data.dao.ExamSetDao;
import com.example.oto.data.dao.NoteDao;
import com.example.oto.data.dao.QuestionDao;
import com.example.oto.data.dao.TrafficSignDao;
import com.example.oto.data.dao.UserDao;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Attempt;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.ExamSet;
import com.example.oto.data.entity.ExamSetQuestion;
import com.example.oto.data.entity.Note;
import com.example.oto.data.entity.Question;
import com.example.oto.data.entity.ReviewSchedule;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.data.entity.User;
import com.example.oto.data.entity.UserAnswer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                Chapter.class,
                Question.class,
                Answer.class,
                TrafficSign.class,
                ExamSet.class,
                ExamSetQuestion.class,
                User.class,
                Attempt.class,
                UserAnswer.class,
                ReviewSchedule.class,
                Note.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ChapterDao chapterDao();

    public abstract QuestionDao questionDao();

    public abstract TrafficSignDao trafficSignDao();

    public abstract ExamSetDao examSetDao();

    public abstract AttemptDao attemptDao();

    public abstract UserDao userDao();

    public abstract NoteDao noteDao();

    private static volatile AppDatabase INSTANCE;

    /** Executor dùng chung cho mọi thao tác ghi/đọc DB ngoài luồng UI. */
    public static final ExecutorService IO = Executors.newFixedThreadPool(4);

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "oto.db")
                            // Bật kiểm tra khóa ngoại của SQLite (đảm bảo toàn vẹn dữ liệu)
                            .addCallback(SEED_CALLBACK)
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Callback SEED_CALLBACK = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Seed chạy ở luồng nền, dùng lại INSTANCE đã build xong.
            IO.execute(() -> DatabaseSeeder.seed(INSTANCE));
        }
    };
}
