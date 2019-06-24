package com.itcast.core.controller.addr;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.pojo.address.Address;
import com.itcast.core.service.addr.AddrService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddrController {
    @Reference
    private AddrService addrService;
    @RequestMapping("/findListByLoginUser.do")
    public List<Address> findListByLoginUser(){
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        return addrService.findListByLoginUser(userId);
    }
}
