package com.example.oto.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.oto.data.dao.AttemptDao;
import com.example.oto.data.dao.ChapterDao;
import com.example.oto.data.dao.ExamSetDao;
import com.example.oto.data.dao.NoteDao;
import com.example.oto.data.dao.QuestionDao;
import com.example.oto.data.dao.ReviewScheduleDao;
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
        version = 2,
        // Xuất lược đồ ra thư mục schemas/ và commit vào Git: Room dùng file này để
        // đối chiếu khi viết migration, và nhóm xem được lược đồ đổi những gì qua từng bản.
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ChapterDao chapterDao();

    public abstract QuestionDao questionDao();

    public abstract TrafficSignDao trafficSignDao();

    public abstract ExamSetDao examSetDao();

    public abstract AttemptDao attemptDao();

    public abstract UserDao userDao();

    public abstract NoteDao noteDao();

    public abstract ReviewScheduleDao reviewScheduleDao();

    private static volatile AppDatabase INSTANCE;

    /** Executor dùng chung cho mọi thao tác ghi/đọc DB ngoài luồng UI. */
    public static final ExecutorService IO = Executors.newFixedThreadPool(4);

    /**
     * Application context, giữ lại để seed đọc được file trong assets/.
     * Dùng application context (không phải Activity) nên không gây rò rỉ bộ nhớ.
     */
    private static Context appContext;

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "oto.db")
                            .addCallback(SEED_CALLBACK)
                            // Nâng cấp lược đồ bằng migration tường minh.
                            // KHÔNG dùng fallbackToDestructiveMigration: cách đó xoá sạch
                            // database mỗi lần đổi version, nghĩa là người dùng thật sẽ mất
                            // toàn bộ lịch sử thi và ghi chú sau một bản cập nhật.
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Nâng cấp lược đồ từ bản 1 lên bản 2: thêm cột questions.updated_at.
     *
     * NOT NULL DEFAULT 0 là bắt buộc — bảng đang có sẵn dữ liệu, SQLite cần biết
     * điền giá trị nào cho các dòng cũ. Giá trị 0 nghĩa là "chưa từng đồng bộ",
     * nên lần đồng bộ delta đầu tiên sẽ coi các câu này là cần gửi lên.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE questions ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Callback SEED_CALLBACK = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Seed chạy ở luồng nền, dùng lại INSTANCE đã build xong.
            IO.execute(() -> DatabaseSeeder.seed(INSTANCE, appContext));
        }
    };
}
