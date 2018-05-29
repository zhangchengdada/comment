package com.netease.comment.config;


import com.alibaba.druid.support.json.JSONUtils;
import com.netease.comment.dto.BaseResponse;
import com.netease.comment.enums.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wb.zhangcheng
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(value = SQLException.class)
  @ResponseBody
  public BaseResponse dbErrorHandler(HttpServletRequest req, SQLException e) {
    log.error("数据库访问异常", e);
    return BaseResponse.createError(
        ResponseCodeEnum.DATABASE_ERROR, "500");
  }

  @ExceptionHandler(value = Exception.class)
  @ResponseBody
  public BaseResponse commonErrorHandler(HttpServletRequest req, Exception e) {
    log.error("服务器异常", e);
    return BaseResponse.createError(
            ResponseCodeEnum.SERVER_EXCEPTION, "500");
  }

  /** 参数异常处理 */
  @ExceptionHandler(
    value = {
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      ConstraintViolationException.class
    }
  )
  @ResponseBody
  public BaseResponse handleValidationException(HttpServletRequest req, Exception e) {
    List<String> errors = new LinkedList<>();
    if (e instanceof MissingServletRequestParameterException) {
      MissingServletRequestParameterException missingException =
          (MissingServletRequestParameterException) e;
      errors.add(missingException.getParameterName() + "不可以为空");
    } else if (e instanceof MethodArgumentTypeMismatchException) {
      MethodArgumentTypeMismatchException mismatchException =
          (MethodArgumentTypeMismatchException) e;
      errors.add(mismatchException.getName() + "格式不匹配");
    } else if (e instanceof ConstraintViolationException) {
      ConstraintViolationException violationException = (ConstraintViolationException) e;
      for (ConstraintViolation<?> s : violationException.getConstraintViolations()) {
        errors.add(s.getMessage());
      }
    }
    log.warn(
        "invalid request parameter, requestId = {}, path = {}, parameters = {}, errors = {}",
        req.getRequestedSessionId(),
        req.getRequestURI(),
        JSONUtils.toJSONString(req.getParameterMap()),
            JSONUtils.toJSONString(errors));

    return BaseResponse.createError(ResponseCodeEnum.PARAMETER_ERROR,
        "500");
  }
}
