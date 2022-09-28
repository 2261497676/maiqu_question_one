package com.cfl.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cfl.common.BaseResponse;
import com.cfl.common.ErrorCode;
import com.cfl.common.ResultUtils;
import com.cfl.exception.BusinessException;
import com.cfl.mapper.UserMapper;
import com.cfl.model.User;
import com.cfl.model.request.UserForgetPassword;
import com.cfl.model.request.UserUpdatePasswordRequest;
import com.cfl.model.request.UserLoginRequest;
import com.cfl.model.request.UserRegisterRequest;
import com.cfl.service.UserService;
import com.cfl.utils.PageUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cfl.constant.UserConstant.USER_LOGIN_STATE;

@Api(tags = "用户模块")
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"对象为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String phone = userRegisterRequest.getPhone();
        String email = userRegisterRequest.getEmail();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,phone,email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, phone, email);
        return ResultUtils.success(result);
    }

    @PostMapping("/loginBySession")
    public BaseResponse<User> userLoginBySession(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"对象为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数为空");
        }
        User user = userService.doLoginBySession(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/loginByToken")
    public BaseResponse<String> userLoginByToken(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        if(userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"对象为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数为空");
        }

        String token = userService.doLoginByToken(userAccount,userPassword);
        return ResultUtils.success(token);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        int result = userService.userLogOut(request);
        return ResultUtils.success(result);
    }

    @PostMapping("/cfl/logoutByToken")
    public BaseResponse<Integer> logout(HttpServletRequest request) {
        int result = userService.logOutByToken(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/currentBySession")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User userObj = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"未登录取不到用户信息");
        }
        User user = userService.getById(userObj.getId());
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/currentByToken")
    public BaseResponse<User> getCurrentUserByToken(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUserByToken = userService.getLoginUserByToken(request);
        return ResultUtils.success(loginUserByToken);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request) {
        if(!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"未登录取不到用户信息");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            queryWrapper.eq("username",username);
        }
        List<User> users = userService.list(queryWrapper);
        List<User> result = users.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());;
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request) {
        if(user == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/updateByToken")
    public BaseResponse<Integer> updateUserByToken(@RequestBody User user,HttpServletRequest request) {
        if(user == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUserByToken = userService.getLoginUserByToken(request);
        if(loginUserByToken == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        int result = userService.updateUser(user, loginUserByToken);
        return ResultUtils.success(result);
    }

    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request) {
        if(!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"未登录取不到用户信息");
        }
        if(id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"删除的用户id小于0，不存在");
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @RequestMapping("/cfl/list")
    public BaseResponse<PageUtils> list(Map<String,Object> params) {
        PageUtils page = userService.queryPage(params);
        return ResultUtils.success(page);
    }

    @RequestMapping("/cfl/save")
    public BaseResponse<User> save(@RequestBody User user) {
        userService.save(user);
        return ResultUtils.success(user);
    }

    @DeleteMapping("/cfl/delete/{id}")
    public BaseResponse<Boolean> delete(@PathVariable("id") long id) {
        boolean b = userService.removeById(id);
        if(b){
            return ResultUtils.success(b);
        }
        return ResultUtils.error(4,"删除失败","删除失败");
    }

    @DeleteMapping("/cfl/delete")
    public BaseResponse<Boolean> delete(@RequestBody Long[] ids) {
        boolean b = userService.removeByIds(Arrays.asList(ids));
        if(b){
            return ResultUtils.success(b);
        }
       return ResultUtils.error(4,"删除失败","删除失败");
    }

    @RequestMapping("/cfl/search/{id}")
    public BaseResponse<User> searchById(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        if(user != null)
        return ResultUtils.success(user);
        return ResultUtils.error(4,"没有找到用户","没有找到用户");
    }

    @GetMapping("/cfl/findAll")
    public BaseResponse<List<User>> findAll() {
        List<User> userList = userService.list();
        if(userList!=null && userList.size()>0) {
            return ResultUtils.success(userList);
        }
        return ResultUtils.error(4,"没有找到用户","没有找到用户");
    }

    @PostMapping("/cfl/updatePassword")
    public BaseResponse<Long> updatePassword(@RequestBody UserUpdatePasswordRequest userForgetRequest) {
        if(userForgetRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数不能为空");
        }
        String userAccount = userForgetRequest.getUserAccount();
        String oldPassword = userForgetRequest.getOldPassword();
        String newPassword = userForgetRequest.getNewPassword();
        String checkPassword = userForgetRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount,oldPassword,newPassword,checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数为空");
        }
        long result = userService.updatePassword(userAccount, oldPassword, newPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/cfl/forgetPassword")
    public BaseResponse<Long> forgetPassword(@RequestBody UserForgetPassword userForgetPassword) {
        if(userForgetPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数不能未空");
        }
        String userAccount = userForgetPassword.getUserAccount();
        String phone = userForgetPassword.getPhone();
        String email = userForgetPassword.getEmail();
        String newPassword = userForgetPassword.getNewPassword();
        String checkPassword = userForgetPassword.getCheckPassword();
        long result = userService.forgetPassword(userAccount, phone, email, newPassword, checkPassword);
        return ResultUtils.success(result);
    }

}
