package com.itcast.core.service;

import com.itcast.core.pojo.seller.Seller;
import com.itcast.core.service.seller.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义认证类
 */
public class UserDetailServiceImpl implements UserDetailsService{

    // 注入SellerService
    private SellerService sellerService;
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * 认证+授权
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 认证
        Seller seller = sellerService.findOne(username);
        if(seller != null && "1".equals(seller.getStatus())){ // 必须审核通过的商家才能登录
            // 认证成功，需要授权
            Set<GrantedAuthority> authorities = new HashSet<>(); // 该用户的权限集
            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_SELLER");
            authorities.add(grantedAuthority);
            User user = new User(username, seller.getPassword(), authorities);
            return user;
        }
        return null;
    }
}
