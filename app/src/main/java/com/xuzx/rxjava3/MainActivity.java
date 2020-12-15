package com.xuzx.rxjava3;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.xuzx.rxjava3.databinding.ActivityMainBinding;

import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.textView.setText("Hello RxJava3");

        // Rxjava 线程切换
        Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                        Log.w(TAG, "ObservableOnSubscribe subscribe: " + Thread.currentThread().getName());
                        emitter.onNext(1);
                        emitter.onNext(2);
                        emitter.onNext(3);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Throwable {
                        Log.w(TAG, "ObservableMap apply: " + Thread.currentThread().getName());
                        return String.valueOf(integer);
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.computation())
                .flatMap(new Function<String, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(String s) throws Throwable {
                        Log.w(TAG, "ObservableFlatMap apply: " + Thread.currentThread().getName());
                        return Observable.just(Integer.valueOf(s));
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.w(TAG, "Observer onSubscribe: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(@NonNull Integer integer) {
                        Log.w(TAG, "Observer onNext: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.w(TAG, "Observer onComplete: " + Thread.currentThread().getName());
                    }
                });

        /* 以上代码拆分转换如下：
        // 1)
        Observable<Integer> observableCreate = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                Log.w(TAG, "ObservableOnSubscribe subscribe: " + Thread.currentThread().getName());
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });

        // 2)
        Observable<Integer> observableSubscribeOn1 = observableCreate.subscribeOn(Schedulers.io());

        // 3)
        Observable<Integer> observableObserveOn1 = observableSubscribeOn1.observeOn(AndroidSchedulers.mainThread());

        // 4)
        Observable<String> observableMap = observableObserveOn1.map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Throwable {
                Log.w(TAG, "ObservableMap apply: " + Thread.currentThread().getName());
                return String.valueOf(integer);
            }
        });

        // 5)
        Observable<String> observableSubscribeOn2 = observableMap.subscribeOn(Schedulers.single());

        // 6)
        Observable<String> observableObserveOn2 = observableSubscribeOn2.observeOn(Schedulers.computation());

        // 7)
        Observable<Integer> observableFlatMap = observableObserveOn2.flatMap(new Function<String, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(String s) throws Throwable {
                Log.w(TAG, "ObservableFlatMap apply: " + Thread.currentThread().getName());
                return Observable.just(Integer.valueOf(s));
            }
        });

        // 8)
        Observable<Integer> observableSubscribeOn3 = observableFlatMap.subscribeOn(Schedulers.newThread());

        // 9)
        Observable<Integer> observableObserveOn3 = observableSubscribeOn3.observeOn(AndroidSchedulers.mainThread());

        // 10)
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.w(TAG, "Observer onSubscribe: " + Thread.currentThread().getName());
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                Log.w(TAG, "Observer onNext: " + Thread.currentThread().getName());
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.w(TAG, "Observer onComplete: " + Thread.currentThread().getName());
            }
        };

        // 11) 调用订阅方法触发上游事件发送，
        observableObserveOn3.subscribe(observer);
         */

        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                for (int i = 0;i < 1000000; i++) {
                    emitter.onNext("i = "+i);
                }
            }
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e("tag","----> "+s);
                    }
                });

    }
}