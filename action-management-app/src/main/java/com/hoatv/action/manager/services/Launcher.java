package com.hoatv.action.manager.services;

@FunctionalInterface
public interface Launcher {

    <TResult> TResult execute(String... args);
}
