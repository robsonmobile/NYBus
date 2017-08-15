/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mindorks.nybus.internal;

import com.mindorks.nybus.Scheduler.SchedulerProvider;
import com.mindorks.nybus.annotations.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Jyoti on 14/08/17.
 */

public class NYBusHandler {
    private SchedulerProvider mSchedulerProvider;
    private ConcurrentHashMap<String, Set<SubscribeMethodHolder>> mChannelToTargetsMap;
    private PublishSubject<Event> subject;

    public NYBusHandler() {
        mChannelToTargetsMap = new ConcurrentHashMap<>();
        subject = PublishSubject.create();
        subject.toFlowable(BackpressureStrategy.BUFFER)
                .subscribe(new Consumer<Event>() {
                    @Override
                    public void accept(@NonNull Event event) throws Exception {
                        Set<SubscribeMethodHolder> targets = mChannelToTargetsMap.get(event.channelId);
                        for (SubscribeMethodHolder target : targets) {
                            if(target.getSubscribedEventType().isInstance(event.object)){
                                deliver(target, event.object);
                            }
                        }
                    }

                });
    }

    private void deliver(SubscribeMethodHolder subscribeMethodHolder, Object object) {
        try {
                Method method = subscribeMethodHolder.getSubscribedMethod();
                method.setAccessible(true);
                method.invoke(subscribeMethodHolder.getSubscriberTarget(), object);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    public void setSchedulerProvider(SchedulerProvider mSchedulerProvider) {
        this.mSchedulerProvider = mSchedulerProvider;
    }

    public void register(Object object, String channelId) {
        List<Method> subscribeAnnotatedMethods = provideMethodsWithSubscribeAnnotation(object.getClass());
        if (subscribeAnnotatedMethods.size() != 0) {
            for (Method method : subscribeAnnotatedMethods) {
                SubscribeMethodHolder subscribeMethodHolder = new SubscribeMethodHolder(object, method, method.getParameterTypes()[0]);
                if (mChannelToTargetsMap.containsKey(channelId)) {
                    Set<SubscribeMethodHolder> subscribeMethodHolders = mChannelToTargetsMap.get(channelId);
                    subscribeMethodHolders.add(subscribeMethodHolder);
                    mChannelToTargetsMap.put(channelId, subscribeMethodHolders);
                } else {
                    Set<SubscribeMethodHolder> subscribeMethodHolders = new HashSet<>();
                    subscribeMethodHolders.add(subscribeMethodHolder);
                    mChannelToTargetsMap.put(channelId, subscribeMethodHolders);
                }
            }
        }


    }


    public void unregister(Object object, String channelId) {
        Set<SubscribeMethodHolder> targets = mChannelToTargetsMap.get(channelId);
        try {
            targets.remove(object);
            mChannelToTargetsMap.put(channelId, targets);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    /**
     * TODO Add all constraints for @subscribe method
     * 1. Public
     * 2. Return type void
     * 3. Single param
     * @param subscriber
     * @return
     */
    private List<Method> provideMethodsWithSubscribeAnnotation(Class<?> subscriber) {
        List<Method> subscribeAnnotatedMethods = new ArrayList<>();
        Method[] declaredMethods = subscriber.getDeclaredMethods();
        for (Method method : declaredMethods) {
            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
            if (subscribeAnnotation != null && (method.getModifiers() & Modifier.PUBLIC) != 0) {
                subscribeAnnotatedMethods.add(method);
            }
        }
        return subscribeAnnotatedMethods;

    }

    public void post(Object object, String channelId) {
        subject.onNext(new Event(object, channelId));
    }

}
