package com.hoatv.sql.repository.proxy;

import com.hoatv.fwk.common.services.CheckedFunction;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResponseHandler<R> extends CheckedFunction<ResultSet, R> {

}
