package se.flinker.document.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

import se.flinker.document.Tx;



abstract class BaseController {

    @Autowired
    private Tx tx;
    @Autowired
    @Qualifier("app-executor")
    protected AsyncTaskExecutor executor;
    @Autowired
    protected HttpServletRequest req;
    
    protected String tx() {
        return tx.tx();
    }

}
