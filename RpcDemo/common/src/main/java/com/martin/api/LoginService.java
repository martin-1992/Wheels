package com.martin.api;

import com.martin.annotation.RpcInterface;


@RpcInterface
public interface LoginService {

    boolean login();
}
