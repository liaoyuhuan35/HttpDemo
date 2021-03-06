package com.yui.libhttp;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Jane on 2018/1/22.
 */

public class RetryWhenFun implements Func1<Observable<? extends Throwable>, Observable<?>> {
    /**
     * retry次数
     */
    private int count = 3;
    /**
     * 延迟
     */
    private long delay = 3000;
    /**
     * 叠加延迟
     */
    private long increaseDelay = 3000;

    public RetryWhenFun() {
    }

    public RetryWhenFun(int count, long delay) {
        this.count = count;
        this.delay = delay;
    }

    public RetryWhenFun(int count, long delay, long increaseDelay) {
        this.count = count;
        this.delay = delay;
        this.increaseDelay = increaseDelay;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .zipWith(Observable.range(1, count + 1), new Func2<Throwable, Integer, Wrapper>() {
                    @Override
                    public Wrapper call(Throwable throwable, Integer integer) {
                        return new Wrapper(throwable, integer);
                    }
                }).flatMap(new Func1<Wrapper, Observable<?>>() {
                    /**
                     * @param wrapper
                     *
                     * @return
                     */
                    @Override
                    public Observable<?> call(Wrapper wrapper) {
                        /**异常类型：超时，连接错误*/
                        boolean wrapperCheck = wrapper.throwable instanceof ConnectException
                                || wrapper.throwable instanceof SocketTimeoutException
                                || wrapper.throwable instanceof TimeoutException;
                        /**连接错误次数计数*/
                        boolean indexCheck = wrapper.index < count + 1;
                        if (wrapperCheck && indexCheck) {
                            /**如果超出重试次数也抛出错误，否则默认是会进入onCompleted*/
                            return Observable.timer(delay + (wrapper.index - 1) * increaseDelay, TimeUnit.MILLISECONDS);

                        }
                        return Observable.error(wrapper.throwable);
                    }
                });
    }

    private class Wrapper {
        private int index;
        /**
         * 错误
         */
        private Throwable throwable;

        public Wrapper(Throwable throwable, int index) {
            this.index = index;
            this.throwable = throwable;
        }
    }
}
