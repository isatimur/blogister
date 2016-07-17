package com.isatimur.blog.cucumber.stepdefs;

import com.isatimur.blog.BlogisterApp;

import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

@WebAppConfiguration
@ContextConfiguration(classes = BlogisterApp.class, loader = SpringApplicationContextLoader.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
