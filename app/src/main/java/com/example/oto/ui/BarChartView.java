package com.example.oto.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.oto.data.relation.ChapterStat;

import java.util.ArrayList;
import java.util.List;

/**
 * Biểu đồ cột ngang tỷ lệ đúng theo từng chương — tự vẽ bằng Canvas,
 * không cần thư viện ngoài (tiêu chí 1.2: Chart).
 *
 * Màu cột: đỏ < 50% (yếu), cam 50–79% (trung bình), xanh >= 80% (tốt).
 */
public class BarChartView extends View {

    private static final int CAO_MOI_COT_DP = 44;
    private static final int LE_DP = 8;

    private final List<ChapterStat> data = new ArrayList<>();

    private final Paint pNen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pChu = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPhanTram = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float mauDo;

    public BarChartView(Context context) {
        this(context, null);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mauDo = getResources().getDisplayMetrics().density;

        pNen.setColor(Color.parseColor("#E0E0E0"));
        pChu.setColor(Color.parseColor("#616161"));
        pChu.setTextSize(12 * mauDo);
        pPhanTram.setTextSize(12 * mauDo);
        pPhanTram.setFakeBoldText(true);
    }

    public void setData(List<ChapterStat> stats) {
        data.clear();
        if (stats != null) {
            data.addAll(stats);
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = (int) (Math.max(data.size(), 1) * CAO_MOI_COT_DP * mauDo + LE_DP * mauDo);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (data.isEmpty()) {
            return;
        }

        float caoDong = CAO_MOI_COT_DP * mauDo;
        float caoCot = 14 * mauDo;
        float radius = caoCot / 2f;
        float rongDanhSachPhanTram = 44 * mauDo; // chừa chỗ ghi "85%"
        float rongToiDa = getWidth() - rongDanhSachPhanTram;

        for (int i = 0; i < data.size(); i++) {
            ChapterStat s = data.get(i);
            float top = i * caoDong;

            // Nhãn chương
            canvas.drawText("Chương " + s.soThuTu + " — " + s.dung + "/" + s.tong + " câu đúng",
                    0, top + 14 * mauDo, pChu);

            float yCot = top + 20 * mauDo;
            RectF nen = new RectF(0, yCot, rongToiDa, yCot + caoCot);
            canvas.drawRoundRect(nen, radius, radius, pNen);

            int pt = s.phanTram();
            pCot.setColor(mauTheoTyLe(pt));
            float rong = Math.max(rongToiDa * pt / 100f, pt > 0 ? radius * 2 : 0);
            RectF cot = new RectF(0, yCot, rong, yCot + caoCot);
            canvas.drawRoundRect(cot, radius, radius, pCot);

            pPhanTram.setColor(mauTheoTyLe(pt));
            canvas.drawText(pt + "%", rongToiDa + 6 * mauDo, yCot + caoCot - 2 * mauDo, pPhanTram);
        }
    }

    static int mauTheoTyLe(int phanTram) {
        if (phanTram < 50) {
            return Color.parseColor("#C62828"); // yếu
        }
        if (phanTram < 80) {
            return Color.parseColor("#EF6C00"); // trung bình
        }
        return Color.parseColor("#2E7D32"); // tốt
    }
}
