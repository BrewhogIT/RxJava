package com.brewhog.android.rxjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

public class BooksActivity extends AppCompatActivity {
    private Disposable bookSubscription; // Observer от котого можно отписаться
    private RecyclerView bookRecyclerView;
    private ProgressBar progressBar;
    private SimpleStringAdapter stringAdapter;
    private RestClient restClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restClient = new RestClient(this);
        configureLayout();
        createObservable();
    }

    private void createObservable() {
        Observable<List<String>> booksObservable = Observable
                .fromCallable(()-> restClient.getFavoriteBooks()); //возвращенный результат оборачивается в Observable

        bookSubscription = booksObservable
                .subscribeOn(Schedulers.io())  //выполнение операции в потоке (в фоновом)
                .observeOn(AndroidSchedulers.mainThread()) // возвращаем результат в нужный поток (в Main) , по сути замена хэндлера
                .subscribe(listStrings -> displayBooks(listStrings)); // this::displayBooks
    }

    private void displayBooks(List<String> books){
        stringAdapter.setStrings(books);
        progressBar.setVisibility(View.GONE);
        bookRecyclerView.setVisibility(View.VISIBLE);
    }

    private void configureLayout() {
        setContentView(R.layout.activity_books);
        progressBar = findViewById(R.id.loader);
        bookRecyclerView = findViewById(R.id.books_list);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stringAdapter = new SimpleStringAdapter(this);
        bookRecyclerView.setAdapter(stringAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //отписка
        if (bookSubscription != null && !bookSubscription.isDisposed()){
            bookSubscription.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookSubscription != null && !bookSubscription.isDisposed()){
            bookSubscription.dispose();
        }
    }


}
