package com.padingpading.we_com.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Date: 2023/2/22
 * Time: 9:42 AM
 *
 * @author lzk
 */
@Slf4j
@Service
public class GdtAdServiceImpl implements GdtAdService {

    private static final String URL_SUFFIX = "?access_token=%s&timestamp=%s&nonce=%s";
    private static final String CONVERSION_CODE = "COMPLETE_ORDER";
    private static final BigDecimal ONE_100 = new BigDecimal("100");
    private static final BigDecimal SIXTY_PERCENT = new BigDecimal("0.6");
    @Resource
    private AdGdtTokenService adGdtTokenService;
    @Resource
    private AdGdtConversionBackService adGdtConversionBackService;
    @Resource
    private AdGdtCallbackService adGdtCallbackService;
    @Resource
    private MemberPlatformService memberPlatformService;
    @Resource
    private AdGdtCallbackUnProcessService adGdtCallbackUnProcessService;
    @Resource
    private AdGdtBillService adGdtBillService;

    @Override
    public void callbackGdt(OrderDetailVO curOrderInfo) {
        List<MemberPlatform> memberPlatforms = memberPlatformService.list(new LambdaQueryWrapper<MemberPlatform>().eq(MemberPlatform::getTenantId, Long.parseLong(curOrderInfo.getTenantId())).eq(MemberPlatform::getUserId, curOrderInfo.getBuyerNo()).eq(MemberPlatform::getIsDelete, Boolean.FALSE));
        if (CollectionUtils.isEmpty(memberPlatforms)) {
            log.warn("[GdtAdServiceImpl]无法关联到用户，存入unProcess表,tenantId:{},userId:{}", curOrderInfo.getTenantId(), curOrderInfo.getBuyerNo());
            AdGdtCallbackUnProcess adGdtCallbackUnProcess = new AdGdtCallbackUnProcess();
            adGdtCallbackUnProcess.setTimes(0);
            adGdtCallbackUnProcess.setOrderBody(JSONObject.toJSONString(curOrderInfo));
            adGdtCallbackUnProcess.setLogStatus(0);
            Date now = new Date();
            adGdtCallbackUnProcess.setCreateTime(now);
            adGdtCallbackUnProcess.setUpdateTime(now);
            adGdtCallbackUnProcessService.save(adGdtCallbackUnProcess);
            return;
        }
        for (MemberPlatform memberPlatform : memberPlatforms) {
            String unionId = memberPlatform.getUnionId();
            List<AdGdtCallback> adGdtCallbacks = adGdtCallbackService.listAdCallback(unionId);
            adGdtCallbacks = filterAdCallback(adGdtCallbacks);
            if (CollectionUtils.isEmpty(adGdtCallbacks)) {
                log.info("[GdtAdServiceImpl]sendGdtCallback,未找到待绑定的unionId:" + unionId);
                //没有需要回传的转化Id
                continue;
            }

            log.info("[GdtAdServiceImpl]sendGdtCallback,tenantId:{},userId:{}", curOrderInfo.getTenantId(), curOrderInfo.getBuyerNo());
            sendGdtCallbacks(adGdtCallbacks, curOrderInfo);
        }
    }

    @Override
    public void generateToken(String authorizationCode, String tId) {
        String generateTokenUrl = GdtConstant.GENERATE_TOKEN_URL + authorizationCode;
        log.info("[GdtAdService]reqGenerateToken:" + generateTokenUrl);
        final String resp = OkHttpUtils.get(generateTokenUrl);
        log.info("[GdtAdService]reqGenerateToken resp:" + resp);
        final GdtTokenResp gdtInitTokenResp = JSON.parseObject(resp, GdtTokenResp.class);
        AdGdtToken adGdtToken = new AdGdtToken();
        Date now = new Date();
        adGdtToken.setAccessTokenRefreshTime(now);
        final InitTokenResp data = gdtInitTokenResp.getData();
        adGdtToken.setAccessToken(data.getAccessToken());
        adGdtToken.setAccessTokenExpiresIn(data.getAccessTokenExpiresIn() + "");
        adGdtToken.setRefreshToken(data.getRefreshToken());
        final AuthorizerInfoResp authorizerInfo = data.getAuthorizerInfo();
        final Integer accountId = authorizerInfo.getAccountId();
        final AdGdtToken gdtToken = adGdtTokenService.getOne(new LambdaQueryWrapper<AdGdtToken>().eq(AdGdtToken::getAccountId, (long) accountId));
        if (gdtToken != null) {
            throw new LunaException("yi shou quan");
        }
        adGdtToken.setAccountId((long) accountId);
        adGdtToken.setAccountName(authorizerInfo.getAccountName());
        adGdtToken.setRefreshTokenExpiresIn(data.getRefreshTokenExpiresIn() + "");
        adGdtToken.setLoginName(authorizerInfo.getLoginName());
        adGdtToken.setAuthorizationCode(authorizationCode);
        adGdtToken.setCreateTime(now);
        adGdtToken.setUpdateTime(now);
        adGdtToken.setTenantId(tId);
        adGdtTokenService.save(adGdtToken);
        try {
            initMktSource(adGdtToken);
        } catch (Exception e) {
            log.warn("初始化数据源失败", e);
            throw new LunaException("create source failed:" + adGdtToken.getAccountId());
        }
    }


    @Override
    public void initMktSource(AdGdtToken adGdtToken) {
        final String reqUrl = buildReqUrl(GdtConstant.SOURCE_INTERFACE, adGdtToken.getAccessToken());
        GdtAddSourceReq gdtAddSourceReq = new GdtAddSourceReq();
        gdtAddSourceReq.setType("WEB");
        gdtAddSourceReq.setName(GdtConstant.SOURCE_NAME);
        gdtAddSourceReq.setAccountId(adGdtToken.getAccountId() + "");
        log.info("[GdtAdService]initMktSource:" + reqUrl);
        final String postJson = OkHttpUtils.postJson(reqUrl, gdtAddSourceReq);
        log.info("[GdtAdService]initMktSource resp:" + postJson);
        final GdtAddSourceResp gdtAddSourceResp = JSON.parseObject(postJson, GdtAddSourceResp.class);
        if (gdtAddSourceResp != null && gdtAddSourceResp.getData() != null) {
            adGdtToken.setSourceId(gdtAddSourceResp.getData().getUserActionSetId() + "");
            adGdtTokenService.updateById(adGdtToken);
            final String test = buildReqUrl(GdtConstant.UPLOAD_INTERFACE, adGdtToken.getAccessToken());
        } else {
            log.error("[GdtAdService]初始化数据源失败 resp:" + postJson);
            throw new LunaException("创建数据源失败:" + adGdtToken.getAccountId());
        }
    }

    @Override
    public void sendGdtCallbacks(List<AdGdtCallback> adGdtCallbacks, OrderDetailVO curOrderInfo) {
        final List<AdGdtToken> adGdtTokens = adGdtTokenService.list();
        Map<String, AdGdtToken> adGdtTokenMap = new HashMap<>(adGdtTokens.size() * 2);
        for (AdGdtToken adGdtToken : adGdtTokens) {
            adGdtTokenMap.put(adGdtToken.getAccountId() + "", adGdtToken);
        }
        Map<String, AdGdtCallback> adGdtCallbackMap = new HashMap<>(adGdtCallbacks.size() * 2);
        for (AdGdtCallback adGdtCallback : adGdtCallbacks) {
            adGdtCallbackMap.put(adGdtCallback.getAccountId(), adGdtCallback);
        }
        for (Map.Entry<String, AdGdtCallback> adGdtCallbackEntry : adGdtCallbackMap.entrySet()) {
            final AdGdtCallback adGdtCallback = adGdtCallbackEntry.getValue();
            doUploadCallback(adGdtCallback, curOrderInfo, adGdtTokenMap);
        }
    }

    @Override
    public void refreshTokenExpire(AdGdtToken adGdtToken) {
        final String reqUrl = String.format(GdtConstant.TOKEN_URL, GdtConstant.CLIENT_ID + "", GdtConstant.CLIENT_SECRET, adGdtToken.getRefreshToken());
        final String resp = OkHttpUtils.get(reqUrl);
        final GdtTokenResp gdtTokenResp = JSON.parseObject(resp, GdtTokenResp.class);
        final InitTokenResp data = gdtTokenResp.getData();
        if (!adGdtToken.getAccessToken().equals(data.getAccessToken())) {
            adGdtToken.setAccessToken(data.getAccessToken());
            log.info("[GdtTokenRefreshJob]token刷新,老token:{},新token:{}", adGdtToken.getAccessToken(), data.getAccessToken());
        }
        adGdtToken.setUpdateTime(new Date());
        adGdtTokenService.updateById(adGdtToken);
        log.info("[GdtTokenRefreshJob]refreshGdtToken,req:{},resp:{}", reqUrl, resp);
    }

    public static void main(String[] args) {
        GdtAdServiceImpl gdtAdService = new GdtAdServiceImpl();
        AdGdtToken adGdtToken = new AdGdtToken();
        adGdtToken.setAccessToken("2715e1880421e4a3efc5e72c8850260d");
        adGdtToken.setAccountId(26003665L);
        gdtAdService.syncBill(adGdtToken, "2023-03-12");
    }

    @Override
    public int syncBill(AdGdtToken adGdtToken, String syncDate) {
        final String reqUrl = buildReqUrl(GdtConstant.LOAD_BILL, adGdtToken.getAccessToken());
        OkHttpUtils.Params params = new OkHttpUtils.Params();
        Long accountId = adGdtToken.getAccountId();
        params.add("account_id", accountId);
        params.add("date_range", "{\"start_date\":\"" + syncDate + "\",\"end_date\":\"" + syncDate + "\"}");
        params.add("page", 1);
        params.add("page_size", 50);
        log.info("[syncBill]reqUrl:{},req:{}", reqUrl, JSONObject.toJSONString(params));
        long start = System.currentTimeMillis();
        final String resp = OkHttpUtils.get(reqUrl, params);
        log.info("[syncBill]resp {},cost {} ms", resp, (System.currentTimeMillis() - start));
        final GdtBillResp gdtBillResp = JSON.parseObject(resp, GdtBillResp.class);
        if (gdtBillResp == null) {
            return 0;
        }
        final GdtBillList data = gdtBillResp.getData();
        if (data == null) {
            return 0;
        }
        final List<GdtBill> list = data.getList();
        Date now = new Date();
        for (GdtBill gdtBill : list) {
            AdGdtBill gdtBillServiceOne = adGdtBillService.getOne(new LambdaQueryWrapper<AdGdtBill>()
                    .eq(AdGdtBill::getSyncData, syncDate)
                    .eq(AdGdtBill::getAccountId, accountId)
                    .eq(AdGdtBill::getFundType, gdtBill.getFundType()));
            if (gdtBillServiceOne == null) {
                AdGdtBill adGdtBill = new AdGdtBill();
                BeanUtils.copyProperties(gdtBill, adGdtBill);
                adGdtBill.setSetTime(gdtBill.getTime());
                adGdtBill.setCreateTime(now);
                adGdtBill.setUpdateTime(now);
                adGdtBill.setSyncData(syncDate);
                adGdtBillService.save(adGdtBill);
            }
        }
        return list.size();
    }

    private void doUploadCallback(AdGdtCallback adGdtCallback, OrderDetailVO curOrderInfo, Map<String, AdGdtToken> adGdtTokenMap) {
        final String accountId = adGdtCallback.getAccountId();
        final AdGdtToken adGdtToken = adGdtTokenMap.get(accountId);
        if (adGdtToken == null) {
            return;
        }
        final String reqUrl = buildReqUrl(GdtConstant.UPLOAD_INTERFACE, adGdtToken.getAccessToken());
        BigDecimal multiply = curOrderInfo.getPaymentAmount().multiply(ONE_100);
        //假如是简美的，打6折上报腾讯
        if ("2060285017638520476".equals(curOrderInfo.getTenantId())) {
            multiply = multiply.multiply(SIXTY_PERCENT).setScale(2, RoundingMode.HALF_UP);
        }
        final GdtCallbackActionParam actionParam = GdtCallbackActionParam.builder().orderNo(curOrderInfo.getOrderNo()).value(multiply.longValue()).build();
        final GdtCallbackUserInfo gdtCallbackUserInfo = GdtCallbackUserInfo.builder().wechatUnionId(adGdtCallback.getUnionId()).build();

        final GdtCallbackTrace gdtCallbackTrace = GdtCallbackTrace.builder().clickId(adGdtCallback.getTraceId()).build();
        final GdtCallbackAction callbackAction = GdtCallbackAction.builder().actionParam(actionParam).userId(gdtCallbackUserInfo).actionTime(curOrderInfo.getOrderTime().getTime() / 1000).actionType(CONVERSION_CODE).trace(gdtCallbackTrace).build();
        final GdtCallbackInfo gdtCallbackInfo = GdtCallbackInfo.builder().accountId(accountId).userActionSetId(adGdtToken.getSourceId()).actions(Lists.newArrayList(callbackAction)).build();
        final String resp = OkHttpUtils.postJson(reqUrl, gdtCallbackInfo);
        AdGdtConversionBack adGdtConversionBack = new AdGdtConversionBack();
        adGdtConversionBack.setConversionId(adGdtCallback.getId());
        adGdtConversionBack.setReqUrl(reqUrl);
        adGdtConversionBack.setResp(resp);
        Date now = new Date();
        adGdtConversionBack.setTraceId(adGdtCallback.getTraceId());
        adGdtConversionBack.setUnionId(adGdtCallback.getUnionId());
        adGdtConversionBack.setCreateTime(now);
        adGdtConversionBack.setUpdateTime(now);
        adGdtConversionBack.setAccountId(adGdtToken.getAccountId());
        adGdtConversionBack.setSendBody(JSONObject.toJSONString(gdtCallbackInfo));
        adGdtConversionBackService.save(adGdtConversionBack);
    }

    private static List<AdGdtCallback> filterAdCallback(List<AdGdtCallback> adGdtCallbacks) {
        if (CollectionUtils.isEmpty(adGdtCallbacks)) {
            return adGdtCallbacks;
        }
        Set<String> accountSet = new HashSet<>();
        List<AdGdtCallback> resultCallbacks = new ArrayList<>(adGdtCallbacks.size());
        for (AdGdtCallback adGdtCallback : adGdtCallbacks) {
            final String accountId = adGdtCallback.getAccountId();
            if (StringUtils.isBlank(accountId)) {
                //假如账户ID为空，不处理
                continue;
            }
            if (!accountSet.contains(accountId)) {
                resultCallbacks.add(adGdtCallback);
                accountSet.add(accountId);
            }
        }
        return resultCallbacks;
    }

    private String buildReqUrl(String interfaceName, String token) {
        final String baseReqUrl = GdtConstant.BASE_URL + interfaceName + URL_SUFFIX;
        final String guid = UUID.randomUUID().toString().replace("-", "");
        final long second = System.currentTimeMillis() / 1000;
        return String.format(baseReqUrl, token, second, guid);
    }
}
