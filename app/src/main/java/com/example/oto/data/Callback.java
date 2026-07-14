package com.example.oto.data;

/** Callback đơn giản để trả kết quả truy vấn nền về luồng UI. */
public interface Callback<T> {
    void onResult(T result);
}
