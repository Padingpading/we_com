package com.padingpading.we_com.controller;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author libin
 * @description
 * @date 2023-03-24
 */
@Data
public class UserUpDATE {
    private String account_id;
    private String user_action_set_id;
    private List<UserActions> list;
    private Map<String,String> action_param;
}
