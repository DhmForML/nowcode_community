package com.newcode.community.community.util;

import com.newcode.community.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUsers(User user){
        users.set(user);
    }

    public User getUsers(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
